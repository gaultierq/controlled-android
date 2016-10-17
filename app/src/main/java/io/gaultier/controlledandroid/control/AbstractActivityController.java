package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by q on 16/10/16.
 */

public abstract class AbstractActivityController extends AbstractController {


    public <T extends ControlledActivity> void  startActivity(Activity activity, Class<T> activityClass) {
        ControllerManager manager = ControllerManager.getInstance();
        Intent intent = new Intent(activity, activityClass);
        manager.manage(this);

        manager.saveController2(intent, this);
        activity.startActivity(intent);
    }

}
