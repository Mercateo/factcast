package org.factcast.core.store;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class RetryableExceptionTest {

    @Test
    public void testValidatesMinimumWaitTime() throws Exception {
        RuntimeException rte = mock(RuntimeException.class);
        long minimumWaitTime = -42;

        assertThrows(IllegalArgumentException.class, () -> new RetryableException(rte,
                minimumWaitTime));
    }

}
