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

public abstract class ControlledFragment<T extends AbstractFragmentController> extends Fragment implements ControlledElement<T> {

    private static final String TAG = "ControlledFragment";

    private T controller;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, this , " onCreate");
        Assert.ensure(getActivity() instanceof ControlledActivity,
                "ControlledFragment can only exist in ControlledActivity"
        );
        super.onCreate(savedInstanceState);
        controller = obtainController(savedInstanceState);

    }

    @Override
    public void onResume() {
        Log.i(TAG, this , " onResume");
        super.onResume();
        Assert.ensureNotNull(controller);
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, this , " onSaveInstanceState");
        super.onSaveInstanceState(outState);

        // saving controller state
        ControllerManager.getInstance().saveController(outState, controller);
    }

    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {
            ControllerManager manager = ControllerManager.getInstance();
            //Debug.waitForDebugger();

            controller = ControllerManager.obtainController(savedInstanceState, getArguments(), this, manager);
            Log.i(TAG, "controller obtained:", "controller=", controller, "for fragment", getClass().getSimpleName(), "with savedinstancestate:", savedInstanceState);
            manager.addFragmentController(controller, ((ControlledActivity)getActivity()));
        }

        return controller;
    }

    protected abstract void updateView(View v);


    protected abstract View createView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = createView(inflater, container, savedInstanceState);
        updateView(view);
        return view;
    }

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

}
