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

package io.streamics.droidcast.core.source;

import java.io.IOException;
import java.net.MalformedURLException;

import android.os.AsyncTask;

/**
 * {@code Initiator} in charge of creating a {@code Source} from a
 * specific stream URL.
 */
public class Initiator extends AsyncTask<Void, Void, Source> {
    /**
     * {@code Handler} providing the created {@code Source}.
     */
    public static interface Handler {
        /**
         * Handle newly created {@code Source}.
         *
         * @param source Stream source or {@code null} if there was an error
         */
        public void onReady(Source source);

        /**
         * Handle error during stream initialization.
         */
        public void onError();
    }

    private String url;
    private Handler handler;

    /**
     * Constructor for {@code Initiator}.
     *
     * @param url Source URL
     * @param handler Handler
     */
    public Initiator(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    @Override
    protected Source doInBackground(Void... params) {
        try {
            return new Source(this.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SourceException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Source source) {
        if (source == null) {
            this.handler.onError();
        } else {
            this.handler.onReady(source);
        }
    }
}