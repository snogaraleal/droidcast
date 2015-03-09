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

package io.streamics.droidcast.core.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

/**
 * OGG Vorbis decoder using JOrbis
 * See: http://www.jcraft.com/jorbis/
 * 
 * 1. Create a {@code Decoder} specifying an {@code InputStream}.
 * 2. Use {@code addConsumer} to add stream consumers.
 * 3. Call {@code start} to start the decoder.
 * 4. Call {@code stop} to stop the decoder.
 * 
 * The decoding loop is based on the JOrbis example
 * http://www.jcraft.com/jorbis/tutorial/ExamplePlayer.java
 */
public class Decoder {
    /**
     * Stream decoder interface.
     */
    public static interface Consumer {
        /**
         * Handle Vorbis info.
         */
        void onInfo(Info info);

        /**
         * Handle meta data.
         * @param meta Meta data
         */
        void onMeta(Meta meta);

        /**
         * Read PCM audio.
         * @param data
         * @param off
         * @param len
         */
        void onRead(byte data[], int off, int len);

        /**
         * Finish decoding.
         */
        void onFinish();
    }

    private InputStream stream;
    private List<Consumer> consumers = new ArrayList<Consumer>();
    private boolean working;

    // OGG
    SyncState ogg_sync_state;
    StreamState ogg_stream_state;
    Page ogg_page;
    Packet ogg_packet;

    // Vorbis
    Info vorbis_info;
    Comment vorbis_comment;
    DspState vorbis_dsp_state;
    Block vorbis_block;

    public static final int BUFFER_SIZE = 2048;

    private static int MAX_16BIT = 32767;
    private static int MIN_16BIT = -32768;

