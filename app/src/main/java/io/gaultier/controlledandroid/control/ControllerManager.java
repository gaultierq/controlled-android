package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

    private final AbstractController mainController = new ApplicationController();

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

    @Nullable
    public static <T extends AbstractController> T obtainIt(ControlledElement<T> element, Bundle savedInstanceState, Bundle arguments) {
        ControllerManager manager = element.getManager();
        int controllerId;
        T controller;
        if ((controllerId = readControllerId(savedInstanceState)) != INVALID_CONTROLLER_ID) {
            // fragment has already existed (rotation, restore, killed)
            // -> restore controller
            //eg: rotation
            if (manager.isManaged(controllerId)) {
                controller = (T) manager.getManagedController(controllerId);
            } else { //killed
                controller = manager.restoreController(savedInstanceState);
                manager.manage(controller);
            }
        }
        else if ((controllerId = readControllerId(arguments)) != INVALID_CONTROLLER_ID) {
            // fragment created programatically (already managed)
            // -> find controller
            Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller for id=" + controllerId + ", arguments=" + arguments );
            controller = (T) manager.getManagedController(controllerId);
        } else {
            // creation by system (main activity, fragment)
            // -> create new controller (should be the only controller factory)
            controller = element.makeController();
            manager.manage(controller);
            Assert.ensure(controller != null);
        }
        controller.setManagedElement(element);
        controller.ensureInitialized();
        element.link(controller);
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
    public String toString() {
        return "[" + "manager ["+toString(managedControllers) + "]" ;
    }

    private <K,V> String toString(Map<K, V> managedControllers) {
        if (managedControllers == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder("size="+managedControllers.size() + " {");
        Iterator<Map.Entry<K, V>> it = managedControllers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> e = it.next();
            b.append(e.getValue());
            if (it.hasNext()) {
                b.append(", ");
            }
        }
        b.append("}");
        return b.toString();
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
            frag.setController(null);
            return frag;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unmanage(AbstractController controller) {
        unmanage(controller.snapSubControllers());

        int id = controller.getControllerId();
        Assert.ensure(id != INVALID_CONTROLLER_ID, "Unexpected. " + controller + " in " + this);
        AbstractController unmanaged = managedControllers.remove(id);
        Assert.ensure(unmanaged == controller, "Controller not found:" + controller + " in " + this);

        unmanaged.cleanup();
        unmanaged.setManaged(false);
        Log.i(TAG, "--: ", controller, "manager:", this);
    }

    private void unmanage(Collection<AbstractController> controllers) {
        for (AbstractController ctrl : controllers) {
            unmanage(ctrl);
        }
    }

    // manage provide an id to the controller
    public <T extends AbstractController> void manage(T controller) {
        Assert.ensure(controller != null);
        Assert.ensure(controller.getControllerId() == AbstractController.INVALID_CONTROLLER_ID);
        controller.setControllerId(generateControllerId());
        managedControllers.put(controller.getControllerId(), controller);
        controller.setManaged(true);
        Log.i(TAG, "++: ", controller, "manager:", this);
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
        outState.putInt(AbstractController.CONTROLLER_ID, controller.getControllerId());
        outState.putParcelable(AbstractController.CONTROLLER, Parcels.wrap(controller));
        return outState;
    }

    public AbstractController getMainController() {
        return mainController;
    }
}


