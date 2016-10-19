package io.gaultier.controlledandroid.control;

/**
 * Created by q on 18/10/16.
 */
interface ControlledElement<T extends AbstractController> {
    
    public abstract T makeController();

    public T getController();

    public int getControllerId();
}
