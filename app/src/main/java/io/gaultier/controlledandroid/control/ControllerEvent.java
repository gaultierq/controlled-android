package io.gaultier.controlledandroid.control;

/**
 * Created by qg on 31/01/17.
 */

public class ControllerEvent {

    private AbstractController subcontroller;

    ControllerEvent(AbstractController subcontroller) {
        this.subcontroller = subcontroller;
    }

    public AbstractController getSubcontroller() {
        return subcontroller;
    }
}
