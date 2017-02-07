package io.gaultier.controlledandroid.control;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;
import static io.gaultier.controlledandroid.control.AbstractController.isValidId;

/**
 * Created by q on 16/10/16.
 */

public class ControllerManager {

    private static final String TAG = "ControllerManager";

    private static ControllerManager INSTANCE;

    private final LinkedHashMap<String, AbstractController> managedControllers = new LinkedHashMap<>();

    private final AtomicInteger counter = new AtomicInteger(-1);

    private final int session;

    private final AbstractController mainController;

    private ControllerManager(int oldSessionId, int sessionId) {
        Log.i(TAG, "Creating instance");
        mainController = new ApplicationController();
        session = sessionId;
        mainController.setPreviousId("" + oldSessionId);
        manageAndAssignParent(mainController, new ApplicationController());
    }

    public static void init(Context context) {
        int oldSessionId = PreferenceManager.getDefaultSharedPreferences(context).getInt("sessionId", 0);
        int sessionId = new Random().nextInt(100000);//oldSessionId + 1;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("sessionId", sessionId).apply();
        Assert.ensure(INSTANCE == null, "ControllerManager already init");
        INSTANCE = new ControllerManager(oldSessionId, sessionId);
    }

    public static ControllerManager getInstance() {
        if (INSTANCE == null) throw new RuntimeException("ControllerManager not initialized");
        return INSTANCE;
    }

    //managed elements asked for display / removal
    protected static <U extends AbstractController> void refreshPendings(U controller) {

        ControlledElement parentEl = controller.getManagedElement();

        //main controller
        if (parentEl == null) return;

        if (!parentEl.isViewCreated()) {
            Log.w(TAG, "trying to refresh pendings fragments when parent view is not ready yet is a bad idea");
            return;
        }

        FragmentTransactionHelper helper = new FragmentTransactionHelper(parentEl);

        List<AbstractFragmentController> controllersToRemove = new ArrayList<>();
        List<AbstractFragmentController> controllersToAdd = new ArrayList<>();
        for (AbstractController sub : controller.snapSubControllers()) {
            if (sub instanceof AbstractFragmentController) {
                AbstractFragmentController c = (AbstractFragmentController) sub;

                if (c.isAskRemove()) {
                    controllersToRemove.add(c);
                } else if (c.isAskAdd()) {
                    controllersToAdd.add(c);
                }
            }

        }

        for (AbstractFragmentController rm : controllersToRemove) {
            helper.removeManagedElement(rm);
            rm.unsetPending();
        }
        for (AbstractFragmentController ad : controllersToAdd) {
            helper.add(ad);
            ad.unsetPending();
        }
        helper.commit();
    }

    //this is where all the complexity is handled
    @NonNull
    static <T extends AbstractController> T obtainIt(ControlledElement<T> element, Bundle savedInstanceState, Bundle arguments) {
        ControllerManager manager = ControllerManager.getInstance();
        String controllerId;
        T controller;
        if (isValidId(controllerId = readControllerId(savedInstanceState))) {
            // fragment has already existed (rotation, restore, killed)
            // -> restore controller
            //eg: rotation
            if (manager.isManaged(controllerId)) {
                controller = (T) manager.getManagedController(controllerId);
                controller.debugmode = "saved-managed";
            } else { //killed
                controller = manager.restoreController(savedInstanceState);
                controller.debugmode = "saved-killed";
                manager.manage(controller);
            }
        } else if (isValidId(controllerId = readControllerId(arguments))) {
            // fragment created programatically (already managed)
            // -> find controller
            Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller for id=" + controllerId + ", arguments=" + arguments);
            controller = (T) manager.getManagedController(controllerId);
            Assert.ensure(!controller.isOrphan(), "unexpected orphan:" + controller);

            controller.debugmode = "from args";
        } else {
            // creation by system (main activity, fragment)
            // -> create new controller (should be the only controller factory)
            controller = element.makeController();
            Assert.ensure(controller != null);
            Assert.ensure(controller.getParentController() != null, "This controller should have a parent controller.");
            manager.manage(controller);
            controller.debugmode = "from factory";
        }

        // binding
        controller.setPreviousId(controllerId);
        if (!manager.assignParent(controller)) {
            manager.runParentNet();
        }
        if (controller.getParentController() == null) {
            Log.w(TAG, "No parent linked to", controller, "for parent id", controller.parentControllerId, "when mode was", controller.debugmode);
        }
        Assert.ensure(controller.isManaged());
        controller.setManagedElement(element);
        return controller;
    }


