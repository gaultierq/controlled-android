package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.gaultier.controlledandroid.util.Assert;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledFragment<T extends AbstractController> extends Fragment implements ControlledElement<T> {


    private ControllerAccessor<T> ctrlAccessor = new ControllerAccessor<>();

    protected int[] animation;

    @Override
    public final void onCreate(Bundle savedInstanceState) {

        ctrlAccessor.obtain(this, savedInstanceState, getArguments());

        Assert.ensure(getActivity() instanceof ControlledActivity, "ControlledFragment can only exist in ControlledActivity");

        super.onCreate(savedInstanceState);

    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refresh(view);
    }

    protected abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container);


    protected void refresh(View v) {
    }

    public final void refresh() {
        ControllerManager.refreshPendings(getController());
        refresh(getView());
    }

    private void prepareViewInternal(View fragmentView, T fragmentController) {
        getController().reset();
        prepareView(fragmentView, fragmentController);
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
        getController().reset();
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
        return getClass().getSimpleName() + getControllerId();
    }

    @Override
    public ControllerManager getManager() {
        return ((ControlledActivity) getActivity()).getManager();
    }

    public void setController(T controller) {
        ctrlAccessor.set(controller);
    }

    public int[] getAnimation() {
        return animation;
    }

    public boolean addToBackStack() {
        return true;
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
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getController().onActivityResult(requestCode, resultCode, data);
    }
}
