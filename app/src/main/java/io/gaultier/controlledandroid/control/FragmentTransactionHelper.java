package io.gaultier.controlledandroid.control;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import io.gaultier.controlledandroid.util.Assert;

/**
 * Created by q on 01/11/16.
 */

public class FragmentTransactionHelper {


    private static final String TAG = "FragmentTransactionHelper";
    private final ControlledElement parentEl;
    private FragmentTransaction fragmentTransaction;
    int[] animation = new int[4];

    public FragmentTransactionHelper(ControlledElement parentEl) {
        this.parentEl = parentEl;
    }

    protected void add(AbstractFragmentController c) {
        ControlledElement managedElement = c.getManagedElement();
        ControlledFragment f = ((ControlledFragment) managedElement);
        FragmentTransaction trans = obtainOpenedFragmentTrans(c);

        int[] anim = c.animation;
        animation[0] = anim[0];
        animation[3] = anim[3];
        makeAnimation(trans);


        String tag = f.tag(c.getControllerId());
        trans.add(c.getAddIn(), f, tag);
        if (c.addToBackstack) {
            trans.addToBackStack(tag);
        }
    }

    // fragment was added to the stack, then we need to remove it from the stack
    protected void removeManagedElement(AbstractFragmentController child) {
        ControlledFragment f = ((ControlledFragment) child.getManagedElement());

        if (child.isAskReplace()) {
            FragmentTransaction trans = obtainOpenedFragmentTrans(child);
            int[] anim = child.animation;
            animation[1] = anim[1];
            animation[2] = anim[2];
            makeAnimation(trans);
            trans.remove(f);
            trans.addToBackStack(f.tag(child.getControllerId()));

        }
        else if (child.isAskBack()) {
            child.getManagedElement().getManager().unmanage(child);
            //this is completely hacky. TODO: die
            //remove all childs
            FragmentManager cm = f.getChildFragmentManager();
            while (cm.getBackStackEntryCount() > 0) {
                cm.popBackStackImmediate();
            }
            //remove 1st
            FragmentManager fragmentManager = getFragmentManager(child);
            fragmentManager.popBackStack();

        } else {
            Assert.thrown();
        }

    }

    private void makeAnimation(FragmentTransaction trans) {
        trans.setCustomAnimations(animation[0], animation[1], animation[2], animation[3]);
    }

    private FragmentTransaction obtainOpenedFragmentTrans(AbstractController managedElement) {
        if (fragmentTransaction == null) {
            fragmentTransaction = getFragmentManager(managedElement).beginTransaction();
        }
        return fragmentTransaction;
    }

    private FragmentManager getFragmentManager(AbstractController child) {
        return parentEl.obtainFragmentManager(child);
    }

    public void commit() {
        if (fragmentTransaction != null) {
            fragmentTransaction.commit();
        }
    }
}
