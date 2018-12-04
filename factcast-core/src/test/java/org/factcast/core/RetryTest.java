package org.factcast.core;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.factcast.core.store.FactStore;
import org.factcast.core.store.RetryableException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RetryTest {
    @Test
    void testHappyPath() throws Exception {
        // arrange

        // Note: we intended the retryableException to be passed from store to factcast,
        // so we mock the store here
        FactStore fs = mock(FactStore.class);
        doThrow(new RetryableException(new IllegalStateException()))//
                .doThrow(new RetryableException(new IllegalArgumentException()))//
                .doNothing()//
                .when(fs)
                .publish(anyListOf(Fact.class));

        // retry(5) wraps the factcast instance
        FactCast uut = FactCast.from(fs).retry(5).retry(5);

        // act
        uut.publish(Fact.builder().build("{}"));
        Mockito.verify(fs, times(3)).publish(anyListOf(Fact.class));
    }
}
