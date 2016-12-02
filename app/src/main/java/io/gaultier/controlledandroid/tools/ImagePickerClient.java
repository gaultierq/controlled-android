package io.gaultier.controlledandroid.tools;

import java.io.File;

/**
 * Created by q on 01/11/16.
 */
public interface ImagePickerClient {
    int getId();

    void setId(int id);

    File getFile();

    void setFile(File file);
}
