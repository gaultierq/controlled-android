package io.gaultier.controlledandroid.control;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by q on 16/10/16.
 */

public class ControllerManager {

    private static final ControllerManager INSTANCE = new ControllerManager();

    private ControllerManager() {}

    private final Map<String, AbstractActivityController> managedControllers = new HashMap<>();

    public static ControllerManager getInstance() {
        return INSTANCE;
    }

    public <T extends AbstractActivityController> void unregister(T controller) {
        managedControllers.remove(controller.getId());
    }

    public <T extends AbstractActivityController> void register(T controller) {
        managedControllers.put(controller.getId(), controller);
    }

    public void startActivity(AbstractController a) {

    }
}


