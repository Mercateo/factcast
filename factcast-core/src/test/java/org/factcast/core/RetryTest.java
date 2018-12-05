package org.factcast.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.factcast.core.store.FactStore;
import org.factcast.core.store.RetryableException;
import org.junit.jupiter.api.Test;

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
        FactCast uut = FactCast.from(fs).retry(5);

        // act
        uut.publish(Fact.builder().build("{}"));
        verify(fs, times(3)).publish(anyListOf(Fact.class));
        verifyNoMoreInteractions(fs);
    }

    @Test
    void testWrapsOnlyOnce() throws Exception {
        FactStore fs = mock(FactStore.class);

        FactCast uut = FactCast.from(fs).retry(3);

        assertThat(uut).isEqualToComparingFieldByField(uut.retry(5));
    }

    @Test
    void testMaxRetries() throws Exception {

        int maxRetries = 3;
        // as we literally "re"-try, we expect the original attempt plus maxRetries:
        int expectedPublishAttempts = maxRetries + 1;
        FactStore fs = mock(FactStore.class);
        doThrow(new RetryableException(new RuntimeException(""))).when(fs).publish(anyListOf(
                Fact.class));
        FactCast uut = FactCast.from(fs).retry(maxRetries);

        assertThrows(MaxRetryAttemptsExceededException.class, () -> {
            uut.publish(Fact.builder().ns("foo").build("{}"));
        });
        
        verify(fs, times(expectedPublishAttempts)).publish(anyListOf(Fact.class));
        verifyNoMoreInteractions(fs);
    }
}
