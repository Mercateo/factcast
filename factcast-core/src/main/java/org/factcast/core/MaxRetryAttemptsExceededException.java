package org.factcast.core;

public class MaxRetryAttemptsExceededException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MaxRetryAttemptsExceededException(String string) {
        super(string);
    }

}
