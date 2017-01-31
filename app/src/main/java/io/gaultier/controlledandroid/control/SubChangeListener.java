package io.gaultier.controlledandroid.control;

/**
 * Created by q on 05/11/16.
 */
public interface SubChangeListener {

    // one of my sub-controller tells me to check something
    void onSubEvent(ControllerEvent event);
}
