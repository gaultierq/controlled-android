package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Intent;

import org.parceler.Transient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by q on 16/10/16.
 */


public abstract class AbstractActivityController extends AbstractController {

    @Transient
    Set<AbstractController> fragmentControllers = new HashSet<>();

    public <T extends ControlledActivity> void  startActivity(Activity activity, Class<T> activityClass) {
        ControllerManager manager = ControllerManager.getInstance();
        Intent intent = new Intent(activity, activityClass);
        manager.manage(this);

        manager.saveController2(intent, this);
        activity.startActivity(intent);
    }

    public Collection<AbstractController> getFragmentControllers() {
        return fragmentControllers;
    }


    public boolean addFragmentControllers(AbstractFragmentController fragmentControllerId) {
        return fragmentControllers.add(fragmentControllerId);
    }
}