    private <T extends AbstractController> boolean assignParent(T controller) {
        if (controller.isOrphan()) {
            AbstractController parent = findParent(controller);
            if (parent != null) {
                controller.assignParentController(parent);
                return true;
            }
        }
        return false;
    }

    private void runParentNet() {
        //check
        int o = logOrphanCount();
        for (AbstractController c : managedControllers.values()) {
            if (this.assignParent(c)) {
                Log.w(TAG, "The net has caught an orphan");
            }
        }
        int n = logOrphanCount();
        if (o != n) {
            Log.w(TAG, n, "orphans");
        }
    }

    private int logOrphanCount() {
        int orphan = 0;
        for (AbstractController c : managedControllers.values()) {
            if (c.getParentController() == null) {
                orphan++;
            }
        }
        return orphan;
    }

    private AbstractController findParent(AbstractController controller) {
        AbstractController parent = controller.getParentController();
        if (parent == null) {
            //lets find your parent
            String id = controller.parentControllerId;
            Assert.ensure(isValidId(id), "unexpected id " + id + " for controller " + controller);
            for (AbstractController c : managedControllers.values()) {
                if (id.equals(c.getControllerId()) || id.equals(c.getPreviousId())) {
                    Assert.ensure(parent == null);
                    parent = c;
                }
            }
        }

        return parent;
    }

