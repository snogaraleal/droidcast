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

package io.streamics.droidcast;

import io.streamics.droidcast.core.decoder.Meta;
import io.streamics.droidcast.service.StreamServiceClient;
import io.streamics.droidcast.service.StreamServiceClientException;
import io.streamics.droidcast.service.StreamServiceClient.ServiceEventHandler;
import io.streamics.droidcast.service.StreamServiceClient.StreamEventHandler;

import android.app.Activity;
import android.os.Bundle;

/**
 * Android <code>Activity</code> with streaming service binding and events
 * methods.
 */
public class StreamActivity extends Activity
    implements ServiceEventHandler, StreamEventHandler {

    private StreamServiceClient client;

    /**
     * Get service client.
     * @return <code>StreamServiceClient</code>
     */
    public StreamServiceClient getStreamClient() {
        return this.client;
    }

    /**
     * Create <code>Activity</code> and bind <code>StreamServiceClient</code>.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = new StreamServiceClient(this);
        client.addServiceEventHandler(this);
        client.addStreamEventHandler(this);
        client.bind();
    }

    /**
     * Unregister stream client when the activity becomes invisible.
     */
    @Override
    public void onPause() {
        try {
            client.unregister();
        } catch (StreamServiceClientException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    /**
     * Register stream client when the activity becomes visible.
     */
    @Override
    public void onResume() {
        try {
            client.register();
        } catch (StreamServiceClientException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    /**
     * Unbind stream client when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        client.unbind();
        super.onDestroy();
    }

    /*
     * ServiceEventHandler implementation.
     */

    @Override
    public void onServiceConnect() {
        try {
            // Register client to receive messages from service
            client.register();

            // Request current stream information
            client.requestInfo();
            client.requestMeta();

        } catch (StreamServiceClientException e) {
            e.printStackTrace();
        }

        // To be overridden by activity
        // Always call super.onServiceConnect() in the overridden method
    }

    @Override
    public void onServiceDisconnect() {
        // To be overridden by activity
    }

    /*
     * StreamEventHandler implementation.
     */

    @Override
    public void onStreamInfoReceived(
            String url, String contentType,
            String name, String genre) {

        // To be overridden by activity
    }

    @Override
    public void onStreamMetaReceived(Meta meta) {
        // To be overridden by activity
    }

    @Override
    public void onStreamError() {
        // To be overridden by activity
    }

    @Override
    public void onStreamStart() {
        // To be overridden by activity
    }

    @Override
    public void onStreamStop() {
        // To be overridden by activity
    }
}
