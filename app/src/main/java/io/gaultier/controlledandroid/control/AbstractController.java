package io.gaultier.controlledandroid.control;

import org.parceler.Transient;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

/**
 * Created by q on 16/10/16.
 */

public class AbstractController {
    // getInstance all class transient

    public static final String INVALID_CONTROLLER_ID = "0";
    public static final String CONTROLLER_ID = "CONTROLLER_ID";
    public static final String CONTROLLER = "CONTROLLER";


    public String debugmode;

    @Transient
    private String controllerId = INVALID_CONTROLLER_ID;

    @Transient
    Set<AbstractController> subControllers = new HashSet<AbstractController>();

    @Transient
    private AbstractController parentController;

    protected String parentControllerId;

    @Transient
    private ControlledElement managedElement;

    boolean managed;

    boolean isInitialized;

    boolean askRemove;
    boolean askAdd;
    int addIn;

    @Transient
    private String previousId;


    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public final String toString() {
        return "[" + blaze() + "." + controllerId + "<-" + parentControllerId + "]" + "~" + previousId;
    }

    protected String blaze() {
        return getClass().getSimpleName().replaceAll("Controller", "").replace("Activity", "-A").replace("Fragment", "-F");
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
        Assert.ensure(removed, "cleaning up an unmanaged controller:" + this);
        parentController = null;
        //parentControllerId = null;
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

    void ensureInitialized() {
        if (isInitialized == false) {
            init();
            isInitialized = true;
        }
    }

    // first time this controller will be displayed
    protected void init() {

    }


    public AbstractController getParentController() {
        return parentController;
    }

    public void assignParentController(AbstractController p) {
        this.parentController = p;
        if (p != null) {
            p.subControllers.add(this);
            parentControllerId = p.getControllerId();
        }
        else {
            parentControllerId = "-1";
            Log.e("Manager", "null parent for", this);
        }
    }

    public static boolean isValidId(String id) {
        return id != null && !INVALID_CONTROLLER_ID.equals(id) && Integer.parseInt(id) > 0;
    }

    public String getPreviousId() {
        return previousId;
    }

    public void setPreviousId(String previousId) {
        this.previousId = previousId;
    }

    public boolean isOrphan() {
        return getParentController() == null;
    }

    public boolean isAskRemove() {
        return askRemove;
    }

    public void setAskRemove(boolean askRemove) {
        this.askRemove = askRemove;
    }

    public boolean isAskAdd() {
        return askAdd;
    }

    public void setAskAdd(boolean askAdd, int askAddIn) {
        this.askAdd = askAdd;
        this.addIn = askAddIn;
    }

    public int getAddIn() {
        return addIn;
    }

    public final String tag() {
        return getClass().getSimpleName();
    }

    protected AbstractController self() {
        return this;
    }

    //notify change on parent controller
    protected final void notifyChange() {
        AbstractController p = getParentController();
        if (p != null) {
            p.onSubChange(this);
            ControlledElement managedEl = p.getManagedElement();
            if (managedEl != null) {
                p.getManagedElement().refresh();
            }
            p.notifyChange();
        }
    }
    // one of my sub-controller tells me to check something
    public void onSubChange(AbstractController controller) {

    }


}

