package io.gaultier.controlledandroid.control;

/**
 * Created by q on 11/11/16.
 */
public abstract class AbstractFragmentController extends AbstractController {

    //true if this fragment is nested in another fragment
    boolean nestedFragment = false;

    protected boolean addToBackstack = true;

    //fragment theme
    protected int overrideTheme;


    public void askAddIn(int target, boolean nestedFragment) {
        this.nestedFragment = nestedFragment;
        askAddIn(target);
    }

    public int callGetOverrideTheme() {
        return overrideTheme > 0 ? overrideTheme : callGetOverrideTheme(getParentController());
    }

    private static int callGetOverrideTheme(AbstractController p) {
        if (p != null && p instanceof AbstractFragmentController) {
            return ((AbstractFragmentController) p).callGetOverrideTheme();
        }
        return 0;
    }


    @Override
    public abstract ControlledFragment makeElement();
}
