package io.streamics.droidcast.service;

/**
 * Stream service client exception.
 */
@SuppressWarnings("serial")
public class StreamServiceClientException extends Exception {
    public StreamServiceClientException(String message) {
        super(message);
    }

    public StreamServiceClientException(
            String message, Throwable throwable) {
        super(message, throwable);
    }
}
