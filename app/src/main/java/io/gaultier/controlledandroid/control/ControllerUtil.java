package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.List;

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
                                if (pleaseConsiderAllowing != 0) Toast.makeText(element.getControlledActivity(), pleaseConsiderAllowing, Toast.LENGTH_LONG).show();
                            }
                            else {
                                //Never ask again selected, or device policy prohibits the app from having that permission.
                                //So, disable that feature, or fall back to another situation...
                                if (deniedToast != 0) Toast.makeText(element.getControlledActivity(), deniedToast, Toast.LENGTH_LONG).show();
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

    @Nullable
    static <T extends AbstractController> T getByClass(Class<T> clazz, List<AbstractController> subCon) {
        for (AbstractController abstractController : subCon) {
            if (abstractController != null && abstractController.getClass() == clazz) {
                return (T) abstractController;
            }
        }
        return null;
    }

    public static <L extends AbstractActivityController> void launchActivitiesAndFinish(L ctrl, ControlledActivity from) {
        Intent intent1 = from.getManager().prepareIntent(from, ctrl, from.getController().getParentController());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(from);

        AbstractController p = ctrl;
        int i = 0;
        while (p instanceof AbstractActivityController) {
            AbstractActivityController ctrl1 = (AbstractActivityController) p;

            //creating the intent
            Class<? extends ControlledActivity> clazz = ctrl1.makeElement().getClass();
            stackBuilder.addParentStack(clazz);

            if (stackBuilder.getIntents().length != i+1) break;
            //changing the intent
            Intent parentIntent = stackBuilder.editIntentAt(i);
            AbstractController nextParent = p.getParentController();
            from.getManager().manageAndPutExtras(ctrl1, nextParent, parentIntent);
            p = nextParent;

        }
        stackBuilder.addNextIntent(intent1);


        TaskStackBuilder intent = stackBuilder;
        intent.startActivities();
        from.finish();
    }

    public static void launchActivityAndFinish(AbstractActivityController controller, ControlledActivity activity) {

        Intent intent = activity.getManager().prepareIntent(activity, controller, activity.getController().getParentController());

        activity.startActivity(intent);

        activity.finish();

        //activity.overridePendingTransition(controller.animation[0], activity.getController().animation[1]);
    }

    public static void launchActivity(AbstractActivityController controller, Context context) {
        ControllerManager manager = ControllerManager.getInstance();
        Intent intent = manager.prepareIntent(context, controller, manager.getMainController());

        context.startActivity(intent);
    }

    //for result -> requestCode = -1
    public static void launchActivity(AbstractController from, AbstractController to) {

        launchActivityForResult(from, to, -1, null);
    }

    public static void launchActivityForResult(AbstractController from, AbstractController to, int requestCode) {
        launchActivityForResult(from, to, requestCode, null);
    }

    public static void launchActivityForResult(AbstractController from, AbstractController to, int requestCode, Bundle options) {
        ControlledElement element = from.getManagedElement();

        ControllerManager manager = ControllerManager.getInstance();

        Intent intent = manager.prepareIntent(element.getControlledActivity(), (AbstractActivityController) to, from);


        if (element instanceof ControlledActivity) {
            ((ControlledActivity) element).startActivityForResult(intent, requestCode, options);
        }
        else if (element instanceof ControlledFragment) {
            ((ControlledFragment) element).startActivityForResult(intent, requestCode, options);
        }
        else throw new IllegalArgumentException();
    }

    public interface OnPermitted {

        void executeAction();
    }
}
