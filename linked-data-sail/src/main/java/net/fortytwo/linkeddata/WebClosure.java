/*
 * $URL$
 * $Revision$
 * $Author$
 *
 * Copyright (C) 2007-2010 Joshua Shinavier
 */


package net.fortytwo.linkeddata;

import net.fortytwo.flow.rdf.RDFBuffer;
import net.fortytwo.flow.rdf.RDFSink;
import net.fortytwo.flow.rdf.SesameInputAdapter;
import net.fortytwo.flow.rdf.SingleContextPipe;
import net.fortytwo.ripple.Ripple;
import net.fortytwo.ripple.RippleException;
import net.fortytwo.ripple.StringUtils;
import net.fortytwo.ripple.URIMap;
import net.fortytwo.ripple.util.BNodeToURIFilter;
import net.fortytwo.ripple.util.RDFUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.sail.SailConnection;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A configurable container of URI dereferencers and RDFizers which provides a unified view of the Web as a collection of RDF documents.
 * 
 * Note: this tool stores metadata about web activity; if a suitable
 * dereferencer cannot be found for a URI, no metadata will be stored.
 * <p/>
 * Author: josh
 * Date: Jan 16, 2008
 * Time: 12:25:29 PM
 */
public class WebClosure
{
    // TODO: these should probably not be HTTP URIs
    public static final String
            CACHE_NS = "http://fortytwo.net/2008/01/webclosure#",
            CACHE_CONTEXT = CACHE_NS + "context",
            CACHE_MEMO = CACHE_NS + "memo",
            FULL_MEMO = CACHE_NS + "fullMemo";

    private static final Logger LOGGER = Logger.getLogger(WebClosure.class);

    private final WebCache cache;

    // Maps media types to Rdfizers
    private final Map<MediaType, MediaTypeInfo> rdfizers
            = new HashMap<MediaType, MediaTypeInfo>();

    // Maps URI schemes to Dereferencers
    private final Map<String, Dereferencer> dereferencers = new HashMap<String, Dereferencer>();

    private final URIMap uriMap;
    private final ValueFactory valueFactory;
    private final boolean useBlankNodes;

    private String acceptHeader = null;

    public WebClosure(final WebCache cache,
                      final URIMap uriMap,
                      final ValueFactory valueFactory) throws RippleException {
        this.cache = cache;
        this.valueFactory = valueFactory;
        this.uriMap = uriMap;
        useBlankNodes = Ripple.getProperties().getBoolean(Ripple.USE_BLANK_NODES);
    }

    public String getAcceptHeader() {
        if (null == acceptHeader) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            // Order from highest quality to lowest.
            Comparator<MediaTypeInfo> comparator
                    = new Comparator<MediaTypeInfo>() {
                public int compare(final MediaTypeInfo first,
                                   final MediaTypeInfo second) {
                    return first.quality < second.quality ? 1 : first.quality > second.quality ? -1 : 0;
                }
            };

            MediaTypeInfo[] array = new MediaTypeInfo[rdfizers.size()];
            rdfizers.values().toArray(array);
            Arrays.sort(array, comparator);

            for (MediaTypeInfo m : array) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(m.mediaType.getName());
                double quality = m.quality;
                if (1.0 != quality) {
                    sb.append(";q=").append(quality);
                }
            }

