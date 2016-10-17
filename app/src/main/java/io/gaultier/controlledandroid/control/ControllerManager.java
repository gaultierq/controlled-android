package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import org.parceler.Parcels;

import java.util.concurrent.atomic.AtomicInteger;

import io.gaultier.controlledandroid.Assert;

/**
 * Created by q on 16/10/16.
 */

public class ControllerManager {

    private static final ControllerManager INSTANCE = new ControllerManager();

    private ControllerManager() {}

    private final SparseArray<AbstractController> managedControllers = new SparseArray<>();
    private final AtomicInteger counter = new AtomicInteger();

    public static ControllerManager getInstance() {
        return INSTANCE;
    }

    public <T extends AbstractController> void unmanage(T controller) {
        managedControllers.remove(controller.getId());
    }

    public <T extends AbstractController> void manage(T controller) {
        Assert.ensure(controller != null);
        Assert.ensure(controller.getId() == AbstractController.INVALID_CONTROLLER_ID);
        controller.setId(generateControllerId());
        managedControllers.put(controller.getId(), controller);
    }

    public void startActivity(AbstractController a) {

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
        //changing the controller ID
        controller.setId(AbstractController.INVALID_CONTROLLER_ID);
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
}


