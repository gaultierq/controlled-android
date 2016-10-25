package io.gaultier.controlledandroid.control;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.parceler.Parcels;

import java.util.Collection;
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

    private static final ControllerManager INSTANCE = new ControllerManager();

    private final SparseArray<AbstractController> managedControllers = new SparseArray<>();

    private final AtomicInteger counter = new AtomicInteger();

    //TODO: get rid of random
    private final int session = new Random().nextInt(10000);

    //TODO: let the controller know its fragment

    private ControllerManager() {
        Log.i(TAG, "Creating instance");
    }

    public static ControllerManager getInstance() {
        return INSTANCE;
    }

    // return a managed controller
    public static <T extends AbstractController> T obtainController(final Bundle savedInstanceState,
                                                                    Bundle arguments,
                                                                    ControlledElement<T> element,
                                                                    ControllerManager manager) {
        T controller;
        int controllerId;

        if (savedInstanceState != null) {
            // fragment has already existed (rotation, restore, killed)
            // -> restore controller
            controllerId = savedInstanceState.getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
            Assert.ensure(controllerId != INVALID_CONTROLLER_ID);

            //eg: rotation
            if (manager.isManaged(controllerId)) {
                controller = (T) manager.getManagedController(controllerId);
            }
            else { //killed
                controller = manager.<T>restoreController(savedInstanceState);
                manager.manage(controller);
            }
        }
        else if (arguments != null) {
            // fragment created programatically (already managed)
            // -> find controller
            controllerId = arguments.getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
            Assert.ensure(controllerId != INVALID_CONTROLLER_ID, "This controller should have an id " + arguments);
            Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller");
            controller = (T) manager.getManagedController(controllerId);
        }
        else {
            // creation by system (main activity, fragment)
            // -> create controller
            controller = element.makeController();
            Assert.ensure(controller != null);
            manager.manage(controller);
        }
        Assert.ensure(controller.isManaged());

        return controller;
    }

    @NonNull
    static String toString(ControlledElement el) {
        return "["+"controlled:" + el.getClass().getSimpleName() + "." + el.getControllerId()+"]";
    }

    public static <T extends ControlledActivity> void startActivity(
            Activity fromActivity,
            Class<T> toActivityClass,
            AbstractController toActivityController) {

        Intent intent = new Intent(fromActivity, toActivityClass);

        getInstance().manage(toActivityController);
        intent.putExtras(ControllerManager.saveController(new Bundle(), toActivityController));
        fromActivity.startActivity(intent);
    }


    public <T extends ControlledFragment> T managedNewFragment(T frag, AbstractController fragmentController) {
        try {
            Assert.ensure(!fragmentController.hasId());
            manage(fragmentController);
            frag.setArguments(ControllerManager.saveController(new Bundle(), fragmentController));
            return frag;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unmanage(Collection<AbstractController> controllers) {
        for (AbstractController ctrl : controllers) {
            Log.i(TAG, "Unmanaging controller: ", ctrl);
            unmanage(ctrl.getSubControllers());
            ctrl.assignStatus(ControllerStatus.UNACTIVE);
            managedControllers.remove(ctrl.getId());
            ctrl.assignStatus(ControllerStatus.UNMANAGED);
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


