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

import io.streamics.droidcast.core.decoder.Meta;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;

/**
 * Communication interface with a {@code StreamService}.
 */
public class StreamServiceClient {
    /**
     * Service event handler.
     */
    public static interface ServiceEventHandler {
        public void onServiceConnect();
        public void onServiceDisconnect();
    }

    /**
     * Stream event handler.
     */
    public static interface StreamEventHandler {
        public void onStreamInfoReceived(
                String url, String contentType,
                String name, String genre);
        public void onStreamMetaReceived(Meta meta);
        public void onStreamError();
        public void onStreamStart();
        public void onStreamStop();
    }

    /**
     * Incoming message handler.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            Bundle data = message.getData();
            data.setClassLoader(Meta.class.getClassLoader());

            switch (message.what) {

            /*
             * Meta change
             */
            case StreamServiceMessage.Type.META:
                Parcelable meta = data.getParcelable(
                        StreamServiceMessage.Response.VALUE);
                for (StreamEventHandler handler : streamEventHandlers) {
                    handler.onStreamMetaReceived((Meta) meta);
                }
                break;

            /*
             * Status change
             */
            case StreamServiceMessage.Type.STATUS:
                int status = data.getInt(StreamServiceMessage.Response.VALUE);

                switch(status) {
                case StreamServiceMessage.Response.STATUS_ERROR:
                    for (StreamEventHandler handler : streamEventHandlers) {
                        handler.onStreamError();
                    }
                    break;

                case StreamServiceMessage.Response.STATUS_STARTED:
                    for (StreamEventHandler handler : streamEventHandlers) {
                        handler.onStreamStart();
                    }
                    break;

                case StreamServiceMessage.Response.STATUS_STOPPED:
                    for (StreamEventHandler handler : streamEventHandlers) {
                        handler.onStreamStop();
                    }
                    break;
                }

                break;

            /*
             * Info
             */
            case StreamServiceMessage.Type.INFO:
                String url = data.getString(
                        StreamServiceMessage.Response.URL);
                String contentType = data.getString(
                        StreamServiceMessage.Response.CONTENT_TYPE);
                String name = data.getString(
                        StreamServiceMessage.Response.NAME);
                String genre = data.getString(
                        StreamServiceMessage.Response.GENRE);

