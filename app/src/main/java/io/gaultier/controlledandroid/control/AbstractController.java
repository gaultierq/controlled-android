package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import org.parceler.Transient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.PendingOperationType.ADD;
import static io.gaultier.controlledandroid.control.PendingOperationType.REMOVE_BACK;
import static io.gaultier.controlledandroid.control.PendingOperationType.REMOVE_REPLACE;

/**
 * Created by q on 16/10/16.
 */

public abstract class AbstractController {
    // getInstance all class transient

    static final String INVALID_CONTROLLER_ID = "0";
    static final String CONTROLLER_ID = "CONTROLLER_ID";
    static final String CONTROLLER = "CONTROLLER";
    public static final String TAG = "AbstractController";

    @Transient
    private String controllerId = INVALID_CONTROLLER_ID;

    @Transient
    String debugmode;

    @Transient
    List<AbstractController> subControllers = new ArrayList<>();

    //can be null, when app is recovering from being killed. The parent is still in the
    //savedInstanceState of the parent activity
    @Transient
    private AbstractController parentController;

    @Transient
    private String previousId;

    @Transient
    //private WeakReference<ControlledElement> managedElement = new WeakReference<ControlledElement>(null);
    private ControlledElement managedElement;

    String parentControllerId;

    boolean managed;

    @Transient
    boolean isInitialized;

    AtomicBoolean refreshing = new AtomicBoolean();

    @Transient
    private EventBusImplem bus;

    public <T extends AbstractController> T withAnimation(int[] animation) {
        this.animation = animation;
        return (T) this;
    }

    //enter, exit, popEnter, popExit;
    protected int[] animation = new int[4];

    @Transient
    PendingOperation pendingOperation;


    @Transient
    List<OnActivityResultListener> activityResultCallbacks = new ArrayList<>();

    @Transient
    List<OnRequestPermissionsResultCallback> requestPermissionsResultCallbacks = new ArrayList<>();

    @Transient
    List<OnResumeCallback> onResumeCallbackCallbacks = new ArrayList<>();

    //theme override
    protected int overrideTheme;

