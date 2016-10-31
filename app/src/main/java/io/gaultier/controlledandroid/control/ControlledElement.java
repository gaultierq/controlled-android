package io.gaultier.controlledandroid.control;

/**
 * Created by q on 18/10/16.
 */
public interface ControlledElement<T extends AbstractController> {
    
    T makeController();

    T getController();

    String getControllerId();

    void refresh();

    ControllerManager getManager();

}
