package io.gaultier.controlledandroid.control;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by qg on 07/12/16.
 */

public class ControllerUtil {

    public static <T extends AbstractController> void exectuteWhenPermitted(
            final ControlledActivity<T> activity,
            final String thePermission,
            final OnPermitted onActionPermitted,
            final int pleaseConsiderAllowing,
            final int deniedToast
    ) {
        if (ContextCompat.checkSelfPermission(activity, thePermission) != PackageManager.PERMISSION_GRANTED) {



            ActivityCompat.requestPermissions(activity,
                    new String[]{thePermission},
                    permissionCode(thePermission));


            activity.getController().addOnRequestPermissionsResultCallback(new AbstractController.OnRequestPermissionsResultCallback<T>() {
                @Override
                public void onRequestPermissionsResult(
                        ControlledActivity<T> a,
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
                                    Toast.makeText(activity, pleaseConsiderAllowing, Toast.LENGTH_LONG).show();
                                }
                                else {
                                    //Never ask again selected, or device policy prohibits the app from having that permission.
                                    //So, disable that feature, or fall back to another situation...
                                    Toast.makeText(activity, deniedToast, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                    }


                }
            });
        }
        else {
            onActionPermitted.executeAction();
        }
    }

    protected static int permissionCode(String thePermission) {
        return thePermission.hashCode() & 0x0000ffff;
    }

    public interface OnPermitted {

        void executeAction();
    }
}
