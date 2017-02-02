package io.gaultier.controlledandroid.control;

/**
 * Created by qg on 31/01/17.
 */

public class ControllerEvent {

    private final AbstractController publisher;

    public ControllerEvent(AbstractController publisher) {
        this.publisher = publisher;
    }
    public AbstractController getPublisher() {
        return publisher;
    }

}
