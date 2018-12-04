package org.factcast.core;

import org.factcast.core.store.RetryableException;

public interface RetryingFactCastTask<T> {

    T call() throws RetryableException;

}
