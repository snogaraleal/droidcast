package io.streamics.droidcast.core.decoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <code>Parcelable</code> class for stream meta data.
 */
public class Meta implements Parcelable {
    public static String SEPARATOR = "=";

    private Map<String, String> values = new HashMap<String, String>();

    /**
     * Constructor for <code>StreamMeta</code>.
     */
    public Meta() {
    }

    /**
     * Constructor for <code>StreamMeta</code> with Vorbis user comments.
     * @param comments User comments
     */
    public Meta(byte[][] comments) {
        for (int n = 0; n < comments.length; n++) {
            if (comments[n] == null) {
                break;
            }

            String[] comment = new String(
                    comments[n], 0,
                    comments[n].length - 1).split(SEPARATOR);

            values.put(comment[0], comment[1]);
        }
    }

    /**
     * Read contents from parcel.
     */
    public void readFromParcel(Parcel in) {
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            String[] comment = new String[2];
            in.readStringArray(comment);
            values.put(comment[0], comment[1]);
        }
    }

    /**
     * Write contents to parcel.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.values.keySet().size());
        Iterator<Map.Entry<String, String>> iterator =
                this.values.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> pair = iterator.next();
            dest.writeStringArray(new String[] {
                pair.getKey(), pair.getValue()
            });
        }
    }

    /**
     * Get value.
     * @param key Key
     * @return Value
     */
    public String getValue(String key) {
        return this.values.get(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Meta> CREATOR =
            new Parcelable.Creator<Meta>() {
        public Meta createFromParcel(Parcel in) {
            Meta meta = new Meta();
            meta.readFromParcel(in);
            return meta;
        }

        public Meta[] newArray(int size) {
            return new Meta[size];
        }
    };
}