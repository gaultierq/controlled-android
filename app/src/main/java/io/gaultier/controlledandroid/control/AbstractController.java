package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.preference.PreferenceManager;

import org.parceler.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.PendingOperationType.ADD;
import static io.gaultier.controlledandroid.control.PendingOperationType.REMOVE_BACK;
import static io.gaultier.controlledandroid.control.PendingOperationType.REMOVE_REPLACE;

/**
 * Created by q on 16/10/16.
 */

public class AbstractController implements SubChangeListener {
    // getInstance all class transient

    static final String INVALID_CONTROLLER_ID = "0";
    static final String CONTROLLER_ID = "CONTROLLER_ID";
    static final String CONTROLLER = "CONTROLLER";


    @Transient
    private String controllerId = INVALID_CONTROLLER_ID;

    @Transient
    String debugmode;

    @Transient
    List<AbstractController> subControllers = new ArrayList<AbstractController>();

    @Transient
    private AbstractController parentController;

    @Transient
    private String previousId;

    @Transient
    private ControlledElement managedElement;

    String parentControllerId;
    boolean managed;
    boolean isInitialized;

    @Transient
    PendingOperation pendingOperation;


    @Transient
    private Collection<SubChangeListener> subChangeListeners = new LinkedHashSet<>();

    @Transient
    List<PreferenceManager.OnActivityResultListener> activityResultCallbacks = new ArrayList<>();

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (int i = activityResultCallbacks.size(); i --> 0;) {
            activityResultCallbacks.get(i).onActivityResult(requestCode, resultCode, data);
        }
    }

    public AbstractController() {
        subChangeListeners.add(this);
    }

    String getControllerId() {
        return controllerId;
    }

    void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public final String toString() {
        return "[" + blaze() + "." + controllerId + "<-" + parentControllerId + "]" + "~" + previousId;
    }

    private String blaze() {
        return getClass().getSimpleName().replaceAll("Controller", "").replace("Activity", "-A").replace("Fragment", "-F");
    }

    public List<AbstractController> snapSubControllers() {
        return new ArrayList<>(subControllers);
    }

    boolean isManaged() {
        return managed;
    }

    boolean hasId() {
        return isValidId(controllerId);
    }

    void cleanup() {
        boolean removed = parentController.subControllers.remove(this);
        Assert.ensure(removed, "cleaning up an unmanaged controller:" + this);
        parentController = null;
        //parentControllerId = null;
    }


    protected ControlledElement getManagedElement() {
        return managedElement;
    }

    <T extends AbstractController> void setManagedElement(ControlledElement<T> managedElement) {
        Assert.ensure(isManaged());
        this.managedElement = managedElement;
    }

    // cleanup all states from previous displays
    // the view is about to be displayed again
    public void reset() {
    }

    void setManaged(boolean managed) {
        this.managed = managed;
    }

    void ensureInitialized() {
        if (!isInitialized) {
            init();
            isInitialized = true;
        }
    }

    // first time this controller will be displayed
    protected void init() {

    }


    AbstractController getParentController() {
        return parentController;
    }

    void assignParentController(AbstractController p) {
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

    static boolean isValidId(String id) {
        return id != null && !INVALID_CONTROLLER_ID.equals(id) && Integer.parseInt(id) > 0;
    }

    String getPreviousId() {
        return previousId;
    }

    void setPreviousId(String previousId) {
        this.previousId = previousId;
    }

    boolean isOrphan() {
        return getParentController() == null;
    }

    boolean isAskRemove() {
        return isPending(REMOVE_REPLACE) || isPending(REMOVE_BACK);
    }

    boolean isAskBack() {
        return isPending(REMOVE_BACK);
    }

    boolean isAskReplace() {
        return isPending(REMOVE_REPLACE);
    }

    private boolean isPending(PendingOperationType removeReplace) {
        return pendingOperation != null && pendingOperation.type == removeReplace;
    }

    public void goBack() {
        assignPending(REMOVE_BACK, 0);
        notifyChange();
    }

    public void askReplace() {
        assignPending(REMOVE_REPLACE, 0);
    }

    protected void assignPending(PendingOperationType removeReplace, int addIn) {
        pendingOperation = new PendingOperation(removeReplace, addIn);
    }

    boolean isAskAdd() {
        return isPending(ADD);
    }

    public void askAddIn(int askAddIn) {
        assignPending(ADD, askAddIn);
    }

    public void unsetPending() {
        pendingOperation = null;
    }

    int getAddIn() {
        return pendingOperation.addIn;
    }

    public final String tag() {
        return getClass().getSimpleName();
    }

    protected AbstractController self() {
        return this;
    }

    //notify change to parent controller
    protected final void notifyChange() {
        AbstractController p = getParentController();
        if (p != null) {
            p.onSubChangeInternal(this);

            ControlledElement managedEl = p.getManagedElement();
            if (managedEl != null) {
                p.getManagedElement().refresh();
            }
            p.notifyChange();
        }
    }


    // one of my sub-controller tells me to check something
    @Override
    public void onSubChange(AbstractController subController) {

    }

    private void onSubChangeInternal(AbstractController subController) {
        for (SubChangeListener listener : snapSubListeners()) {
            listener.onSubChange(subController);
        }
    }

    private Collection<SubChangeListener> snapSubListeners() {
        return new LinkedHashSet<>(subChangeListeners);
    }

    public boolean addSubListeners(SubChangeListener listener) {
        return subChangeListeners.add(listener);
    }

    public boolean addOnActivityResultCallback(PreferenceManager.OnActivityResultListener listener) {
        return this.activityResultCallbacks.add(listener);
    }

    public boolean removeSubListener(SubChangeListener listener) {
        return subChangeListeners.remove(listener);
    }

    //check if intercepted by subcontroller first
    final boolean onBackPressed() {
        List<AbstractController> sub = snapSubControllers();
        for (int i = sub.size(); i --> 0;) {
            AbstractController el = sub.get(i);
            if (el.onBackPressed()) {
                Log.d(tag(), "Back intercepted by", el);
                return true;
            }
        }

        return getManagedElement().interceptBackPressed();
    }
}

