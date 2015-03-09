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

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Source from which to stream.
 */
public class Source {
    /**
     * Input stream
     */
    public class Stream extends FilterInputStream {
        /**
         * Constructor for {@code Stream}.
         * @param connection Source URL connection
         * @throws IOException
         */
        public Stream(URLConnection connection) throws IOException {
            super(new BufferedInputStream(connection.getInputStream()));
        }
    }

    /**
     * Content type
     */
    public static class ContentType {
        public static String CONTENT_TYPE_OGG = "application/ogg";
    }

    /**
     * ICY values
     */
    public static class Icy {
        public static final String ICY_HEADER_GENRE = "icy-genre";
        public static final String ICY_HEADER_NAME = "icy-name";
    }

    /*
     * Communication
     */
    private URLConnection connection;
    private Stream stream;

    /*
     * Information
     */
    private String url;
    private String contentType;
    private String name;
    private String genre;

    /**
     * Constructor for creating a {@code Source} from URL.
     * @throws IOException 
     * @throws MalformedURLException 
     * @throws StreamException 
     */
    public Source(String url)
            throws MalformedURLException, IOException, SourceException {

        this.url = url;
        this.connection = new URL(url).openConnection();
        this.stream = new Stream(this.connection);
        this.contentType = this.connection.getContentType();

        if (this.contentType.equals(ContentType.CONTENT_TYPE_OGG)) {
            this.genre = connection.getHeaderField(Icy.ICY_HEADER_GENRE);
            this.name = connection.getHeaderField(Icy.ICY_HEADER_NAME);

        } else {
            throw new SourceException("Unknown content type");
        }
    }

    /**
     * Get stream
     * @return Stream
     */
    public Stream getStream() {
        return this.stream;
    }

    /**
     * Get URL
     * @return URL
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Get content type
     * @return Content type
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Get stream name
     * @return Name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get stream genre
     * @return Genre
     */
    public String getGenre() {
        return this.genre;
    }
}
