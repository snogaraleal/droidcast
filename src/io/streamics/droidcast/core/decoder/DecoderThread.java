/*
 * Droidcast
 *
 * Copyright 2015 Sebastian Nogara <snogaraleal@gmail.com>
 *
 * This file is part of Droidcast.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

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
