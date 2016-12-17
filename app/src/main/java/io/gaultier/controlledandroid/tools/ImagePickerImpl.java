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

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.gaultier.controlledandroid.control.ControlledElement;
import io.gaultier.controlledandroid.util.Log;

/**
 * Created by q on 01/11/16.
 * launch activity and return with an image uri
 */
public abstract class ImagePickerImpl {

    protected static final int REQUEST_TAKE_PHOTO = 100;
    protected static final int REQUEST_GALLERY_PHOTO = 200;
    private static final String TAG = "ImagePickerImpl";

    private File mTempFile;

    public abstract void provideImage(final Fragment frag, final ImagePickerClient client);

    protected void executeBasedOnCode(Fragment frag, int code, ImagePickerClient client) {
        // Create the File where the photo should go
        try {
            mTempFile = createImageFile(frag.getContext());
        } catch (IOException ex) {
            // Error occurred while creating the File
            onCantCreateFile(frag.getContext());
            return;
        }
        //saving the uri where the picture will be stored
        client.setFile(mTempFile);

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

    public boolean onActivityResult(ImagePickerClient client, ControlledElement activity, int requestCode, int resultCode, Intent data) {
        Boolean ok = null;

        switch (requestCode) {
            case REQUEST_GALLERY_PHOTO:
                ok = resultCode == Activity.RESULT_OK && copyToTempFile(activity.getControlledActivity(), data.getData());
//                if (ok) {
//                    client.setUri(getPath(activity, data.getData()));
//                }
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

    boolean copyToTempFile(Context context, Uri uri) {
        InputStream inputStream = null;
        boolean okk = false;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                IOUtils.copy(inputStream, new FileOutputStream(mTempFile));
                okk = true;
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, e, "unable to open stream for uri", uri);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return okk;
    }

    public void onResult(ImagePickerClient client, boolean ok) {
        Callback callback = makeCallback(client.getId());
        if (ok) {
            callback.onImageFound(client.getFile());
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
        void onImageFound(File imageFile);

        void onError();
    }

}