    /**
     * Constructor for feeding a {@code StreamDecoder} with an
     * {@code InputStream}.
     * @param stream Stream
     */
    public Decoder(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Add stream consumer.
     * @param consumer Consumer
     */
    public void addConsumer(Consumer consumer) {
        this.consumers.add(consumer);
    }

    /**
     * Initialize states.
     */
    private void init() {
        ogg_sync_state = new SyncState();
        ogg_stream_state = new StreamState();
        ogg_page = new Page();
        ogg_packet = new Packet();

        vorbis_info = new Info();
        vorbis_comment = new Comment();
        vorbis_dsp_state = new DspState();
        vorbis_block = new Block(vorbis_dsp_state);

        ogg_sync_state.init();
    }

    /**
     * Clear states.
     */
    private void clear() {
        ogg_stream_state.clear();
        vorbis_block.clear();
        vorbis_dsp_state.clear();
        vorbis_info.clear();
    }

    /**
     * Decoder loop.
     */
    private void work() {
        boolean chained = false;

        worker: while (this.working) {
            if (stream == null) {
                break;
            }

            /*
             * Begin decoding
             */

            int bytes = 0;
            int index = ogg_sync_state.buffer(BUFFER_SIZE);
            byte buffer[] = ogg_sync_state.data;

            try {
                bytes = stream.read(buffer, index, BUFFER_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            ogg_sync_state.wrote(bytes);

            if (chained) {
                chained = false;
            } else {
                if (ogg_sync_state.pageout(ogg_page) != 1) {
                    if (bytes < BUFFER_SIZE) {
                        break;
                    }
                }
            }

            ogg_stream_state.init(ogg_page.serialno());
            ogg_stream_state.reset();

            vorbis_info.init();
            vorbis_comment.init();

            if (ogg_stream_state.pagein(ogg_page) < 0) {
                break;
            }

            if (ogg_stream_state.packetout(ogg_packet) !=1 ) {
                break;
            }

            if (vorbis_info.synthesis_headerin(
                    vorbis_comment, ogg_packet) < 0) {
                break;
            }

            for (Consumer consumer : this.consumers) {
                consumer.onInfo(vorbis_info);
            }

            int i = 0;

            while (i < 2) {
                while (i < 2) {
                    int result = ogg_sync_state.pageout(ogg_page);

                    if (result == 0) {
                        break;
                    } else if (result == 1) {
                        ogg_stream_state.pagein(ogg_page);

                        while (i < 2) {
                            result = ogg_stream_state.packetout(ogg_packet);

                            if (result == 0) {
                                break;
                            } else if (result == -1) {
                                break worker;
                            }

                            vorbis_info.synthesis_headerin(
                                    vorbis_comment, ogg_packet);

                            i++;
                        }
                    }
                }

                index = ogg_sync_state.buffer(BUFFER_SIZE);
                buffer = ogg_sync_state.data;

                try {
                    bytes = stream.read(buffer, index, BUFFER_SIZE);
                } catch (IOException e) {
                    e.printStackTrace();
                    break worker;
                }

                if (bytes == 0 && i < 2) {
                    break worker;
                }

                ogg_sync_state.wrote(bytes);
            }

            Meta meta = new Meta(vorbis_comment.user_comments);
            for (Consumer consumer : this.consumers) {
                consumer.onMeta(meta);
            }

            int conv_size = BUFFER_SIZE * vorbis_info.channels;
            byte conv_buffer[] = new byte[conv_size];

            vorbis_dsp_state.synthesis_init(vorbis_info);
            vorbis_block.init(vorbis_dsp_state);

            float[][][] _pcmf = new float[1][][];
            int[] _index = new int[vorbis_info.channels];

            int eos = 0;
            while (eos == 0) {
                while (eos == 0) {
                    int result = ogg_sync_state.pageout(ogg_page);

                    if (result == 0) {
                        break;
                    } else if (result == -1) {
                    } else {
                        ogg_stream_state.pagein(ogg_page);

                        if (ogg_page.granulepos() == 0) {
                            chained = true;
                            eos = 1;
                            break;
                        }

                        while (true) {
                            result = ogg_stream_state.packetout(ogg_packet);

                            if (result == 0) {
                                break;
                            } else if (result == -1) {
                            } else {
                                int samples;

                                if (vorbis_block.synthesis(ogg_packet) == 0) {
                                    vorbis_dsp_state.synthesis_blockin(
                                            vorbis_block);
                                }

                                while (true) {
                                    samples = vorbis_dsp_state.synthesis_pcmout(
                                            _pcmf, _index);
                                    if (!(samples > 0)) {
                                        break;
                                    }

                                    float[][] pcmf = _pcmf[0];
                                    int bytes_out;
                                    if (samples < conv_size) {
                                        bytes_out = samples;
                                    } else {
                                        bytes_out = conv_size;
                                    }

                                    // Convert doubles to 16 bit signed integers
                                    // (host order) and interleave
                                    for (i = 0; i < vorbis_info.channels; i++) {
                                        int mono = _index[i];
                                        int ptr = i * 2;
                                        for (int n = 0; n < bytes_out; n++) {
                                            int val = (int)(pcmf[i][mono + n] *
                                                            32767.);

                                            if (val > MAX_16BIT) {
                                                val = MAX_16BIT;
                                            }

                                            if (val < MIN_16BIT) {
                                                val = MIN_16BIT;
                                            }

                                            if (val < 0) {
                                                val = val | 0x8000;
                                            }

                                            conv_buffer[ptr] = (byte)(val);
                                            conv_buffer[ptr + 1] = (byte)
                                                                   (val >>> 8);
                                            ptr += 2 * (vorbis_info.channels);
                                        }
                                    }

                                    // Write to consumer
                                    for (Consumer consumer : this.consumers) {
                                        consumer.onRead(conv_buffer, 0,
                                            2 * vorbis_info.channels *
                                            bytes_out);
                                    }

                                    // Finish
                                    vorbis_dsp_state.synthesis_read(bytes_out);
                                }
                            }
                        }

                        if (ogg_page.eos() != 0) {
                            eos = 1;
                        }
                    }
                }

                if (eos == 0) {
                    index = ogg_sync_state.buffer(BUFFER_SIZE);
                    buffer = ogg_sync_state.data;

                    try {
                        bytes = stream.read(buffer, index, BUFFER_SIZE);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break worker;
                    }

                    if (bytes == -1) {
                        break;
                    }

                    ogg_sync_state.wrote(bytes);

                    if (bytes == 0) {
                        eos = 1;
                    }
                }
            }

            clear();

            /*
             * End decoding
             */
        }
    }

    /**
     * Run decoder.
     */
    public void start() {
        // Initialize
        this.working = true;
        this.init();

        // Start decoder loop
        try {
            this.work();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Finish decoder execution.
     */
    public void stop() {
        if (this.working) {
            ogg_sync_state.clear();

            // Update status
            this.working = false;

            // Callback
            for (Consumer consumer : this.consumers) {
                consumer.onFinish();
            }
        }
    }
}
