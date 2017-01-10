package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by qg on 07/12/16.
 */

public class ControllerUtil {

    public static <T extends AbstractController> void exectuteWhenPermitted(
            final ControlledElement<T> element,
            final String thePermission,
            final OnPermitted onActionPermitted,
            final int pleaseConsiderAllowing,
            final int deniedToast) {


        element.getController().addOnRequestPermissionsResultCallback(new AbstractController.OnRequestPermissionsResultCallback() {
            @Override
            public void onRequestPermissionsResult(
                    ControlledActivity a,
                    int requestCode,
                    @NonNull String[] permissions,
                    @NonNull int[] grantResults) {


                if (requestCode == permissionCode(thePermission)) {
                    a.getController().removeOnRequestPermissionsResultCallback(this);
                    if (grantResults.length > 0){
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            onActionPermitted.executeAction();
                        }
                        else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                            if (ActivityCompat.shouldShowRequestPermissionRationale(a, thePermission)) {
                                Toast.makeText(element.getControlledActivity(), pleaseConsiderAllowing, Toast.LENGTH_LONG).show();
                            }
                            else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                Toast.makeText(element.getControlledActivity(), deniedToast, Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                }


            }
        });

        if (ContextCompat.checkSelfPermission(getContext(element), thePermission) != PackageManager.PERMISSION_GRANTED) {
            if (element instanceof Activity) {
                ActivityCompat.requestPermissions(
                        (Activity) element,
                        new String[]{thePermission},
                        permissionCode(thePermission));
            }
            else if (element instanceof Fragment) {
                ((Fragment) element).requestPermissions(new String[]{thePermission}, permissionCode(thePermission));
            }
            else {
                throw new IllegalStateException();
            }
        }
        else {
            onActionPermitted.executeAction();
        }
    }

    public static <T extends AbstractController> Context getContext(ControlledElement<T> element) {
        if (element instanceof Activity) {
            return (Context) element;
        } else if (element instanceof Fragment) {
            return ((Fragment) element).getContext();
        }
        throw new IllegalStateException();
    }

    protected static int permissionCode(String thePermission) {
        return thePermission.hashCode() & 0x0000ffff;
    }

    public static void startActivityForResult(Intent intent, ControlledElement element, int requestCode) {
        if (element instanceof Fragment) {
            ((Fragment) element).startActivityForResult(intent, requestCode);

        }
        else if (element instanceof Activity) {
            ActivityCompat.startActivityForResult((Activity) element, intent, requestCode, null);
        }
        else {
            throw new IllegalStateException();
        }
    }

    public interface OnPermitted {

        void executeAction();
    }
}
