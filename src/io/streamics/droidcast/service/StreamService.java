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

package io.streamics.droidcast.service;

import io.streamics.droidcast.AudioUtils;
import io.streamics.droidcast.core.decoder.Decoder;
import io.streamics.droidcast.core.decoder.DecoderThread;
import io.streamics.droidcast.core.decoder.Meta;
import io.streamics.droidcast.core.source.Initiator;
import io.streamics.droidcast.core.source.Source;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.jcraft.jorbis.Info;

/**
 * Service that runs a <code>StreamThread</code> with an <code>AudioTrack</code>
 * and publishes meta data to all registered clients.
 */
public class StreamService extends Service {
    /**
     * Incoming message handler.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            Bundle data = message.getData();

            if (message.what == StreamServiceMessage.Type.COMMAND) {
                int command = data.getInt(
                        StreamServiceMessage.Parameter.COMMAND);

                switch (command) {
                case StreamServiceMessage.Command.REGISTER:
                    register(message.replyTo);
                    break;

                case StreamServiceMessage.Command.UNREGISTER:
                    unregister(message.replyTo);
                    break;

                case StreamServiceMessage.Command.START:
                    start(data.getString(StreamServiceMessage.Parameter.URL));
                    break;

                case StreamServiceMessage.Command.STOP:
                    stop();
                    break;

                case StreamServiceMessage.Command.REQUEST_INFO:
                    requestInfo();
                    break;

                case StreamServiceMessage.Command.REQUEST_META:
                    requestMeta();
                    break;
                }
            }
        }
    }

    private AudioTrack audio;
    private Decoder.Consumer consumer;
    private DecoderThread thread;

    private Meta currentMeta = null;
    private Source currentSource = null;

    private Messenger incoming = new Messenger(new IncomingHandler());
    private List<Messenger> outgoing = new ArrayList<Messenger>();

    /**
     * Constructor for <code>StreamService</code>.
     */
    public StreamService() {
        /*
         * Stream consumer
         */
        this.consumer = new Decoder.Consumer() {
            @Override
            public void onRead(byte[] data, int off, int len) {
                if (audio != null &&
                    audio.getState() == AudioTrack.STATE_INITIALIZED) {
                    audio.write(data, off, len);
                }
            }

            @Override
            public void onMeta(Meta meta) {
                currentMeta = meta;

                Message message = Message.obtain(
                        null, StreamServiceMessage.Type.META);
                message.getData().putParcelable(
                        StreamServiceMessage.Response.VALUE, meta);
                broadcast(message);
            }

            @Override
            public void onInfo(Info info) {
                audio = AudioUtils.fromVorbisInfo(info);
                audio.play();

                Message message = Message.obtain(
                        null, StreamServiceMessage.Type.STATUS);
                message.getData().putInt(
                        StreamServiceMessage.Response.VALUE,
                        StreamServiceMessage.Response.STATUS_STARTED);
                broadcast(message);
            }

            @Override
            public void onFinish() {
                if (audio != null &&
                    audio.getState() == AudioTrack.STATE_INITIALIZED) {
                    audio.release();
                }

                currentMeta = null;

                Message message = Message.obtain(
                        null, StreamServiceMessage.Type.STATUS);
                message.getData().putInt(
                        StreamServiceMessage.Response.VALUE,
                        StreamServiceMessage.Response.STATUS_STOPPED);
                broadcast(message);
            }
        };
    }

    /**
     * Bind service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return incoming.getBinder();
    }

    /**
     * Start service.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /**
     * Broadcast message to all clients.
     * @param message Message
     */
    private void broadcast(Message message) {
        for (Messenger messenger : outgoing) {
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Register client.
     *
     * This allows the service to broadcast info, meta and status messages to
     * the specified client.
     *
     * @param messenger Client
     */
    private void register(Messenger messenger) {
        this.outgoing.add(messenger);
    }

    /**
     * Unregister client.
     *
     * This prevents the client from receiving info, meta and status messages
     * broadcasted by the service.
     *
     * @param messenger Client
     */
    private void unregister(Messenger messenger) {
        this.outgoing.remove(messenger);
    }

    /**
     * Start streaming the specified URL.
     * @param url Source URL
     */
    private void start(String url) {
        new Initiator(url, new Initiator.Handler() {
            @Override
            public void onReady(Source source) {
                if (thread != null) {
                    thread.stopDecoder();
                }

                thread = new DecoderThread(source.getStream());
                thread.getDecoder().addConsumer(consumer);
                thread.start();

                currentSource = source;

                Message message = Message.obtain(
                    null, StreamServiceMessage.Type.INFO);

                Bundle data = message.getData();
                data.putString(
                    StreamServiceMessage.Response.URL,
                    source.getUrl());
                data.putString(
                    StreamServiceMessage.Response.CONTENT_TYPE,
                    source.getContentType());
                data.putString(
                    StreamServiceMessage.Response.NAME,
                    source.getName());
                data.putString(
                    StreamServiceMessage.Response.GENRE,
                    source.getGenre());
                broadcast(message);
            }

            @Override
            public void onError() {
                Message message = Message.obtain(
                    null, StreamServiceMessage.Type.STATUS);
                message.getData().putInt(
                    StreamServiceMessage.Response.VALUE,
                    StreamServiceMessage.Response.STATUS_ERROR);
                broadcast(message);

                currentSource = null;
            }
        }).execute();
    }

    /**
     * Stop underlying decoder.
     */
    private void stop() {
        if (thread != null) {
            thread.stopDecoder();
        }
    }

    /**
     * Request stream meta data.
     */
    private void requestMeta() {
        Message message = Message.obtain(
                null, StreamServiceMessage.Type.META);
        message.getData().putParcelable(
                StreamServiceMessage.Response.VALUE, currentMeta);
        broadcast(message);
    }

    /**
     * Request stream general information.
     */
    private void requestInfo() {
        Message message = Message.obtain(
                null, StreamServiceMessage.Type.INFO);
        Bundle data = message.getData();

        String url = null;
        String contentType = null;
        String name = null;
        String genre = null;

        if (currentSource != null) {
            url = currentSource.getUrl();
            contentType = currentSource.getContentType();
            name = currentSource.getName();
            genre = currentSource.getGenre();
        }

        data.putString(StreamServiceMessage.Response.URL, url);
        data.putString(StreamServiceMessage.Response.CONTENT_TYPE, contentType);
        data.putString(StreamServiceMessage.Response.NAME, name);
        data.putString(StreamServiceMessage.Response.GENRE, genre);

        broadcast(message);
    }
}