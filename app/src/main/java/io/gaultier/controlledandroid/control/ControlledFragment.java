package io.gaultier.controlledandroid.control;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledFragment<T extends AbstractController> extends Fragment implements ControlledElement<T> {

    private static final String TAG = "ControlledFragment";

    private T controller;


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        controller = obtainController(savedInstanceState);

        Log.i(TAG, this, " onCreate");

        Assert.ensure(getActivity() instanceof ControlledActivity, "ControlledFragment can only exist in ControlledActivity");

        super.onCreate(savedInstanceState);

    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = createView(inflater, container);

        controller.viewCreationCount ++;

        return view;
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
        refresh(getView());
    }

    private void prepareViewInternal(View fragmentView, T fragmentController) {
        controller.reset();
        prepareView(fragmentView, fragmentController);
    }

    protected void prepareView(View fragmentView, T fragmentController) {
    }

    @Override
    public String toString() {
        return ControllerManager.toString(this);
    }

    public T getController() {
        return controller;
    }

    public int getControllerId() {
        return controller != null ? controller.getId() : INVALID_CONTROLLER_ID;
    }

    @Override
    public void onResume() {
        super.onResume();
        Assert.ensureNotNull(controller);
        prepareViewInternal(getView(), getController());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // saving controller state
        getManager().saveController(outState, controller);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {
            controller = ControllerManager.obtainController(savedInstanceState, getArguments(), this, getManager());

            Log.i(TAG, this, "managing subcontroller", controller);

            AbstractController c = ((ControlledActivity) getActivity()).getController();
            c.manageSubController(this.controller);
        }

        return controller;
    }

    public final String tag() {
        return getClass().getSimpleName() + getControllerId();
    }

    @Override
    public ControllerManager getManager() {
        return ((ControlledActivity)getActivity()).getManager();
    }

}
