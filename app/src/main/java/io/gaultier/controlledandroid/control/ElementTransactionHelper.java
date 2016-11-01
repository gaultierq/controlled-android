package io.gaultier.controlledandroid.control;

import android.support.v4.app.FragmentTransaction;

import io.gaultier.controlledandroid.util.Assert;

/**
 * Created by q on 01/11/16.
 */

public class ElementTransactionHelper {


    private final ControlledElement parentEl;
    private FragmentTransaction fragmentTransaction;

    public ElementTransactionHelper(ControlledElement parentEl) {
        this.parentEl = parentEl;
    }

    protected void addSub(ControlledElement managedElement, int addIn) {
        if (managedElement instanceof ControlledActivity) {
            parentEl.getManager().startActivity(parentEl.getControlledActivity(), ((ControlledActivity) managedElement).getClass(), managedElement.getController());
        } else if (managedElement instanceof ControlledFragment) {
            ControlledFragment f = ((ControlledFragment) managedElement);
            FragmentTransaction trans = obtainOpenedFragmentTrans();
            trans.add(addIn, f, f.tag());
            if (f.addToBackStack(ControlledFragment.FragTrans.ADD)) {
                trans.addToBackStack(f.tag());
            }
        } else {
            Assert.thrown("" + managedElement);
        }
    }

    protected void removeManagedElement(ControlledElement managedElement) {
        if (managedElement instanceof ControlledActivity) {
            ((ControlledActivity) managedElement).finish();
        } else if (managedElement instanceof ControlledFragment) {
            ControlledFragment f = ((ControlledFragment) managedElement);
            FragmentTransaction trans = obtainOpenedFragmentTrans();
            trans.remove(f);
            if (f.addToBackStack(ControlledFragment.FragTrans.REMOVE)) {
                trans.addToBackStack(f.tag());
            }

        } else {
            Assert.thrown();
        }
    }

    private FragmentTransaction obtainOpenedFragmentTrans() {
        if (fragmentTransaction == null) {
            fragmentTransaction = parentEl.getControlledActivity().getSupportFragmentManager().beginTransaction();
        }
        return fragmentTransaction;
    }

    public void commit() {
        if (fragmentTransaction != null) {
            fragmentTransaction.commit();
        }
    }
}