    private static String readControllerId(Bundle bundle) {
        if (bundle != null) {
            return bundle.getString(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
        }
        return INVALID_CONTROLLER_ID;
    }

    @NonNull
    static String toString(ControlledElement el) {
        return "[" + "controlled:" + el.getClass().getSimpleName() + "." + el.getControllerId() + "]";
    }

    public String toString() {
        return "[" + "manager [" + toString(managedControllers) + "]";
    }

    private <K, V> String toString(LinkedHashMap<K, V> managedControllers) {
        if (managedControllers == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder("size=" + managedControllers.size() + " {");
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(managedControllers.entrySet());

        ListIterator<Map.Entry<K, V>> it = entries.listIterator(entries.size());

        while (it.hasPrevious()) {
            Map.Entry<K, V> e = it.previous();
            b.append(e.getValue());
            if (it.hasPrevious()) {
                b.append(", ");
            }
        }
        b.append("}");
        return b.toString();
    }


    @NonNull
    protected <C extends AbstractActivityController> Intent makeIntent(Context from, C controllerToLaunch, AbstractController parent) {
        Class<? extends ControlledActivity> clazz = controllerToLaunch.makeElement().getClass();

        Intent intent = new Intent(from, clazz);
        //managing the current controller if needed
        manageAndPutExtras(controllerToLaunch, parent, intent);
        return intent;
    }

    <C extends AbstractActivityController> void manageAndPutExtras(C controllerToPrepare, AbstractController parent, Intent intent) {
        boolean wasManaged = manageAndAssignParent(controllerToPrepare, parent);
        if (wasManaged) {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        intent.putExtras(ControllerManager.saveController(new Bundle(), controllerToPrepare));
    }

    protected <F extends ControlledActivity, C extends AbstractActivityController> void overridePendingTransition(F fromActivity, int i, int i1) {
        int enterAnim = i;
        int exitAnim = i1;

        if (enterAnim > 0 || exitAnim > 0) {
            fromActivity.overridePendingTransition(enterAnim, exitAnim);
        }
    }

    public <U extends AbstractFragmentController, T extends ControlledFragment<U>> T manageNewFragment(U fragmentController, AbstractController parent) {

        //TODO: stink
        if (!fragmentController.hasId()) {
            manageAndAssignParent(fragmentController, parent);
        }

        // this is a test, to let the parent controller element find frag, and add it
        T fragment = (T) fragmentController.makeElement();
        fragmentController.setManagedElement(fragment);
        fragment.setArguments(ControllerManager.saveController(new Bundle(), fragmentController));
        return fragment;
    }

    public void unmanage(AbstractController controller) {
        unmanage(controller.snapSubControllers());

        String id = controller.getControllerId();
        Assert.ensure(isValidId(id), "Unexpected. " + controller + " in " + this);
        AbstractController unmanaged = managedControllers.remove(id);
        Assert.ensure(unmanaged == controller, "Controller not found:" + controller + " in " + this);

        unmanaged.cleanupInternal();
        unmanaged.setManaged(false);
        Log.i(TAG, "--: ", controller, "manager:", this);
    }

    private void unmanage(Collection<AbstractController> controllers) {
        for (AbstractController ctrl : controllers) {
            unmanage(ctrl);
        }
    }

    private <T extends AbstractController> void manage(T controller) {
        Assert.ensure(AbstractController.INVALID_CONTROLLER_ID.equals(controller.getControllerId()), "" + controller);
        controller.setControllerId("" + generateControllerId());
        managedControllers.put(controller.getControllerId(), controller);
        controller.setManaged(true);
        Log.i(TAG, "++: ", "manager:", this);
    }

    // manageAndAssignParent provide an id to the controller
    private <T extends AbstractController> boolean manageAndAssignParent(T controller, AbstractController rootParentController) {

        List<AbstractController> ctrlChain = new ArrayList<>();

        AbstractController p = controller;
        while (p != null) {
            if (ctrlChain.contains(p)) break; //avoid cycles
            if (p == rootParentController) break;
            ctrlChain.add(p);
            p = p.getParentController();
        }

        Collections.reverse(ctrlChain);

        boolean was = true;

        p = rootParentController;

        for (AbstractController c : ctrlChain) {
            if (c.getParentController() != p) {
                c.assignParentController(p);
            }
            if (!c.isManaged()) {
                manage(c);
                if (controller == c) was = false;
            }
            p = c;
        }
//        if (!controller.isManaged()) {
//            controller.assignParentController(rootParentController);
//            manage(controller);
//            was = false;
//        }
        return was;
    }

    public void dumpAll() {
        Set<AbstractController> already = new HashSet<>();
        for (Map.Entry<String, AbstractController> entry : managedControllers.entrySet()) {
            AbstractController v = entry.getValue();
            dump(v, already, "managed");
        }
        dump(mainController, new HashSet<AbstractController>(), "root");
    }


    protected void dump(AbstractController v, Set<AbstractController> already, String tag) {
        if (v == null) return;
        if (already.add(v)) {
            Log.d(TAG, "dump", tag, ": ", v, "element=", v.getManagedElement());
            List<AbstractController> sub = v.snapSubControllers();
            for (AbstractController s : sub) {
                dump(s, already, tag);
            }
        }
    }

    @Nullable
    public AbstractController getManagedController(String controllerId) {
        return managedControllers.get(controllerId);
    }

    @Nullable
    public <T extends AbstractController> T getManagedController(Class<T> controllerId) {
        List<AbstractController> values = new ArrayList<>(managedControllers.values());
        return ControllerUtil.getByClass(controllerId, values);
    }

    private boolean isManaged(String controllerId) {
        AbstractController managedController = getManagedController(controllerId);
        if (managedController != null) {
            Assert.ensure(managedController.isManaged());
            return true;
        }
        return false;
    }

    private <T extends AbstractController> T restoreController(Bundle savedInstanceState) {
        Parcelable wrappedController = savedInstanceState.getParcelable(AbstractController.CONTROLLER);
        T controller = Parcels.unwrap(wrappedController);
        Assert.ensure(controller != null);
        return controller;
    }

    private int generateControllerId() {
        return session + counter.incrementAndGet();

    }

    static <T extends AbstractController> Bundle saveController(Bundle outState, T controller) {
        outState.putString(AbstractController.CONTROLLER_ID, controller.getControllerId());
        outState.putParcelable(AbstractController.CONTROLLER, Parcels.wrap(controller));
        return outState;
    }

    AbstractController getMainController() {
        return mainController;
    }

    private static class ApplicationController extends AbstractController {
        @Override
        public ControlledElement makeElement() {
            return null;
        }

        @Override
        protected boolean onEvent(Object event) {
            return true;
        }

        @Override
        boolean onEventInternal(Object event) {
            super.onEventInternal(event);
            return true;
        }
    }
}


