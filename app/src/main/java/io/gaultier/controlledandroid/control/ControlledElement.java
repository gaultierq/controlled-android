package io.gaultier.controlledandroid.control;

import android.support.v4.app.FragmentManager;

/**
 * Created by q on 18/10/16.
 */
public interface ControlledElement<T extends AbstractController> {
    
    T makeController();

    // get the controller of this element
    // NB: this controller may be null. it is set at the begining of "createView"
    // NB2: a controller can hold an element (w/ getManagedElement) before "createView"
    T getController();

    String getControllerId();

    void refresh();

    ControllerManager getManager();

    ControlledActivity getControlledActivity();

    // return true if intercepted, false otherwise
    boolean interceptBackPressed();

    // witch fragment manager should we use to add fragment child
    FragmentManager obtainFragmentManager(AbstractController child);
}
