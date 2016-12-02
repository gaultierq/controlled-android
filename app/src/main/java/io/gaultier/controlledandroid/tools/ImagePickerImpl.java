package io.gaultier.controlledandroid.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

    public boolean onActivityResult(ImagePickerClient client, Activity activity, int requestCode, int resultCode, Intent data) {
        Boolean ok = null;

        switch (requestCode) {
            case REQUEST_GALLERY_PHOTO:
                ok = resultCode == Activity.RESULT_OK;
                if (ok) {
                    client.setUri(getPath(activity, data.getData()));
                }
                break;
            case REQUEST_TAKE_PHOTO:
                ok = resultCode == Activity.RESULT_OK;
                break;
        }

        if (ok != null) {
            onResult(client, ok);
            return true;
        }
        return false;
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public Uri getPath(Activity activity, Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    return Uri.fromFile(file);
                }
            }
        }
        // this is our fallback here
        return uri;
    }

    public void onResult(ImagePickerClient client, boolean ok) {
        Callback callback = makeCallback(client.getId());
        if (ok) {
            callback.onImageFound(client.getUri());
        } else {
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
