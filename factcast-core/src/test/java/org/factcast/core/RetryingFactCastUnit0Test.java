package org.factcast.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.factcast.core.store.RetryableException;
import org.mockito.Mockito;

public class RetryingFactCastUnit0Test {
    @org.junit.jupiter.api.Test
    void publishFails() throws Exception {

        FactCast fc = mock(FactCast.class);

        doThrow(new RetryableException(new RuntimeException(""))).when(fc).publish(Mockito.any(
                Fact.class));

        RetryingFactCast uut = new RetryingFactCast(fc, 8);

        assertThrows(MaxRetryAttemptsExceededException.class, () -> {
            uut.publish(Fact.builder().ns("foo").build("{}"));
        });

        verify(fc, times(8)).publish(Mockito.any(Fact.class));
        verifyNoMoreInteractions(fc);

    }

    @org.junit.jupiter.api.Test
    void publishSucceeds() throws Exception {

        FactCast fc = mock(FactCast.class);

        doThrow(new RetryableException(new RuntimeException(""))).doNothing().when(fc).publish(
                Mockito.any(Fact.class));

        RetryingFactCast uut = new RetryingFactCast(fc, 8);

        uut.publish(Fact.builder().ns("foo").build("{}"));

        verify(fc, times(2)).publish(Mockito.any(Fact.class));
        verifyNoMoreInteractions(fc);
    }
}
