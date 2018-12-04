package org.factcast.core.store;

import lombok.Getter;

public class RetryableException extends RuntimeException {

    static final long DEFAULT_WAIT_TIME_MILLIS = 10;

    private static final long serialVersionUID = 1L;

    @Getter
    private final long minimumWaitTimeMillis;

    public RetryableException(RuntimeException cause, long minimumWaitTimeMillis) {
        super(cause);
        this.minimumWaitTimeMillis = minimumWaitTimeMillis;
    }

    public RetryableException(RuntimeException cause) {
        this(cause, DEFAULT_WAIT_TIME_MILLIS);

    }

}
