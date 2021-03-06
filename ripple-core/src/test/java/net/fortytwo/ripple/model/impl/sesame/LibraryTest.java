package net.fortytwo.ripple.model.impl.sesame;

import net.fortytwo.ripple.model.ModelConnection;
import net.fortytwo.ripple.model.PrimitiveStackMapping;
import net.fortytwo.ripple.test.RippleTestCase;

import java.net.URI;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class LibraryTest extends RippleTestCase {
    public void testPrimitiveAlias() throws Exception {
        ModelConnection mc = this.modelConnection;

        URI dup05 = URI.create("http://fortytwo.net/2007/05/ripple/stack#dup");
        URI dup08 = URI.create("http://fortytwo.net/2007/08/ripple/stack#dup");

        Object dup05Val = mc.canonicalValue(mc.valueOf(dup05));
        Object dup08Val = mc.canonicalValue(mc.valueOf(dup08));

        assertNotNull(dup05Val);
        assertNotNull(dup08Val);
        assertTrue(dup05Val instanceof PrimitiveStackMapping);
        assertTrue(dup08Val instanceof PrimitiveStackMapping);

        assertEquals(dup05Val, dup08Val);
    }

    public void testAliasInExpression() throws Exception {
        assertReducesTo("<http://fortytwo.net/2007/05/ripple/stack#dup>",
                "<http://fortytwo.net/2007/08/ripple/stack#dup>");
        assertReducesTo("2 <http://fortytwo.net/2007/05/ripple/stack#dup>.", "2 2");
        assertReducesTo("2 <http://fortytwo.net/2007/08/ripple/stack#dup>.", "2 2");
    }

    public void testAliasesAsKeywords() throws Exception {
        assertReducesTo("dup", "<http://fortytwo.net/2007/05/ripple/stack#dup>");
        assertReducesTo("2 dup.", "2 <http://fortytwo.net/2007/05/ripple/stack#dup>.");

        assertReducesTo("xsd:type", "type");
        assertReducesTo("xsd:type", "<http://www.w3.org/2001/XMLSchema#type>");
        assertReducesTo("42 xsd:type.", "42 type.");
        assertReducesTo("42 type.", "42 <http://www.w3.org/2001/XMLSchema#type>.");
    }
}
