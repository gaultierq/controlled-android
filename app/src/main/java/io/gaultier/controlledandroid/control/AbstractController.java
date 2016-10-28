package io.gaultier.controlledandroid.control;

import org.parceler.Transient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.gaultier.controlledandroid.util.Assert;

/**
 * Created by q on 16/10/16.
 */

public class AbstractController {
    // getInstance all class transient

    public static final int INVALID_CONTROLLER_ID = 0;
    public static final String CONTROLLER_ID = "CONTROLLER_ID";
    public static final String CONTROLLER = "CONTROLLER";


    @Transient
    private int controllerId = INVALID_CONTROLLER_ID;

    @Transient
    Set<AbstractController> subControllers = new HashSet<AbstractController>();

    @Transient
    protected AbstractController parentController;

    @Transient
    private ControlledElement managedElement;
    private boolean managed;

    boolean isInitialized;


    public int getControllerId() {
        return controllerId;
    }

    public void setControllerId(int controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public final String toString() {
        return "["+ getClass().getSimpleName() + "." + controllerId + "]" + "~" + hashCode();
    }

    public Collection<AbstractController> snapSubControllers() {
        return new HashSet<>(subControllers);
    }

    public boolean isManaged() {
        return managed;
    }

    public boolean hasId() {
        return controllerId != INVALID_CONTROLLER_ID;
    }

    public void cleanup() {
        boolean removed = parentController.subControllers.remove(this);
        Assert.ensure(removed);
        parentController = null;
    }

    // one of my sub-controller tells me to check something
    public void notifyChange(AbstractController controller) {

    }

    public ControlledElement getManagedElement() {
        return managedElement;
    }

    public <T extends AbstractController> void setManagedElement(ControlledElement<T> managedElement) {
        Assert.ensure(isManaged());
        this.managedElement = managedElement;
    }

    // cleanup all states from previous displays
    // the view is about to be displayed again
    public void reset() {
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    public void setNew() {
        this.isInitialized = true;
    }


    void ensureInitialized() {
        if (isInitialized == false) {
            init();
            isInitialized = true;
        }
    }

    // first time this controller will be displayed
    protected void init() {

    }
}

