package org.factcast.core;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

import org.factcast.core.store.RetryableException;
import org.factcast.core.subscription.Subscription;
import org.factcast.core.subscription.SubscriptionRequest;
import org.factcast.core.subscription.observer.FactObserver;
import org.factcast.core.subscription.observer.IdObserver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RetryingFactCast implements FactCast {

    private final FactCast delegate;

    private final int maxRetries;

    private <T> T runWithRetries(String description, RetryingFactCastTask<T> task) {
        int retryAttempt = 1;
        do {
            try {
                return task.call();

            } catch (RetryableException e) {
                log.warn("{} failed, ", description, e.getCause());
                if (retryAttempt <= maxRetries) {
                    try {
                        Thread.sleep(e.minimumWaitTimeMillis());
                    } catch (InterruptedException ignore) {
                        //
                    }
                    log.warn("Retrying {} attempt {}/{}", description, retryAttempt, maxRetries);
                }
            }
        } while (retryAttempt++ < maxRetries);
        throw new MaxRetryAttemptsExceededException("Exceeded max retry attempts of '" + description
                + "', giving up.");

    }

    public void publish(@NonNull List<? extends Fact> factsToPublish) {
        runWithRetries("publish", () -> {
            delegate.publish(factsToPublish);
            return null;
        });

    }

    public Subscription subscribeToFacts(@NonNull SubscriptionRequest request,
            @NonNull FactObserver observer) {
        return runWithRetries("subscribeToFacts", () -> delegate.subscribeToFacts(request,
                observer));
    }

    public void publish(@NonNull Fact factToPublish) {
        runWithRetries("publish", () -> {
            delegate.publish(factToPublish);
            return null;
        });
    }

    public Subscription subscribeToIds(@NonNull SubscriptionRequest request,
            @NonNull IdObserver observer) {
        return runWithRetries("subscribeToIds", () -> delegate.subscribeToIds(request, observer));
    }

    public UUID publishWithMark(@NonNull Fact factToPublish) {
        return delegate.publishWithMark(factToPublish);
    }

    public Optional<Fact> fetchById(@NonNull UUID id) {
        return delegate.fetchById(id);
    }

    public OptionalLong serialOf(@NonNull UUID id) {
        return delegate.serialOf(id);
    }

    public Set<String> enumerateNamespaces() {
        return delegate.enumerateNamespaces();
    }

    public UUID publishWithMark(@NonNull List<Fact> factsToPublish) {
        return delegate.publishWithMark(factsToPublish);
    }

    public Set<String> enumerateTypes(@NonNull String ns) {
        return delegate.enumerateTypes(ns);
    }

}
