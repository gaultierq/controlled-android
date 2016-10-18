package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import org.parceler.Parcels;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import io.gaultier.controlledandroid.Assert;
import io.gaultier.controlledandroid.Log;

/**
 * Created by q on 16/10/16.
 */

public class ControllerManager {

    private static final ControllerManager INSTANCE = new ControllerManager();
    private static final String TAG = "ControllerManager";

    private ControllerManager() {
        Log.i(TAG, "Creating instance");
    }

    private final SparseArray<AbstractController> managedControllers = new SparseArray<>();
    private final AtomicInteger counter = new AtomicInteger();

    public static ControllerManager getInstance() {
        return INSTANCE;
    }

    public void unmanage(Collection<AbstractController> controllerIds) {
        if (controllerIds != null) {
            for (AbstractController c : controllerIds) {
                AbstractController ctrl = getManagedController(c.getId());
                Assert.ensure(ctrl != null);
                Log.i(TAG, "Un-managing controller: ", ctrl);
                managedControllers.remove(c.getId());
            }
            logSize();
        }
    }

    public <T extends AbstractController> void manage(T controller) {
        Assert.ensure(controller != null);
        Assert.ensure(controller.getId() == AbstractController.INVALID_CONTROLLER_ID);
        controller.setId(generateControllerId());
        managedControllers.put(controller.getId(), controller);
        Log.i(TAG, "Managing controller: ", controller);
        logSize();
    }

    private void logSize() {
        Log.i(TAG, "Managed controllers: ", managedControllers.size());
    }

    public AbstractController getManagedController(int controllerId) {
        return managedControllers.get(controllerId);
    }

    public boolean isManaged(int controllerId) {
        return getManagedController(controllerId) != null;
    }

    public <T extends AbstractController> T restoreController(Bundle savedInstanceState) {
        Parcelable wrappedController = savedInstanceState.getParcelable(AbstractController.CONTROLLER);
        T controller = Parcels.<T>unwrap(wrappedController);
        Assert.ensure(controller != null);
        return controller;
    }

    private int generateControllerId() {
        return counter.incrementAndGet();

    }


    public <T extends AbstractController> void saveController(Bundle outState, T controller) {
        outState.putInt(AbstractController.CONTROLLER_ID, controller.getId());
        outState.putParcelable(AbstractController.CONTROLLER, Parcels.wrap(controller));
    }

    public <T extends AbstractController> void saveController2(Intent intent, AbstractActivityController controller) {
        intent.putExtra(AbstractController.CONTROLLER_ID, controller.getId());
        intent.putExtra(AbstractController.CONTROLLER, Parcels.wrap(controller));
    }

    public void addFragmentController(AbstractFragmentController fragController, ControlledActivity activity) {
        boolean added = activity.getController().addFragmentControllers(fragController);
        if (added) {
            Log.i(TAG, "Fragment controller", fragController, " added to activity controller.", "size=", activity.getController().getFragmentControllers().size());
        }
    }
}


