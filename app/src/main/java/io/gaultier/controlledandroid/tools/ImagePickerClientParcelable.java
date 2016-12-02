package io.gaultier.controlledandroid.tools;

import org.parceler.Parcel;

import java.io.File;


/**
 * Created by q on 01/11/16.
 */

@Parcel
public class ImagePickerClientParcelable implements ImagePickerClient {

    int id;
    File file;

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
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

}
