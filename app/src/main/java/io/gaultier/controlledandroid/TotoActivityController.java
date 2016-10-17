package io.gaultier.controlledandroid;

import org.parceler.Parcel;

import io.gaultier.controlledandroid.control.AbstractActivityController;

/**
 * Created by q on 16/10/16.
 */
@Parcel
public class TotoActivityController extends AbstractActivityController {
    private boolean progress;

    public boolean isProgress() {
        return progress;
    }

    public void setProgress(boolean progress) {
        this.progress = progress;
    }
}