            acceptHeader = sb.toString();
        }

        return acceptHeader;
    }

    public void addRdfizer(final MediaType mediaType,
                           final Rdfizer rdfizer,
                           final double qualityFactor) {
        if (qualityFactor <= 0 || qualityFactor > 1) {
            throw new IllegalArgumentException("quality factor must be between 0 and 1");
        }

        MediaTypeInfo rq = new MediaTypeInfo();
        rq.mediaType = mediaType;
        rq.quality = qualityFactor;
        rq.rdfizer = rdfizer;
        rdfizers.put(mediaType, rq);

        acceptHeader = null;
    }

    public void addRdfizer(final MediaType mediaType, final Rdfizer rdfizer) {
        addRdfizer(mediaType, rdfizer, 1.0);
    }

    public void addDereferencer(final String scheme, final Dereferencer uriDereferencer) {
        dereferencers.put(scheme, uriDereferencer);
    }

    public void addMemo(final String uri,
                        final ContextMemo memo,
                        final SailConnection sc) throws RippleException {
        cache.setMemo(uri, memo, sc);
    }

    public ContextMemo.Status extendTo(final URI nonInfoURI,
                                       final RDFSink<RippleException> resultSink,
                                       final SailConnection sc) throws RippleException {
        // TODO: memos should be inferred in a scheme-specific way
        String defrag = RDFUtils.removeFragmentIdentifier(nonInfoURI.toString());

        ContextMemo memo;
        Dereferencer dref;

        // Note: this URL should be treated as a "black box" once created; it
        // need not resemble the URI it was created from.
        String mapped;

        // Rules out an otherwise possible race condition
        synchronized (cache) {
            memo = cache.getMemo(defrag, sc);

            if (null != memo) {
                // Don't log success or failure based on cached values.
                return memo.getStatus();
            }

            try {
                mapped = uriMap.get(defrag);
            }

            catch (RippleException e) {
                // Don't log extremely common errors.
                return ContextMemo.Status.InvalidUri;
            }

            try {
                dref = chooseDereferencer(mapped);
            }

            catch (RippleException e) {
                e.logError(false);

                // Don't log extremely common errors.
                return ContextMemo.Status.InvalidUri;
            }

            if (null == dref) {
                // Don't log extremely common errors.
                return ContextMemo.Status.BadUriScheme;
            }

            LOGGER.info("Dereferencing URI <"
                    + StringUtils.escapeURIString(nonInfoURI.toString()) + ">");
            //+ " at location " + mapped );

            memo = new ContextMemo(ContextMemo.Status.Success);
            cache.setMemo(defrag, memo, sc);
        }

        memo.setUriDereferencer(dref);

        // Note: from this point on, failures are explicitly stored as caching
        // metadata.

        Representation rep;

        try {
            rep = dref.dereference(mapped);
        }

        catch (RippleException e) {
            e.logError();
            memo.setStatus(ContextMemo.Status.DereferencerError);
            return logStatus(nonInfoURI, memo.getStatus());
        }

        catch (Throwable t) {
            memo.setStatus(ContextMemo.Status.DereferencerError);
            logStatus(nonInfoURI, memo.getStatus());
            throw new RippleException(t);
        }

        MediaType mt;

        try {
            mt = rep.getMediaType();
        }

        catch (Throwable t) {
            throw new RippleException(t);
        }

//System.out.println( "media type = " + mt );
        memo.setMediaType(mt);

        Rdfizer rfiz = chooseRdfizer(mt);
        if (null == rfiz) {
            memo.setStatus(ContextMemo.Status.BadMediaType);
            memo.setMediaType(mt);
            return logStatus(nonInfoURI, memo.getStatus());
        }

        memo.setRdfizer(rfiz);

        URI context;

        try {
            context = valueFactory.createURI(defrag);
        }

        catch (Throwable t) {
            throw new RippleException(t);
        }

        // Note: any pre-existing context information is discarded.
        RDFSink<RippleException> scp = new SingleContextPipe(resultSink, context, valueFactory);

        RDFBuffer<RippleException> results = new RDFBuffer<RippleException>(scp);
        RDFHandler hdlr = new SesameInputAdapter(useBlankNodes
                ? results
                : new BNodeToURIFilter(results, valueFactory));

        InputStream is;

        try {
            is = rep.getStream();
        }

        catch (IOException e) {
            throw new RippleException(e);
        }

        catch (Throwable t) {
            throw new RippleException(t);
        }

        // For now...
        String baseUri = defrag;

        ContextMemo.Status status;

        status = rfiz.handle(is, hdlr, nonInfoURI, baseUri);

        if (ContextMemo.Status.Success == status) {
            // Push results and record success
            results.flush();
        }

        memo.setStatus(status);

        return logStatus(nonInfoURI, status);
    }

    private ContextMemo.Status logStatus(final URI uri, final ContextMemo.Status status) {
        if (ContextMemo.Status.Success != status) {
            LOGGER.info("Failed to dereference URI <"
                    + StringUtils.escapeURIString(uri.toString()) + ">: " + status);
        }

        return status;
    }

    private Dereferencer chooseDereferencer(final String uri) throws RippleException {
        String scheme;

        try {
            scheme = new java.net.URI(uri).getScheme();
        }

        catch (URISyntaxException e) {
            throw new RippleException(e);
        }

        return dereferencers.get(scheme);
    }

    private Rdfizer chooseRdfizer(final MediaType mediaType) throws RippleException {
        MediaTypeInfo rq = rdfizers.get(mediaType);
        return (null == rq) ? null : rq.rdfizer;
    }

    private class MediaTypeInfo {
        MediaType mediaType;
        public double quality;
        public Rdfizer rdfizer;
    }
}
