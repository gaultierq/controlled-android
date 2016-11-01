package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;
import static io.gaultier.controlledandroid.control.AbstractController.isValidId;

/**
 * Created by q on 16/10/16.
 */

public class ControllerManager {

    private static final String TAG = "ControllerManager";

    private static ControllerManager INSTANCE;

    private final LinkedHashMap<String, AbstractController> managedControllers = new LinkedHashMap<>();

    private final AtomicInteger counter = new AtomicInteger(-1);

    private final int session;
    private FragmentManager.OnBackStackChangedListener listener;

    private final AbstractController mainController;

    private WeakReference<Activity> currentActivity;

    private ControllerManager(int oldSessionId, int sessionId) {
        Log.i(TAG, "Creating instance");
        mainController = new AbstractController();
        session = sessionId;
        mainController.setPreviousId("" + oldSessionId);
        manageAndAssignParent(mainController, new AbstractController());
    }

    public static void make(int oldSessionId, int sessionId) {
        Assert.ensure(INSTANCE == null, "ControllerManager already init");
        INSTANCE = new ControllerManager(oldSessionId, sessionId);
    }

    public static ControllerManager getInstance(ControlledActivity activity) {
        if (INSTANCE == null) {
            throw new RuntimeException("ControllerManager not inited");
        }
        INSTANCE.attachActivity(activity);
        return INSTANCE;
    }


    // for sure 100% buggy
    private void attachActivity(final ControlledActivity activity) {
        if (currentActivity == null || currentActivity.get() != activity) {
            FragmentManager supportFragmentManager = activity.getSupportFragmentManager();

            if (listener != null) {
                supportFragmentManager.removeOnBackStackChangedListener(listener);
            }
            listener = new FragmentManager.OnBackStackChangedListener() {
                public void onBackStackChanged() {
                    Log.d(TAG, "On back stack change");
                    ControllerManager.this.cleanupFragmentStack(activity);
                }
            };

            supportFragmentManager.addOnBackStackChangedListener(listener);
        }

    }

    @Nullable
    static <T extends AbstractController> T obtainIt(ControlledElement<T> element, Bundle savedInstanceState, Bundle arguments) {
        ControllerManager manager = element.getManager();
        String controllerId;
        T controller;
        if (isValidId(controllerId = readControllerId(savedInstanceState))) {
            // fragment has already existed (rotation, restore, killed)
            // -> restore controller
            //eg: rotation
            if (manager.isManaged(controllerId)) {
                controller = (T) manager.getManagedController(controllerId);
                controller.debugmode = "saved-managed";
            } else { //killed
                controller = manager.restoreController(savedInstanceState);
                controller.debugmode = "saved-killed";
                manager.manage(controller);
            }
        } else if (isValidId(controllerId = readControllerId(arguments))) {
            // fragment created programatically (already managed)
            // -> find controller
            Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller for id=" + controllerId + ", arguments=" + arguments);
            controller = (T) manager.getManagedController(controllerId);
            Assert.ensure(!controller.isOrphan(), "unexpected orphan:" + controller);

            controller.debugmode = "from args";
        } else {
            // creation by system (main activity, fragment)
            // -> create new controller (should be the only controller factory)
            controller = element.makeController();
            Assert.ensure(controller != null);
            Assert.ensure(controller.getParentController() != null, "This controller should have a parent controller.");
            manager.manage(controller);
            controller.debugmode = "from factory";
        }

        // binding
        controller.setPreviousId(controllerId);
        if (manager.assignParent(controller)) {
            manager.runParentNet();
        }

        if (controller.getParentController() == null){
            Log.w(TAG, "No parent linked to", controller, "for parent id",controller.parentControllerId, "when mode was", controller.debugmode);
        }

        Assert.ensure(controller.isManaged());
        controller.setManagedElement(element);
        controller.ensureInitialized();

        return controller;
    }


    private <T extends AbstractController> boolean assignParent(T controller) {
        if (controller.isOrphan()) {
            AbstractController parent = findParent(controller);
            if (parent != null) {
                controller.assignParentController(parent);
                return true;
            }
        }
        return false;
    }

    private void runParentNet() {
        //check
        int o = logOrphanCount();
        for (AbstractController c : managedControllers.values()) {
            if (this.assignParent(c) ) {
                Log.i(TAG, "The net is working");
            }
        }
        int n = logOrphanCount();
        if (o != n) {
            Log.w(TAG, n, "orphans");
        }
    }

    private int logOrphanCount() {
        int orphan = 0;
        for (AbstractController c : managedControllers.values()) {
            if (c.getParentController() == null) {
                orphan++;
            }
        }
        return orphan;
    }

    private AbstractController findParent(AbstractController controller) {
        AbstractController parent = controller.getParentController();
        if (parent == null) {
            //lets find your father
            String id = controller.parentControllerId;
            Assert.ensure(isValidId(id), "unexpected id " + id + " for controller " + controller);
            for (AbstractController c : managedControllers.values()) {
                if (id.equals(c.getControllerId()) || id.equals(c.getPreviousId())) {
                    Assert.ensure(parent == null);
                    parent = c;
                }
            }
        }

        return parent;
    }

