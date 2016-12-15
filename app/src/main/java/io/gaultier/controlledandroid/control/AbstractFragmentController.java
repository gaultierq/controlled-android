package io.gaultier.controlledandroid.control;

import org.parceler.Parcel;

/**
 * Created by q on 11/11/16.
 */

@Parcel
public class AbstractFragmentController extends AbstractController {

    //true if this fragment is nested in another fragment
    boolean nestedFragment = false;

    protected boolean addToBackstack = true;

    protected int[] animation = new int[4];

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

}
