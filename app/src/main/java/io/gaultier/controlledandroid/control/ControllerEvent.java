package io.gaultier.controlledandroid.control;

/**
 * Created by qg on 31/01/17.
 */

public class ControllerEvent {

    private AbstractController publisher;

    ControllerEvent() {
    }

    public AbstractController getPublisher() {
        return publisher;
    }

    void setPublisher(AbstractController publisher) {
        this.publisher = publisher;
    }
}
