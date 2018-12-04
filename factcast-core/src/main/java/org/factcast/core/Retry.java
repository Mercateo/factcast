package org.factcast.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.factcast.core.store.RetryableException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Retry {
    private static final ClassLoader classLoader = Retry.class.getClassLoader();

    @SuppressWarnings("unchecked")
    public static <T extends ReadFactCast> T wrap(boolean readOnly, T toWrap, int retryCount) {

        Class<?> interfaceToProxy = readOnly ? ReadFactCast.class : FactCast.class;
        return (T) Proxy.newProxyInstance(classLoader, new Class[] { interfaceToProxy },
                new RetryProxyInvocationHandler(toWrap, retryCount));
    }

    @RequiredArgsConstructor
    private static class RetryProxyInvocationHandler implements InvocationHandler {
        @NonNull
        final Object delegateObject;

        final int maxRetries;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.getName().equals("retry")) {
                log.warn("Trying to double-wrap a factcast instance. Ignoring.");
                return proxy;
            }

            String description = toString(method);

            int retryAttempt = 1;
            do {
                try {
                    return method.invoke(delegateObject, args);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof RetryableException) {
                        RetryableException e = (RetryableException) cause;
                        log.warn("{} failed: ", description, e.getCause());
                        if (retryAttempt <= maxRetries) {
                            sleep(e.minimumWaitTimeMillis());
                            log.warn("Retrying attempt {}/{}", retryAttempt, maxRetries);
                        }
                    } else {
                        throw cause;
                    }
                }
            } while (retryAttempt++ < maxRetries);
            throw new MaxRetryAttemptsExceededException(
                    "Exceeded max retry attempts of '" + description + "', giving up.");
        }

        private String toString(Method method) {
            String args = Arrays.stream(method.getParameterTypes())
                    .map(t -> t.getSimpleName())
                    .collect(Collectors.joining(", "));
            return method.getDeclaringClass().getSimpleName() + "::" + method.getName() + "(" + args
                    + ")";

        }

        private void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ignore) {
                //
            }
        }

    }
}
