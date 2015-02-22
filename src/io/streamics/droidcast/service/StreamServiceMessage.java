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

/**
 * Constants for service messages
 */
public class StreamServiceMessage {
    /**
     * Type
     */
    public static class Type {
        public static final int COMMAND = 0;
        public static final int META = 1;
        public static final int STATUS = 2;
        public static final int INFO = 3;
    }

    /**
     * Parameter
     */
    public static class Parameter {
        public static final String COMMAND = "command";
        public static final String URL = "url";
    }

    /**
     * Command
     */
    public static class Command {
        public static final int REGISTER = 0;
        public static final int UNREGISTER = 1;

        public static final int START = 2;
        public static final int STOP = 3;

        public static final int REQUEST_INFO = 4;
        public static final int REQUEST_META = 5;
    }

    /**
     * Response
     */
    public static class Response {
        public static final String VALUE = "value";

        public static final int STATUS_STARTED = 0;
        public static final int STATUS_STOPPED = 1;
        public static final int STATUS_ERROR = 2;

        public static final String URL = "url";
        public static final String CONTENT_TYPE = "content_type";
        public static final String NAME = "name";
        public static final String GENRE = "genre";
    }
}
