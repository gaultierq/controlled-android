package io.gaultier.controlledandroid.control;

/**
 * Created by q on 11/11/16.
 */
public abstract class AbstractFragmentController extends AbstractController {

    //true if this fragment is nested in another fragment
    boolean nestedFragment = false;

    protected boolean addToBackstack = true;


    public void askAddIn(int target, boolean nestedFragment) {
        this.nestedFragment = nestedFragment;
        askAddIn(target);
    }

    @Override
    public abstract ControlledFragment makeElement();
}
