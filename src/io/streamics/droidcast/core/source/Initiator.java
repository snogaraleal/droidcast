package io.streamics.droidcast.core.source;

import java.io.IOException;
import java.net.MalformedURLException;

import android.os.AsyncTask;

/**
 * <code>Initiator</code> in charge of creating a <code>Source</code> from a
 * specific stream URL.
 */
public class Initiator extends AsyncTask<Void, Void, Source> {
    /**
     * <code>Handler</code> for providing the created <code>Source</code>.
     */
    public static interface Handler {
        /**
         * Handle newly created <code>Source</code>.
         * @param source Stream source or null if there was an error
         */
        public void onReady(Source source);

        /**
         * Handle error during stream initialization.
         */
        public void onError();
    }

    private String url;
    private Handler handler;

    /**
     * Constructor for <code>Initiator</code>.
     * @param url Source URL
     * @param handler Handler
     */
    public Initiator(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    @Override
    protected Source doInBackground(Void... params) {
        try {
            return new Source(this.url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SourceException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Source source) {
        if (source == null) {
            this.handler.onError();
        } else {
            this.handler.onReady(source);
        }
    }
}
