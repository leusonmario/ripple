package net.fortytwo.ripple.libs.string;

import net.fortytwo.ripple.test.RippleTestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SubstringTest extends RippleTestCase {
    public void testSimple() throws Exception {
        assertReducesTo("\"Mississippi\" 0 11 substring.", "\"Mississippi\"");
        assertReducesTo("\"Mississippi\" 1 8 substring.", "\"ississi\"");
    }

    public void testOutOfRangeIndices() throws Exception {
        assertReducesTo("\"Mississippi\" -1 11 substring.");
        assertReducesTo("\"Mississippi\" 0 12 substring.");
    }

    public void testEqualIndices() throws Exception {
        assertReducesTo("\"Mississippi\" 2 2 substring.", "\"\"");
    }

    public void testOutOfOrderIndices() throws Exception {
        assertReducesTo("\"Mississippi\" 5 2 substring.");
    }

    public void testEmptyStrings() throws Exception {
        assertReducesTo("\"\" 0 0 substring.", "\"\"");
    }
}
