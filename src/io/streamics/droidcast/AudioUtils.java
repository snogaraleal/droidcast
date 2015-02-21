package io.streamics.droidcast;

import com.jcraft.jorbis.Info;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioUtils {
    /**
     * Initialize an <code>AudioTrack</code> from Vorbis <code>Info</code>.
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
