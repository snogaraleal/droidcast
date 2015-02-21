package io.streamics.droidcast.core.source;

/**
 * Stream source error
 */
public class SourceException extends Exception {
    private static final long serialVersionUID = -1937511577958810075L;

    public SourceException(String message) {
        super(message);
    }

    public SourceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
