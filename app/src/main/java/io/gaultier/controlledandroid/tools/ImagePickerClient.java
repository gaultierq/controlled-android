package io.gaultier.controlledandroid.tools;

import android.net.Uri;

/**
 * Created by q on 01/11/16.
 */
public interface ImagePickerClient {
    int getId();

    void setId(int id);

    Uri getUri();

    void setUri(Uri uri);
}
