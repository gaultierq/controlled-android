package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledFragment<T extends AbstractFragmentController> extends Fragment implements ControlledElement<T> {


    private ControllerAccessor<T> ctrlAccessor = new ControllerAccessor<>();

    @Override
    public final void onCreate(Bundle savedInstanceState) {

        ctrlAccessor.obtain(this, savedInstanceState, getArguments());

        Assert.ensure(getActivity() instanceof ControlledActivity, "ControlledFragment can only exist in ControlledActivity");

        super.onCreate(savedInstanceState);

    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int theme = getController().callGetOverrideTheme();
        if (theme > 0) {
            inflater = inflater.cloneInContext(new ContextThemeWrapper(getActivity(), theme));
        }

        View view = createView(inflater, container);
        refreshInternal(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container);


    protected void refresh(View v) {
    }

    private void refreshInternal(View view) {
        ControllerManager.refreshPendings(getController());
        if (view != null //exemple: refreshing in createView
                && isAdded()
                ) {
            if (getController() == null) {
                Log.w(tag(), "skipping fragment refreshing with null controller");
            } else {
                Log.v(tag(), "refreshing");
                refresh(view);
            }

        } else {
            Log.w(tag(), "Skipping a refresh as the fragment is not added.");
        }
    }

    public final void refresh() {
        refreshInternal(getView());
    }


    protected void prepareView(View fragmentView, T fragmentController) {
    }

    @Override
    public String toString() {
        return ControllerManager.toString(this);
    }

    public T getController() {
        return ctrlAccessor.get();
    }

    public String getControllerId() {
        return ctrlAccessor.getId();
    }

    @Override
    public void onResume() {
        super.onResume();
        Assert.ensureNotNull(getController());

        // add comment pls
        //ctrlAccessor.bind(this);
        //getController().resetInternal();
        getController().onResume();
        prepareView(getView(), getController());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // saving controller state
        ControllerManager.saveController(outState, getController());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public final String tag() {
        return tag(getControllerId());
    }

    @NonNull
    public String tag(String id) {
        if (getTag() != null) {
            return getTag();
        }
        return getClass().getSimpleName() + "-" + id;
    }

    @Override
    public ControllerManager getManager() {
        return ((ControlledActivity) getActivity()).getManager();
    }

    public void setController(T controller) {
        ctrlAccessor.set(controller);
    }

    @Override
    public final T makeController() {
        T res = makeFragmentController();
        if (res.getParentController() == null) {
            res.assignParentController(((ControlledActivity) getActivity()).getController());
        }
        return res;
    }

    public abstract T makeFragmentController();


    public ControlledActivity getControlledActivity() {
        return (ControlledActivity) getActivity();
    }

    protected ControlledFragment<T> self() {
        return this;
    }

    protected ControlledActivity activity() {
        return (ControlledActivity) getActivity();
    }

    @Override
    public boolean interceptBackPressed() {
        if (!isAdded()) {
            Log.w(TAG, "Fragment not added, interceptBackPressed skipped");
            return false;
        }
        if (!getController().addToBackstack) {
            Log.i(TAG, "Fragment not in backstack, not intercepting back");
            return false;
        }
        getController().goBack();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getController().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public final FragmentManager obtainFragmentManager(AbstractController child) {
        //TODO : all fragment should extend AFC
        if (child instanceof AbstractFragmentController) {
            if (((AbstractFragmentController) child).nestedFragment) {
                return getChildFragmentManager();
            }
        }
        return getFragmentManager();
    }

    //HACK regarding the thread:
    // http://stackoverflow.com/questions/14900738/nested-fragments-disappear-during-transition-animation
    private static final Animation dummyAnimation = new AlphaAnimation(1, 1);

    static {
        dummyAnimation.setDuration(500);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter && getParentFragment() != null && nextAnim <= 0) {
            return dummyAnimation;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getController().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