                for (StreamEventHandler handler : streamEventHandlers) {
                    handler.onStreamInfoReceived(url, contentType, name, genre);
                }
                break;
            }
        }
    }

    private Context context;
    private Class<?> serviceClass;

    private List<ServiceEventHandler> serviceEventHandlers =
            new ArrayList<ServiceEventHandler>();
    private List<StreamEventHandler> streamEventHandlers =
            new ArrayList<StreamEventHandler>();

    private Messenger incoming = new Messenger(new IncomingHandler());
    private Messenger outgoing = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            outgoing = new Messenger(service);

            for (ServiceEventHandler handler : serviceEventHandlers) {
                handler.onServiceConnect();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            outgoing = null;

            for (ServiceEventHandler handler : serviceEventHandlers) {
                handler.onServiceDisconnect();
            }
        }
    };

    /**
     * Constructor for {@code StreamServiceClient}.
     */
    public StreamServiceClient(Context context, Class<?> serviceClass) {
        this.context = context;
        this.serviceClass = serviceClass;
    }

    /**
     * Constructor for {@code StreamServiceClient} with the
     * default service {@code Class}.
     */
    public StreamServiceClient(Context context) {
        this.context = context;
        this.serviceClass = StreamService.class;
    }

    /**
     * Add service event handler.
     * @param handler Service event handler
     */
    public void addServiceEventHandler(ServiceEventHandler handler) {
        this.serviceEventHandlers.add(handler);
    }

    /**
     * Remove service event handler.
     * @param handler Service event handler
     */
    public void removeServiceEventHandler(ServiceEventHandler handler) {
        this.serviceEventHandlers.remove(handler);
    }

    /**
     * Add stream event handler.
     * @param handler Stream event handler
     */
    public void addStreamEventHandler(StreamEventHandler handler) {
        this.streamEventHandlers.add(handler);
    }

    /**
     * Remove stream event handler.
     * @param handler Stream event handler
     */
    public void removeStreamEventHandler(StreamEventHandler handler) {
        this.streamEventHandlers.remove(handler);
    }

    /**
     * Bind service.
     */
    public void bind() {
        Intent intent = new Intent(this.context, this.serviceClass);
        this.context.startService(intent);
        this.context.bindService(
                intent, this.connection, Context.BIND_ABOVE_CLIENT);
    }

    /**
     * Unbind service.
     */
    public void unbind() {
        this.context.unbindService(this.connection);
    }

    /**
     * Ensure that there is a service connection.
     * @throws StreamServiceClientException 
     */
    private void ensureConnected() throws StreamServiceClientException {
        if (this.outgoing == null) {
            throw new StreamServiceClientException("Service disconnected");
        }
    }

    /**
     * Get whether the service client is connected.
     * @return Whether the client is connected
     */
    public boolean isConnected() {
        return this.outgoing != null;
    }

    /**
     * Register client.
     * @throws StreamServiceClientException 
     */
    public void register() throws StreamServiceClientException {
        this.ensureConnected();

        Message message = Message.obtain(
                null, StreamServiceMessage.Type.COMMAND);
        message.getData().putInt(StreamServiceMessage.Parameter.COMMAND,
                                 StreamServiceMessage.Command.REGISTER);
        message.replyTo = incoming;

        try {
            this.outgoing.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new StreamServiceClientException(e.getMessage());
        }
    }

    /**
     * Unregister client.
     * @throws StreamServiceClientException 
     */
    public void unregister() throws StreamServiceClientException {
        this.ensureConnected();

        Message message = Message.obtain(
                null, StreamServiceMessage.Type.COMMAND);
        message.getData().putInt(StreamServiceMessage.Parameter.COMMAND,
                                 StreamServiceMessage.Command.UNREGISTER);
        message.replyTo = incoming;

        try {
            this.outgoing.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new StreamServiceClientException(e.getMessage());
        }
    }

    /**
     * Start stream.
     * @param url Source URL
     * @throws StreamServiceClientException 
     */
    public void start(String url) throws StreamServiceClientException {
        this.ensureConnected();

        Message message = Message.obtain(
                null, StreamServiceMessage.Type.COMMAND);
        Bundle data = message.getData();
        data.putInt(StreamServiceMessage.Parameter.COMMAND,
                    StreamServiceMessage.Command.START);
        data.putString(StreamServiceMessage.Parameter.URL, url);

        try {
            this.outgoing.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new StreamServiceClientException(e.getMessage());
        }
    }

    /**
     * Stop stream.
     * @throws StreamServiceClientException 
     */
    public void stop() throws StreamServiceClientException {
        this.ensureConnected();

        Message message = Message.obtain(
                null, StreamServiceMessage.Type.COMMAND);
        message.getData().putInt(StreamServiceMessage.Parameter.COMMAND,
                                 StreamServiceMessage.Command.STOP);

        try {
            this.outgoing.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new StreamServiceClientException(e.getMessage());
        }
    }

    /**
     * Request stream general information.
     * @throws StreamServiceClientException
     */
    public void requestInfo() throws StreamServiceClientException {
        this.ensureConnected();

        Message message = Message.obtain(
                null, StreamServiceMessage.Type.COMMAND);
        message.getData().putInt(StreamServiceMessage.Parameter.COMMAND,
                                 StreamServiceMessage.Command.REQUEST_INFO);

        try {
            this.outgoing.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new StreamServiceClientException(e.getMessage());
        }
    }

    /**
     * Request stream meta data.
     * @throws StreamServiceClientException
     */
    public void requestMeta() throws StreamServiceClientException {
        this.ensureConnected();

        Message message = Message.obtain(
                null, StreamServiceMessage.Type.COMMAND);
        message.getData().putInt(StreamServiceMessage.Parameter.COMMAND,
                                 StreamServiceMessage.Command.REQUEST_META);

        try {
            this.outgoing.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new StreamServiceClientException(e.getMessage());
        }
    }
}
