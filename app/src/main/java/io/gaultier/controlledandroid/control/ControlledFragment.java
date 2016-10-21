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

        Log.i(TAG, this , " onCreate");
        Assert.ensure(getActivity() instanceof ControlledActivity,  "ControlledFragment can only exist in ControlledActivity");

        controller = obtainController(savedInstanceState);

        super.onCreate(savedInstanceState);

    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return createView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateView(view);
    }

    protected abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected abstract void updateView(View v);

    protected final void updateView() {
        updateView(getView());
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
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // saving controller state
        ControllerManager.getInstance().saveController(outState, controller);
    }

    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {
            ControllerManager manager = ControllerManager.getInstance();
            controller = ControllerManager.obtainController(savedInstanceState, getArguments(), this, manager);

            Log.i(TAG, this, "managing subcontroller", controller);
            ((ControlledActivity)getActivity()).getController().manageSubController(controller);
        }

        return controller;
    }


}