    static <T extends AbstractController> void update(final T controller, final Runnable runMe) {
        if (controller == null) {
            Log.w(TAG, "skipping refresh: null controller");
        }
        else if (controller.refreshing.compareAndSet(false, true)) {

            controller.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(controller.tag(), "refreshing");
                    ControllerManager.refreshPendings(controller);

                    runMe.run();

                    controller.refreshing.set(false);
                }
            });
        }
        else {
            Log.w(controller.tag(), "Already refreshing. Skipping nested refresh.");
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (int i = activityResultCallbacks.size(); i --> 0;) {
            activityResultCallbacks.get(i).onActivityResult(getManagedElement(), requestCode, resultCode, data);
        }
    }

    public void onResume() {

        //FIXME: remove !
        for (int i = onResumeCallbackCallbacks.size(); i --> 0;) {
            OnResumeCallback r = onResumeCallbackCallbacks.get(i);
            r.onResume();
        }

        refreshElement();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = requestPermissionsResultCallbacks.size(); i --> 0;) {
            requestPermissionsResultCallbacks.get(i).onRequestPermissionsResult(getManagedElement().getControlledActivity(), requestCode, permissions, grantResults);
        }
    }

    public AbstractController() {
    }

    public String getControllerId() {
        return controllerId;
    }

    void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public String toString() {
        return "[" + blaze() + "." + controllerId + ", p=" + parentControllerId + "]" + (previousId != null ? "~" + previousId : "") ;
    }

    private String blaze() {
        return getClass().getSimpleName().replaceAll("Controller", "").replace("Activity", "-A").replace("Fragment", "-F");
    }

    public List<AbstractController> snapSubControllers() {
        return new ArrayList<>(subControllers);
    }

    //return the first matching subcontroller
    public <T extends AbstractController> T getSubController(Class<T> clazz) {
        List<AbstractController> subCon = snapSubControllers();
        return ControllerUtil.getByClass(clazz, subCon);
    }

    //return the first matching subcontroller
    public <T extends AbstractController> T getSubController(String subId) {
        List<AbstractController> subCon = snapSubControllers();
        for (AbstractController sub : subCon) {
            if (sub != null && subId.equals(sub.getControllerId())) {
                return (T) sub;
            }
        }
        return null;
    }

    public boolean isManaged() {
        return managed;
    }

    boolean hasId() {
        return isValidId(controllerId);
    }

    void cleanupInternal() {
        if (parentController != null) {
            cleanupParentController();
        }
        else {
            Log.w(TAG, "missed cleanup of", this);
        }
        cleanup();
    }

    private void cleanupParentController() {
        if (parentController != null) {
            boolean removed = parentController.subControllers.remove(this);
            Assert.ensure(removed, "cleaning up an unmanaged controller:" + this);
            parentController = null;
        }
    }

    protected void cleanup() {
        //you can for eg. clear all listeners here
    }


    public ControlledElement getManagedElement() {
        return managedElement;
    }

    public ControlledActivity getActivity() {
        if (managedElement == null) return null;
        return managedElement.getControlledActivity();
    }

    <T extends AbstractController> void setManagedElement(ControlledElement<T> managedElement) {
        Assert.ensure(isManaged());
        this.managedElement = managedElement;
    }


    void setManaged(boolean managed) {
        this.managed = managed;
    }

    void ensureInitialized() {
        if (isInitialized) return;
        init();
        isInitialized = true;
    }

    // intialize the controller, make the 1st requests & prepare the data
    protected void init() {
    }

    AbstractController getParentController() {
        return parentController;
    }

    public void assignParentController(AbstractController p) {
        if (parentController == p) return;
        if (parentController != null && p != null) {
            Log.w(tag(), "Changing parent on", this, "old=", parentController, "new=", p);
        }

        cleanupParentController();

        this.parentController = p;
        if (parentController != null) {
            p.subControllers.add(this);
            parentControllerId = p.getControllerId();
        }
        else {
            parentControllerId = "-1";
            Log.e("Manager", "null parent for", this);
        }
        ensureNoCycle();
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

    public void askRemove() {
        assignPending(REMOVE_BACK, 0);
    }

    private void assignPending(PendingOperationType removeReplace, int addIn) {
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

    @NonNull
    public final String tag() {
        return getClass().getSimpleName();
    }

    protected AbstractController self() {
        return this;
    }

    //notify change to parent controller
    //TODO: protected
    public final void notifyChange() {
        publishEvent(new ControllerStructureEvent(this));
    }


    //1st notified is parent
    public final void publishEvent(Object event) {
        publishEventOn(getParentController(), event);
    }

    private static void publishEventOn(AbstractController p, Object event) {
        Log.v(TAG, "publishing", event, "on", p);
        if (p != null) {
            AbstractController parent = p.getParentController();

            //consumption finish activity, but the event should dispatch to parent anyway
            // => keep parent reference before onEvent
            boolean consumed = p.onEventInternal(event);
            if (!consumed) {
                publishEventOn(parent, event);
            }
        }
    }

    public boolean isLinked() {
        if (parentController != null) return parentController.isLinked();
        return false;
    }

    void ensureNoCycle() {
        HashSet<Object> already = new HashSet<>();
        AbstractController c = this;
        do {
            Assert.ensure(already.add(c), String.format("cycle detected: for controller %s", c));
            c = c.getParentController();
        } while (c != null);

    }

    public interface EventBus {

        void publishEvent(Object event);


        void registerListener(EventListener listener);

    }

    public interface EventListener {

        boolean onEvent(Object event);
    }

    public EventBus emitBus() {
        if (bus != null) return bus;
        return bus = new EventBusImplem();
    }

    // one of my sub-controller is notifying me
    //return: consumed
    boolean onEventInternal(Object event) {
        //internal stuff
        if (event instanceof ControllerStructureEvent) {
            ControllerManager.refreshPendings(this);
            return false;
        }
        if (bus != null) {
            bus.onEvent(event);
        }
        return onEvent(event);
    }

    // one of my sub-controller is notifying me
    //return: consumed
    protected boolean onEvent(Object event) {
        return false;
    }


    public interface OnActivityResultListener {

        boolean onActivityResult(ControlledElement activity, int requestCode, int resultCode, Intent data);
    }

    public interface OnRequestPermissionsResultCallback {

        void onRequestPermissionsResult(
                ControlledActivity activity,
                int requestCode,
                @NonNull String[] permissions,
                @NonNull int[] grantResults);
    }

    public interface OnResumeCallback<T extends AbstractController> {

        void onResume();
    }

    public boolean addOnActivityResultCallback(OnActivityResultListener listener) {
        return this.activityResultCallbacks.add(listener);
    }

    public boolean removeOnActivityResultCallback(OnActivityResultListener listener) {
        return activityResultCallbacks.remove(listener);
    }

    //TODO: rm
    public boolean addOnRequestPermissionsResultCallback(OnRequestPermissionsResultCallback listener) {
        return this.requestPermissionsResultCallbacks.add(listener);
    }

    public boolean addOnResumeCallback(OnResumeCallback listener) {
        return this.onResumeCallbackCallbacks.add(listener);
    }

    public boolean removeOnRequestPermissionsResultCallback(OnRequestPermissionsResultCallback listener) {
        return requestPermissionsResultCallbacks.remove(listener);
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

        ControlledElement element = getManagedElement();
        if (element == null) return false;
        return element.interceptBackPressed();
    }

    public abstract ControlledElement makeElement();

    //TODO: bug: when parent is a fragment, I need to call refresh to have the new fragment displayed
    //utility method to add a fragment
    public void addFragment(AbstractFragmentController controller, int target, boolean addToBackStack, boolean nestedFragment) {
        controller.askAddIn(target, nestedFragment);
        controller.addToBackstack = addToBackStack;
        ControllerManager instance = ControllerManager.getInstance();
        instance.manageNewFragment(controller, this);
        controller.notifyChange();
    }

    public void refreshElement() {
        ControlledElement el = getManagedElement();
        if (el != null) {
            el.refresh();
        }
    }


    public int callGetOverrideTheme() {
        return overrideTheme > 0 ? overrideTheme : callGetOverrideTheme(getParentController());
    }

    private static int callGetOverrideTheme(AbstractController p) {
        if (p != null) {
            return p.callGetOverrideTheme();
        }
        return 0;
    }

    public int getOverrideTheme() {
        return overrideTheme;
    }

    public AbstractController setOverrideTheme(@StyleRes int overrideTheme) {
        this.overrideTheme = overrideTheme;
        return this;
    }

    private class EventBusImplem implements EventBus {

        List<WeakReference<EventListener>> listeners = new ArrayList<>();

        WeakReference<AbstractController> ref = new WeakReference<>(AbstractController.this);

        @Override
        public void publishEvent(Object event) {
            AbstractController c = ref.get();
            if (c == null) return;

            publishEventOn(c, event);
        }

        @Override
        public void registerListener(EventListener listener) {
            Iterator<WeakReference<EventListener>> iterator = listeners.listIterator();
            while (iterator.hasNext()) {
                WeakReference<EventListener> l = iterator.next();
                EventListener c;
                if (l == null || (c = l.get()) == null) {
                    iterator.remove();
                    continue;
                }
                if (c == listener) return;
            }
            listeners.add(new WeakReference<>(listener));
        }

        public boolean onEvent(Object event) {
            for (WeakReference<EventListener> listener : listeners) {
                EventListener l = listener.get();
                if (l != null) {
                    boolean consumed = l.onEvent(event);
                    if (consumed) return true;
                }
            }
            return false;
        }
    }
}

