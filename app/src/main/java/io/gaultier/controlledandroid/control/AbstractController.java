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
    // make all class transient

    public static final int INVALID_CONTROLLER_ID = 0;
    public static final String CONTROLLER_ID = "CONTROLLER_ID";
    public static final String CONTROLLER = "CONTROLLER";


    @Transient
    int id = INVALID_CONTROLLER_ID;

    @Transient
    ControllerStatus status = ControllerStatus.CREATE;

    @Transient
    Set<AbstractController> subControllers = new HashSet<AbstractController>();

    @Transient
    protected AbstractController parentController;

    // numer of view created with this controller
    int viewCreationCount;

    @Transient
    private ControlledElement managedElement;


    public int getId() {
        return id;
    }

    public void assignId(int id) {
        this.id = id;
        assignStatus(ControllerStatus.WITH_ID);
    }

    public void assignStatus(ControllerStatus newStatus) {
        Assert.ensure(newStatus.ordinal() - status.ordinal() <= 1,
                "jumping from status '" + status + "' to '" +newStatus + "'");
        status = newStatus;
    }

    @Override
    public final String toString() {
        return "["+ getClass().getSimpleName() + "." + id + "]" + "(" + status + ")~" + hashCode();
    }

    public void manageSubController(AbstractController subCtrl) {
        if (!subCtrl.isReady()) {
            subControllers.add(subCtrl);
            subCtrl.parentController = this;
            subCtrl.assignStatus(ControllerStatus.ACTIVE);
        }
    }

    public Collection<AbstractController> getSubControllers() {
        return subControllers;
    }

    public boolean isManaged() {
        return status.isAtLeast(ControllerStatus.MANAGED);
    }

    public boolean isReady() {
        return status.isAtLeast(ControllerStatus.ACTIVE);
    }

    public boolean hasId() {
        boolean res = status.isAtLeast(ControllerStatus.WITH_ID);
        Assert.ensure(res == (id != INVALID_CONTROLLER_ID));
        return res;
    }

    public boolean isFirstInflate() {
        return viewCreationCount == 0;
    }

    public void cleanup() {
        parentController = null;
    }

    // one of my sub-controller tells me to check something
    public void notifyChange(AbstractController controller) {

    }

    public ControlledElement getManagedElement() {
        return managedElement;
    }

    public <T extends AbstractController> void setManagedElement(ControlledElement<T> managedElement) {
        this.managedElement = managedElement;
    }
}
