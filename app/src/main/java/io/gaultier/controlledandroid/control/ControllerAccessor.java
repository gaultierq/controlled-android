package io.gaultier.controlledandroid.control;

import android.os.Bundle;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 28/10/16.
 */

public class ControllerAccessor<T extends AbstractController> {

    T controllerInternal;

    public void obtain(ControlledElement<T> element, Bundle savedInstanceState, Bundle extras) {
        if (controllerInternal == null) {
            controllerInternal = ControllerManager.obtainIt(element, savedInstanceState, extras);
            controllerInternal.ensureInitialized();
        }
    }

    public T get() {
        return controllerInternal;
    }

    public String getId() {
        return controllerInternal != null ? controllerInternal.getControllerId() : INVALID_CONTROLLER_ID;
    }

    public void set(T controller) {
        controllerInternal = controller;
    }
}
