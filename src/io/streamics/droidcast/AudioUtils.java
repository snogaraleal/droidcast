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

package io.streamics.droidcast;

import com.jcraft.jorbis.Info;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioUtils {
    /**
     * Initialize an {@code AudioTrack} from Vorbis {@code Info}.
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
