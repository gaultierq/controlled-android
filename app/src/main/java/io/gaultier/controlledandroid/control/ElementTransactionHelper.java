package io.gaultier.controlledandroid.control;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import io.gaultier.controlledandroid.util.Assert;

/**
 * Created by q on 01/11/16.
 */

public class ElementTransactionHelper {


    private static final String TAG = "ElementTransactionHelper";
    private final ControlledElement parentEl;
    private FragmentTransaction fragmentTransaction;
    private FragmentManager manager;
    int[] animation = new int[4];

    public ElementTransactionHelper(ControlledElement parentEl) {
        this.parentEl = parentEl;
    }

    protected void addSub(ControlledElement managedElement, int addIn) {
        if (managedElement instanceof ControlledActivity) {
            parentEl.getManager().startActivity(parentEl.getControlledActivity(), ((ControlledActivity) managedElement).getClass(), managedElement.getController());
        } else if (managedElement instanceof ControlledFragment) {
            ControlledFragment f = ((ControlledFragment) managedElement);
            FragmentTransaction trans = obtainOpenedFragmentTrans();

            int[] anim = f.getAnimation();
            animation[0] = anim[0];
            animation[3] = anim[3];
            makeAnimation(trans);

            trans.add(addIn, f, f.tag());
            if (f.shouldAddToBackStack()) {
                trans.addToBackStack(f.tag());
            }
        } else {
            Assert.thrown("" + managedElement);
        }
    }

    // fragment was added to the stack, then we need to remove it from the stack
    protected void removeManagedElement(ControlledElement managedElement) {
        if (managedElement instanceof ControlledActivity) {
            ((ControlledActivity) managedElement).finish();
        } else if (managedElement instanceof ControlledFragment) {
            ControlledFragment f = ((ControlledFragment) managedElement);


            AbstractController c = f.getController();
            if (c.isAskReplace()) {
                FragmentTransaction trans = obtainOpenedFragmentTrans();
                int[] anim = f.getAnimation();
                animation[1] = anim[1];
                animation[2] = anim[2];
                makeAnimation(trans);
                trans.remove(f);
                trans.addToBackStack(f.tag());

            } else if (c.isAskBack()) {
                c.getManagedElement().getManager().unmanage(c);
                obtainerManager().popBackStack();
            } else {
                Assert.thrown();
            }
        } else {
            Assert.thrown();
        }
    }

    private void makeAnimation(FragmentTransaction trans) {
        trans.setCustomAnimations(animation[0], animation[1], animation[2], animation[3]);
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
