package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledDialogFragment<T extends AbstractFragmentController> extends DialogFragment
        implements ControlledElement<T> {


    private ControllerAccessor<T> ctrlAccessor = new ControllerAccessor<>();
    private boolean viewCreated;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        boolean obtained = ctrlAccessor.obtain(this, savedInstanceState, getArguments());

        Assert.ensure(getActivity() instanceof ControlledActivity, "ControlledFragment can only exist in ControlledActivity");

        super.onCreate(savedInstanceState);

        if (!obtained) {
            Log.w(tag(), "impossible to obtain controller, finishing activity");
            getActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int theme = getController().callGetOverrideTheme();
        if (theme > 0) {
            inflater = inflater.cloneInContext(new ContextThemeWrapper(getActivity(), theme));
        }

        View view = createView(inflater, container, savedInstanceState);
        refreshInternal(view);
        viewCreated = true;
        return view;
    }

    protected abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState);


    protected void refresh(View v) {
    }

    private void refreshInternal(final View view) {
        //exemple: refreshing in createView

        if (view != null && isAdded()) {
            if (getController().isAskRemove()) {
                super.dismiss();
                getController().unsetPending();
            }
            AbstractController.update(
                    getController(),
                    new Runnable() {

                        @Override
                        public void run() {
                            refresh(view);
                        }
                    });
        }
        else {
            Log.w(tag(), "Skipping a refresh as the fragment is not added.");
        }
    }

    public final void refresh() {
        refreshInternal(getView());
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
        getController().onResume();
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

    public ControllerManager getManager() {
        return ControllerManager.getInstance();
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
        return getFragmentManager();
    }

    @Override
    public boolean isViewCreated() {
        return viewCreated;
    }
}


