package io.gaultier.controlledandroid.tools;

import android.net.Uri;

import org.parceler.Parcel;


/**
 * Created by q on 01/11/16.
 */

@Parcel
public class ImagePickerClientParcelable implements ImagePickerClient {

    int id;
    Uri uri;

    ImagePickerClientParcelable() {
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
