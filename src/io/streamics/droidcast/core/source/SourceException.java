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
