package io.gaultier.controlledandroid.control;

/**
 * Created by q on 18/10/16.
 */
public interface ControlledElement<T extends AbstractController> {
    
    public T makeController();

    public T getController();

    public String getControllerId();

    public void refresh();

    ControllerManager getManager();

    void link(T controller);
}
