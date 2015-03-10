/*
 * Droidcast
 *
 * Copyright 2015 Sebastian Nogara <snogaraleal@gmail.com>
 *
 * This file is part of Droidcast.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package io.streamics.droidcast.core.decoder;

import java.io.InputStream;

/**
 * {@code Decoder} wrapped in a thread.
 * 
 * 1. Create a {@code DecoderThread} specifying an {@code InputStream}.
 * 2. Get the wrapped {@code Decoder} instance with {@code getDecoder}
 *    and use {@code addConsumer} to add stream consumers.
 * 3. Call {@code start} to start the thread.
 * 4. Call {@code stopDecoder} to stop the wrapped decoder causing the
 *    thread to finish.
 */
public class DecoderThread extends Thread {
    private Decoder decoder;

    /**
     * Constructor for {@code DecoderThread} taking an {@code InputStream}
     * used to create the underlying {@code Decoder}.
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
