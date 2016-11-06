package io.gaultier.controlledandroid.control;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

/**
 * Created by q on 01/11/16.
 */

public class ElementTransactionHelper {


    private static final String TAG = "ElementTransactionHelper";
    private final ControlledElement parentEl;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager manager;

    public ElementTransactionHelper(ControlledElement parentEl) {
        this.parentEl = parentEl;
    }

    public enum FragTransactionOperation {
        ADD,
        REMOVE
    }

    protected void addSub(ControlledElement managedElement, int addIn) {
        if (managedElement instanceof ControlledActivity) {
            parentEl.getManager().startActivity(parentEl.getControlledActivity(), ((ControlledActivity) managedElement).getClass(), managedElement.getController());
        } else if (managedElement instanceof ControlledFragment) {
            ControlledFragment f = ((ControlledFragment) managedElement);
            FragmentTransaction trans = obtainOpenedFragmentTrans();

            int[] anim = f.getAnimation();
            animation[0] = anim == null ? 0 : anim[0];
            makeAnimation(trans);

            trans.add(addIn, f, f.tag());
            if (f.shouldAddToBackStack(FragTransactionOperation.ADD)) {
                trans.addToBackStack(f.tag());
            }
        } else {
            Assert.thrown("" + managedElement);
        }
    }

    int[] animation = new int[2];


    //TODO: debug this
    // fragment was added to the stack, then we need to remove it from the stack
    // possible bug 1 : displaying the previous fragment in the saved state, and we have no way no tell it its displayed again
    // possible bug 2 : the popped fragment is not the good one
    protected void removeManagedElement(ControlledElement managedElement) {
        if (managedElement instanceof ControlledActivity) {
            ((ControlledActivity) managedElement).finish();
        } else if (managedElement instanceof ControlledFragment) {
            ControlledFragment f = ((ControlledFragment) managedElement);


            if (f.shouldAddToBackStack(FragTransactionOperation.REMOVE)) {
                FragmentTransaction trans = obtainOpenedFragmentTrans();
                int[] anim = f.getAnimation();
                animation[1] = anim == null ? 0 : anim[1];
                makeAnimation(trans);
                trans.remove(f);
                trans.addToBackStack(f.tag());

            } else if (f.shouldAddToBackStack(FragTransactionOperation.ADD)) {
                //trans.shouldAddToBackStack(f.tag());
                FragmentManager.BackStackEntry entry = obtainerManager().getBackStackEntryAt(obtainerManager().getBackStackEntryCount() - 1);
                Log.d(TAG, "Poping back fragment ", f, " with result:", entry);
                obtainerManager().popBackStack();
            } else {
                Assert.thrown();
            }
        } else {
            Assert.thrown();
        }
    }

    private void makeAnimation(FragmentTransaction trans) {
        trans.setCustomAnimations(animation[0], animation[1]);
    }

    private FragmentTransaction obtainOpenedFragmentTrans() {
        if (fragmentTransaction == null) {
            fragmentTransaction = obtainerManager().beginTransaction();
        }
        return fragmentTransaction;
    }

    private FragmentManager obtainerManager() {
        if (manager == null) {
            manager = parentEl.getControlledActivity().getSupportFragmentManager();
        }
        return manager;
    }

    public void commit() {
        if (fragmentTransaction != null) {
            fragmentTransaction.commit();
        }
    }
}
