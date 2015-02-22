# io.streamics.droidcast

**Droidcast** is an Android library implementing a service for OGG Vorbis
streaming. Although the Android media player does support OGG Vorbis, it
doesn't provide live meta data from Vorbis comments. Droidcast uses the
*pure Java OGG Vorbis decoder* [JOrbis](http://www.jcraft.com/jorbis/).

## Requirements

The JOrbis decoding library is the only requirement. You can find a
[JAR file](/lib) in this repository with its corresponding license.

## Getting the droidcast JAR

You have 2 options:

* Download and add to classpath.
    * Droidcast JAR [here](/droidcast.jar).
    * You also need the [JOrbis JAR](/lib) in your classpath.

* Clone the repository and build it yourself (since an ANT `build.xml` file
  is provided).
    * Get [Apache ANT](http://ant.apache.org/) if you haven't yet (it's
      available as a package in most Linux distributions).
    * Clone the repository.
    * Add your SDK's `android.jar` to the `lib` directory.
    * Run `ant build`.
    * You can find the JAR file in `build/jar/`.

## Using

### 1. Add libraries

Add `jogg.jar` and `droidcast.jar` to your classpath.

### 2. Communicate with the service

The streaming service is controlled with a `StreamServiceClient` which has to
be bound and registered to receive messages from the service. You can use the
`StreamActivity` base class that does all that for you or implement it
yourself by looking at the `StreamActivity.java` source code.

```java
import android.util.Log;

import io.streamics.droidcast.StreamActivity;
import io.streamics.droidcast.core.decoder.Meta;
import io.streamics.droidcast.service.StreamServiceClientException;

public class MainActivity extends StreamActivity {

    // ... Code omitted for clarity ...

    @Override
    public void onServiceConnect() {
        super.onServiceConnect();

        try {
            getStreamClient().start("http://radio.fm:8000/stream.ogg");
        } catch (StreamServiceClientException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStreamMetaReceived(Meta meta) {
        if (meta != null) {
            String title = meta.getValue("title");
            if (title != null) {
                Log.i("MainActivity", "Stream meta: " + title);
            }
        }
    }
}
```

### 3. Update manifest

Add Internet permission to your `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

Add the service to your `AndroidManifest.xml` nested to the `<application>`
element.

```xml
<service
    android:name="io.streamics.droidcast.service.StreamService"
    android:process=":stream" />
```

## License

This project is under the LGPL license.

Check out `COPYING`.

## Thanks to

[JCraft](http://www.jcraft.com/) for making
[JOrbis](http://www.jcraft.com/jorbis/).
