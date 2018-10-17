package org.factcast.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;

public class Fact0Test {

    @Test(expected = NullPointerException.class)
    public void testOfNull1() throws Exception {
        Fact.of(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void testOfNull2() throws Exception {
        Fact.of("", null);
    }

    @Test(expected = NullPointerException.class)
    public void testOfNull() throws Exception {
        Fact.of(null, null);
    }

    @Test
    public void testOf() throws Exception {
        Test0Fact f = new Test0Fact();
        Fact f2 = Fact.of(f.jsonHeader(), f.jsonPayload());

        assertEquals(f.id(), f2.id());
    }

    @Test
    public void testBefore() throws Exception {
        Fact one = Fact.of("{" +
                "\"ns\":\"ns\"," +
                "\"id\":\"" + UUID.randomUUID() + "\"," +
                "\"meta\":{ \"_ser\":1 }" +
                "}", "{}");
        Fact two = Fact.of("{" +
                "\"ns\":\"ns\"," +
                "\"id\":\"" + UUID.randomUUID() + "\"," +
                "\"meta\":{ \"_ser\":2 }" +
                "}", "{}");
        Fact three = Fact.of("{" +
                "\"ns\":\"ns\"," +
                "\"id\":\"" + UUID.randomUUID() + "\"," +
                "\"meta\":{ \"_ser\":3 }" +
                "}", "{}");

        assertTrue(one.before(two));
        assertTrue(two.before(three));
        assertTrue(one.before(three));

        assertFalse(one.before(one));
        assertFalse(two.before(one));
        assertFalse(three.before(one));
        assertFalse(three.before(two));

    }

    @Test(expected = IllegalStateException.class)
    public void testSerialUnset() throws Exception {
        Fact.of("{" +
                "\"ns\":\"ns\"," +
                "\"id\":\"" + UUID.randomUUID() + "\"" +
                "}", "{}").serial();

    }

}
