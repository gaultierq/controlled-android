package io.gaultier.controlledandroid.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

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
import io.gaultier.controlledandroid.control.ControllerUtil;
import io.gaultier.controlledandroid.util.Log;

/**
 * Created by q on 01/11/16.
 * launch activity and return with an image uri
 */
public abstract class ImagePickerImpl {

    protected static final int REQUEST_TAKE_PHOTO = 100;
    protected static final int REQUEST_GALLERY_PHOTO = 200;
    private static final String TAG = "ImagePickerImpl";


    protected File mTempFile;

    public abstract void provideImage(final ControlledElement frag, final ImagePickerClient client);

    protected void executeBasedOnCode(final ControlledElement frag, final int code, ImagePickerClient client) {
        // Create the File where the photo should go
        Context context = ControllerUtil.getContext(frag);
        try {
            mTempFile = createImageFile(context);
        } catch (IOException ex) {
            // Error occurred while creating the File
            onCantCreateFile(context);
            return;
        }
        //saving the uri where the picture will be stored
        client.setFile(mTempFile);



        ControllerUtil.exectuteWhenPermitted(frag, permission(code), new ControllerUtil.OnPermitted() {

            @Override
            public void executeAction() {

                switch (code) {
                    case REQUEST_GALLERY_PHOTO: {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        ControllerUtil.startActivityForResult(intent, frag, REQUEST_GALLERY_PHOTO);
                        break;
                    }
                    case REQUEST_TAKE_PHOTO: {
                        launchTakePicture(frag, mTempFile);
                        break;
                    }
                }

            }

        }, 0, 0);

//        switch (code) {
//            case REQUEST_GALLERY_PHOTO: {
//                if (ContextCompat.checkSelfPermission(frag.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    frag.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PHOTO);
//                } else {
//                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    frag.startActivityForResult(intent, REQUEST_GALLERY_PHOTO);
//                }
//                break;
//            }
//            case REQUEST_TAKE_PHOTO: {
//                if (ContextCompat.checkSelfPermission(frag.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    frag.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_TAKE_PHOTO);
//                } else {
//                    launchTakePicture(frag, mTempFile);
//                }
//                break;
//            }
//        }
    }

    @NonNull
    private static String permission(int code) {
        switch (code) {
            case REQUEST_GALLERY_PHOTO: {
                return Manifest.permission.READ_EXTERNAL_STORAGE;
            }
            case REQUEST_TAKE_PHOTO: {
                return Manifest.permission.CAMERA;
            }
        }
        throw new IllegalStateException();

    }

    protected void onCantCreateFile(Context context) {
        Log.e("ImagePickerImpl", "Cannot create file");
    }

    //default behaviour
    protected void launchTakePicture(ControlledElement element, File mTempFile) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (canIntentBeHandled(element, intent)) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
            ControllerUtil.startActivityForResult(intent, element, REQUEST_TAKE_PHOTO);
        }
    }

    public boolean onActivityResult(ImagePickerClient client, ControlledElement activity, int requestCode, int resultCode, Intent data) {
        Boolean ok = resolveFile(activity, requestCode, resultCode, data);

        if (ok != null) {
            onResult(client, ok);
            return true;
        }
        return false;
    }

    //ensure the file has the data in it
    protected Boolean resolveFile(ControlledElement activity, int requestCode, int resultCode, Intent data) {
        Boolean ok = null;

        switch (requestCode) {
            case REQUEST_GALLERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    ok = copyToTempFile(activity.getControlledActivity(), data.getData(), mTempFile);
                }
                else {
                    ok = false;
                }
                break;
            case REQUEST_TAKE_PHOTO:
                ok = resultCode == Activity.RESULT_OK;
                break;
        }
        return ok;
    }

    static boolean copyToTempFile(Context context, Uri uri, File mTempFile) {
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

    public void onRequestPermissionsResult(ControlledElement element, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, ImagePickerClient client) {
        if (permissions.length > 0) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeBasedOnCode(element, requestCode, client);
                }
            } else if (permissions[0].equals(Manifest.permission.CAMERA)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeBasedOnCode(element, requestCode, client);
                }
            }
        }
    }


    private static boolean canIntentBeHandled(ControlledElement frag, Intent takePictureIntent) {
        return takePictureIntent.resolveActivity(frag.getControlledActivity().getPackageManager()) != null;
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