    private static String readControllerId(Bundle bundle) {
        if (bundle != null) {
            return bundle.getString(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
        }
        return INVALID_CONTROLLER_ID;
    }

    @NonNull
    static String toString(ControlledElement el) {
        return "[" + "controlled:" + el.getClass().getSimpleName() + "." + el.getControllerId() + "]";
    }

    public String toString() {
        return "[" + "manager [" + toString(managedControllers) + "]";
    }

    private <K, V> String toString(LinkedHashMap<K, V> managedControllers) {
        if (managedControllers == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder("size=" + managedControllers.size() + " {");
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(managedControllers.entrySet());

        ListIterator<Map.Entry<K, V>> it = entries.listIterator(entries.size());

        while (it.hasPrevious()) {
            Map.Entry<K, V> e = it.previous();
            b.append(e.getValue());
            if (it.hasPrevious()) {
                b.append(", ");
            }
        }
        b.append("}");
        return b.toString();
    }


    public static <F extends ControlledActivity, C extends AbstractController, T extends ControlledActivity<C>> void startActivity(
            F fromActivity,
            Class<T> toActivityClass,
            C toActivityController

    ) {

        Intent intent = new Intent(fromActivity, toActivityClass);

        ControllerManager manager = getInstance(fromActivity);
        Assert.ensure(fromActivity.getController().isOrphan() == false, "no parent for :" + fromActivity.getController());
        manager.manageAndAssignParent(toActivityController, fromActivity.getController().getParentController());
        // to unmanage, can finish the previous activity
        //manager.unmanage(fromActivity.getController());
        intent.putExtras(ControllerManager.saveController(new Bundle(), toActivityController));
        fromActivity.startActivity(intent);

    }

    // as we dont have a callback on the fragment "finish" event, we
    // listen to the fragmentmanager changes to cleanup afterwards
    private boolean cleanupFragmentStack(ControlledActivity activity) {
        List<Fragment> frags = activity.getSupportFragmentManager().getFragments();
        AbstractController controller = activity.getController();

        if (cleanup(controller, frags)) {
            Assert.ensure(!cleanupFragmentStack(activity));
            return true;
        }
        return false;
    }

    private boolean cleanup(AbstractController controller, List<Fragment> frags) {
        Set<AbstractController> subcontrollers = new HashSet<>(controller.subControllers);
        boolean cleaned = false;
        for (AbstractController sc : subcontrollers) {
            ControlledElement e = sc.getManagedElement();
            if (e instanceof ControlledFragment && !frags.contains(e)) {
                this.unmanage(sc);
                cleaned = true;
            }
            cleaned |= cleanup(sc, frags);
        }
        return cleaned;
    }


    public <U extends AbstractController, T extends ControlledFragment<U>> T managedNewFragment(
            T frag, U fragmentController, AbstractController parent) {
        try {
            Assert.ensure(!fragmentController.hasId());
            manageAndAssignParent(fragmentController, parent);
            frag.setArguments(ControllerManager.saveController(new Bundle(), fragmentController));
            frag.setController(null);
            return frag;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void unmanage(AbstractController controller) {
        unmanage(controller.snapSubControllers());

        String id = controller.getControllerId();
        Assert.ensure(isValidId(id), "Unexpected. " + controller + " in " + this);
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

    private <T extends AbstractController> void manage(T controller) {
        Assert.ensure(AbstractController.INVALID_CONTROLLER_ID.equals(controller.getControllerId()), ""+controller);
        controller.setControllerId("" + generateControllerId());
        managedControllers.put(controller.getControllerId(), controller);
        controller.setManaged(true);
        Log.i(TAG, "++: ", "manager:", this);
    }

    // manageAndAssignParent provide an id to the controller
    private <T extends AbstractController> void manageAndAssignParent(T controller, AbstractController parentController) {
        controller.assignParentController(parentController);
        manage(controller);
    }

    // can return null
    private AbstractController getManagedController(String controllerId) {
        return managedControllers.get(controllerId);
    }

    private boolean isManaged(String controllerId) {
        AbstractController managedController = getManagedController(controllerId);
        if (managedController != null) {
            Assert.ensure(managedController.isManaged());
            return true;
        }
        return false;
    }

    private <T extends AbstractController> T restoreController(Bundle savedInstanceState) {
        Parcelable wrappedController = savedInstanceState.getParcelable(AbstractController.CONTROLLER);
        T controller = Parcels.unwrap(wrappedController);
        Assert.ensure(controller != null);
        return controller;
    }

    private int generateControllerId() {
        return session + counter.incrementAndGet();

    }

    static <T extends AbstractController> Bundle saveController(Bundle outState, T controller) {
        outState.putString(AbstractController.CONTROLLER_ID, controller.getControllerId());
        outState.putParcelable(AbstractController.CONTROLLER, Parcels.wrap(controller));
        return outState;
    }

    AbstractController getMainController() {
        return mainController;
    }


}


