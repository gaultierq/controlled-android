package io.gaultier.controlledandroid.tools;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.parceler.Parcels;

import io.gaultier.controlledandroid.control.AbstractController;
import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

/**
 * Simple interface for ImagePickerImpl accepting only 1 client
 */

public abstract class FragmentImagePickerImpl extends ImagePickerImpl {

    public static final int UNIQ_CLIENT_ID = 33;
    protected final AbstractController parent;
    protected final int container;
    protected ImagePickerClientParcelable imageClient;

    public FragmentImagePickerImpl(AbstractController parent, final int container) {
        this.parent = parent;
        this.container = container;
    }

    @Override
    public final Callback makeCallback(final int client) {
        switch (client) {
            case UNIQ_CLIENT_ID:
                return new ImagePickerImpl.Callback() {
                    @Override
                    public void onImageFound(Uri imageUri) {
                        onImage(imageUri);
                    }

                    @Override
                    public void onError() {
                        Log.e("FragmentImagePickerImpl", "ImageLoading failed for client {0}", client);
                    }
                };
        }
        Assert.thrown("Invalid client id:" + client);
        return null;
    }

    public abstract void onImage(Uri imageUri);

    public final void provideImage(Fragment fragment) {
        Assert.ensure(imageClient == null, "This picker is already waiting for a result:"+ Parcels.wrap(imageClient));
        imageClient = new ImagePickerClientParcelable();
        imageClient.setId(UNIQ_CLIENT_ID);
        provideImage(fragment, imageClient);
    }

    public void onRequestPermissionsResult(Fragment fragment, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        onRequestPermissionsResult(fragment, requestCode, permissions, grantResults, this.imageClient);
    }
}
