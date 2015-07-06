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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.jcraft.jorbis.Info;

public class AudioUtils {
    /**
     * Initialize an {@code AudioTrack} from Vorbis {@code Info}.
     *
     * @param info Vorbis info
     * @return Audio track
     */
    public static AudioTrack fromVorbisInfo(Info info) {
        int encoding = AudioFormat.ENCODING_PCM_16BIT;
        int rate = info.rate;

        int channel;
        if (info.channels == 2) {
            channel = AudioFormat.CHANNEL_OUT_STEREO;
        } else {
            channel = AudioFormat.CHANNEL_OUT_MONO;
        }

        int bufferSize = AudioTrack.getMinBufferSize(rate, channel, encoding);
        bufferSize *= 8;

        return new AudioTrack(AudioManager.STREAM_MUSIC,
                              rate, channel, encoding, bufferSize,
                              AudioTrack.MODE_STREAM);
    }
}