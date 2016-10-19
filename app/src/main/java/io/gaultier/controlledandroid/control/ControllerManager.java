package io.gaultier.controlledandroid.control;

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

    private static final ControllerManager INSTANCE = new ControllerManager();
    private static final String TAG = "ControllerManager";

    private ControllerManager() {
        Log.i(TAG, "Creating instance");
    }

    private final SparseArray<AbstractController> managedControllers = new SparseArray<>();
    private final AtomicInteger counter = new AtomicInteger();

    private final int session = new Random().nextInt(10000);

    public static ControllerManager getInstance() {
        return INSTANCE;
    }

    public static <T extends AbstractController> T obtainController(final Bundle savedInstanceState,
                                                                    Bundle arguments,
                                                                    ControlledElement<T> factory,
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
        else {
            if (arguments != null) {
                // fragment created programatically (already managed)
                // -> find controller
                controllerId = arguments.getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
                Assert.ensure(controllerId != INVALID_CONTROLLER_ID, "Creating fragments via controllers");
                Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller");
                controller = (T) manager.getManagedController(controllerId);
            }
            else {
                // creation by system (main activity, fragment)
                // -> create controller
                controller = factory.makeController();
                Assert.ensure(controller != null);
                manager.manage(controller);
            }
        }
        return controller;
    }

    @NonNull
    static String toString(ControlledElement tControlledFragment) {
        return "["+"controlled-" + tControlledFragment.getClass().getSimpleName() + "-" + tControlledFragment.getControllerId()+"]";
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
        //Log.i(TAG, "controller ", controller, "restored from savedInstanceState: ", savedInstanceState);
        return controller;
    }

    private int generateControllerId() {
        return session + counter.incrementAndGet();

    }


    public <T extends AbstractController> void saveController(Bundle outState, T controller) {
        outState.putInt(AbstractController.CONTROLLER_ID, controller.getId());
        Assert.ensure(outState.getParcelable(AbstractController.CONTROLLER) == null);
        outState.putParcelable(AbstractController.CONTROLLER, Parcels.wrap(controller));
    }

    public <T extends AbstractController> void saveController2(Intent intent, AbstractActivityController controller) {
        intent.putExtra(AbstractController.CONTROLLER_ID, controller.getId());
        intent.putExtra(AbstractController.CONTROLLER, Parcels.wrap(controller));
    }

    public void addFragmentController(AbstractFragmentController fragController, ControlledActivity activity) {
        activity.getController().addFragmentControllers(fragController);
    }
}


