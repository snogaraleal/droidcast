package io.streamics.droidcast.core.decoder;

import java.io.InputStream;

/**
 * <code>Decoder</code> wrapped in a thread
 * 
 * 1. Create a <code>DecoderThread</code> specifying an <code>InputStream</code>
 * 2. Get the wrapped <code>Decoder</code> instance with <code>getDeocder</code>
 *    and use <code>addConsumer</code> to add stream consumers
 * 3. Call <code>start</code> to start the thread
 * 4. Call <code>stopDecoder</code> to stop the wrapped decoder causing the
 *    thread to finish
 */
public class DecoderThread extends Thread {
    private Decoder decoder;

    /**
     * Constructor for <code>DecoderThread</code> taking an
     * <code>InputStream</code> used to create the underlying
     * <code>Decoder</code>.
     *
     * @param stream Source stream
     * @param decoder Stream decoder
     */
    public DecoderThread(InputStream stream) {
        this.decoder = new Decoder(stream);
    }

    /**
     * Get wrapped decoder.
     * @return Stream decoder
     */
    public Decoder getDecoder() {
        return this.decoder;
    }

    /**
     * Run thread.
     */
    @Override
    public void run() {
        this.decoder.start();
        this.stopDecoder();
    }

    /**
     * Finish thread.
     */
    public void stopDecoder() {
        if (decoder != null) {
            decoder.stop();
        }
    }
}
