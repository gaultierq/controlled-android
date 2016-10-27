package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public class ControllerManager {

    private static final String TAG = "ControllerManager";

    private static ControllerManager INSTANCE;

    private final Map<Integer, AbstractController> managedControllers = new HashMap<>();

    private final AtomicInteger counter = new AtomicInteger();

    //TODO: get rid of random
    private final int session = new Random().nextInt(10000);
    private FragmentManager.OnBackStackChangedListener listener;

    WeakReference<Activity> currentActivity;

    private ControllerManager() {
        Log.i(TAG, "Creating instance");
    }

    public static ControllerManager getInstance(FragmentActivity activity) {
        if (INSTANCE == null) {
            INSTANCE = new ControllerManager();
        }
        INSTANCE.attachActivity(activity);
        return INSTANCE;
    }

    // for sure 100% buggy
    private void attachActivity(final FragmentActivity activity) {
        if (currentActivity == null || currentActivity.get() != activity) {
            FragmentManager supportFragmentManager = activity.getSupportFragmentManager();

            if (listener != null) {
                supportFragmentManager.removeOnBackStackChangedListener(listener);
            }
            listener = new FragmentManager.OnBackStackChangedListener() {
                public void onBackStackChanged() {
                    Log.d(TAG, "On back stack change");
                    ControllerManager.this.cleanupStack(activity.getSupportFragmentManager());
                }
            };

            supportFragmentManager.addOnBackStackChangedListener(listener);
        }

    }

    // return a managed controller
    public static <T extends AbstractController> T obtainController(final Bundle savedInstanceState,
                                                                    Bundle arguments,
                                                                    ControlledElement<T> element,
                                                                    ControllerManager manager) {
        T controller;
        int controllerId;

        if ((controllerId = readControllerId(savedInstanceState)) != INVALID_CONTROLLER_ID) {
            // fragment has already existed (rotation, restore, killed)
            // -> restore controller
            //eg: rotation
            if (manager.isManaged(controllerId)) {
                controller = (T) manager.getManagedController(controllerId);
            } else { //killed
                controller = manager.<T>restoreController(savedInstanceState);
                manager.manage(controller);
            }
        } else if ((controllerId = readControllerId(arguments)) != INVALID_CONTROLLER_ID) {
            // fragment created programatically (already managed)
            // -> find controller
            Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller");
            controller = (T) manager.getManagedController(controllerId);
        } else {
            // creation by system (main activity, fragment)
            // -> create controller
            controller = element.makeController();
            Assert.ensure(controller != null);
            manager.manage(controller);
        }
        Assert.ensure(controller.isManaged());
        controller.setManagedElement(element);

        return controller;
    }

    static int readControllerId(Bundle bundle) {
        if (bundle != null) {
            return bundle.getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
        }
        return INVALID_CONTROLLER_ID;
    }

    @NonNull
    static String toString(ControlledElement el) {
        return "[" + "controlled:" + el.getClass().getSimpleName() + "." + el.getControllerId() + "]";
    }

    public static <T extends ControlledActivity> void startActivity(
            ControlledActivity fromActivity,
            Class<T> toActivityClass,
            AbstractController toActivityController

    ) {

        Intent intent = new Intent(fromActivity, toActivityClass);

        ControllerManager manager = getInstance(fromActivity);
        manager.manage(toActivityController);
        manager.unmanage(fromActivity.getController());
        intent.putExtras(ControllerManager.saveController(new Bundle(), toActivityController));
        fromActivity.startActivity(intent);

    }

    public boolean cleanupStack(FragmentManager supportFragmentManager) {
        boolean cleaned = false;
        Map<ControlledElement, AbstractController> els = this.snapElements();
        List<Fragment> frags = supportFragmentManager.getFragments();
        for (ControlledElement e : els.keySet()) {
            if (e instanceof ControlledFragment && !frags.contains(e)) {
                this.unmanage(els.get(e));
                cleaned = true;
            }
        }
        if (cleaned) {
            Assert.ensure(cleanupStack(supportFragmentManager) == false);
        }
        return cleaned;
    }


    public <T extends ControlledFragment> T managedNewFragment(T frag, AbstractController fragmentController) {
        try {
            Assert.ensure(!fragmentController.hasId());
            manage(fragmentController);
            frag.setArguments(ControllerManager.saveController(new Bundle(), fragmentController));
            return frag;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void unmanage(AbstractController ctrl) {
        Log.i(TAG, "Unmanaging controller: ", ctrl);
        unmanage(ctrl.getSubControllers());
        ctrl.assignStatus(ControllerStatus.UNACTIVE);
        managedControllers.remove(ctrl.getId());
        ctrl.cleanup();
        ctrl.assignStatus(ControllerStatus.UNMANAGED);
    }

    private void unmanage(Collection<AbstractController> controllers) {
        for (AbstractController ctrl : controllers) {
            unmanage(ctrl);
        }
        logSize();
    }

    // manage provide an id to the controller
    public <T extends AbstractController> void manage(T controller) {
        Assert.ensure(controller != null);
        Assert.ensure(controller.getId() == AbstractController.INVALID_CONTROLLER_ID);
        controller.assignId(generateControllerId());
        Log.i(TAG, "Managing controller: ", controller);
        managedControllers.put(controller.getId(), controller);
        controller.assignStatus(ControllerStatus.MANAGED);
        logSize();
    }

    private void logSize() {
        Log.i(TAG, "Managed controllers: ", managedControllers.size());
    }

    // can return null
    public AbstractController getManagedController(int controllerId) {
        return managedControllers.get(controllerId);
    }

    public Map<ControlledElement, AbstractController> snapElements() {
        Map<ControlledElement, AbstractController> r = new HashMap<>();
        for (AbstractController m : managedControllers.values()) {
            r.put(m.getManagedElement(), m);
        }
        return r;
    }

    public boolean isManaged(int controllerId) {
        AbstractController managedController = getManagedController(controllerId);
        if (managedController != null) {
            Assert.ensure(managedController.isManaged());
            return true;
        }
        return false;
    }

    public <T extends AbstractController> T restoreController(Bundle savedInstanceState) {
        Parcelable wrappedController = savedInstanceState.getParcelable(AbstractController.CONTROLLER);
        T controller = Parcels.<T>unwrap(wrappedController);
        Assert.ensure(controller != null);
        return controller;
    }

    private int generateControllerId() {
        return session + counter.incrementAndGet();

    }

    public static <T extends AbstractController> Bundle saveController(Bundle outState, T controller) {
        outState.putInt(AbstractController.CONTROLLER_ID, controller.getId());
        outState.putParcelable(AbstractController.CONTROLLER, Parcels.wrap(controller));
        return outState;
    }

}


