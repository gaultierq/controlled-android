package io.gaultier.controlledandroid.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.gaultier.controlledandroid.util.Log;

/**
 * Created by q on 01/11/16.
 * launch activity and return with an image uri
 */
public abstract class ImagePickerImpl {

    protected static final int REQUEST_TAKE_PHOTO = 100;
    protected static final int REQUEST_GALLERY_PHOTO = 200;

    public abstract void provideImage(final Fragment frag, final ImagePickerClient client);

    protected void executeBasedOnCode(Fragment frag, int code, ImagePickerClient client) {
        switch (code) {
            case REQUEST_GALLERY_PHOTO: {
                if (ContextCompat.checkSelfPermission(frag.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    frag.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PHOTO);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    frag.startActivityForResult(intent, REQUEST_GALLERY_PHOTO);
                }
                break;
            }
            case REQUEST_TAKE_PHOTO: {

                // Ensure that there's a camera frag to handle the intent

                // Create the File where the photo should go
                File mTempFile;
                try {
                    mTempFile = createImageFile(frag.getContext());
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    onCantCreateFile(frag.getContext());
                    return;
                }
                //saving the uri where the picture will be stored
                client.setUri(Uri.fromFile(mTempFile));

                if (ContextCompat.checkSelfPermission(frag.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    frag.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_TAKE_PHOTO);
                } else {
                    launchTakePicture(frag, mTempFile);
                }
                break;
            }
        }
    }

    protected void onCantCreateFile(Context context) {
        Log.e("ImagePickerImpl", "Cannot create file");
    }

    //default behaviour
    protected void launchTakePicture(Fragment frag, File mTempFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (canIntentBeHandled(frag, intent)) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
            frag.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
    }

    public void onActivityResult(ImagePickerClient client, int requestCode, int resultCode, Intent data) {
        boolean ok = false;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY_PHOTO:
                    client.setUri(data.getData());
                    ok = true;
                    break;
                case REQUEST_TAKE_PHOTO:
                    ok = true;
                    break;
            }
        }

        onResult(client, ok);

    }

    public void onResult(ImagePickerClient client, boolean ok) {
        Callback callback = makeCallback(client.getId());
        if (ok) {
            callback.onImageFound(client.getUri());
        }
        else {
            callback.onError();
        }
    }

    public void onRequestPermissionsResult(Fragment fragment, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, ImagePickerClient client) {
        if (permissions.length > 0) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeBasedOnCode(fragment, requestCode, client);
                }
            } else if (permissions[0].equals(Manifest.permission.CAMERA)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeBasedOnCode(fragment, requestCode, client);
                }
            }
        }
    }


    private static boolean canIntentBeHandled(Fragment frag, Intent takePictureIntent) {
        return takePictureIntent.resolveActivity(frag.getActivity().getPackageManager()) != null;
    }

    private static File createImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir("pictures");
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public abstract Callback makeCallback(int client);


    interface Callback {
        void onImageFound(Uri imageUri);

        void onError();
    }

}
