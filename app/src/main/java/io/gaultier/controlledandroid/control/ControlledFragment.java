package io.gaultier.controlledandroid.control;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.gaultier.controlledandroid.Assert;
import io.gaultier.controlledandroid.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledFragment<T extends AbstractFragmentController> extends Fragment {

    private static final String TAG = "ControlledFragment";

    private T controller;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, this , " onCreate");
        Assert.ensure(
                getActivity() instanceof ControlledActivity,
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
    public void onPause() {
        Log.i(TAG, this , " onPause");
        super.onPause();
    }


    @Override
    public void onAttach(Context context) {
        Log.i(TAG, this, "onAttach");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Log.i(TAG, this, "onDetach");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, this, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, this , " onSaveInstanceState");
        super.onSaveInstanceState(outState);
        // saving controller
        ControllerManager.getInstance().saveController(outState, controller);
    }

    @Override
    public void onStop() {
        Log.i(TAG, this , " onStop");
        super.onStop();
    }


    @Override
    public void onStart() {
        Log.i(TAG, this , " onStart");
        super.onStart();
    }


    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {


            ControllerManager manager = ControllerManager.getInstance();
            int controllerId;

            if (savedInstanceState != null) {
                // fragment has already existed (rotation, restore, killed)
                // -> restore controller
                controllerId = savedInstanceState.getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
                Assert.ensure(controllerId != INVALID_CONTROLLER_ID);

                //eg: rotation
                if (manager.isManaged(controllerId)) {
                    controller = (T) manager.getManagedController(controllerId);
                }
                else { //killed
                    controller = manager.restoreController(savedInstanceState);
                    manager.manage(controller);
                }

            }
            else if (getArguments() != null) {
                // fragment created programatically (already managed)
                // -> find controller
                controllerId = getArguments().getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
                Assert.ensure(controllerId != INVALID_CONTROLLER_ID, "Creating fragments via controllers");
                Assert.ensure(manager.isManaged(controllerId), "expecting a managed controller");
                controller = (T) manager.getManagedController(controllerId);
            }
            else {
                // creation by system (main activity, fragment)
                // -> create controller
                controller = makeController();
                Assert.ensure(controller != null);
                manager.manage(controller);
            }

            manager.addFragmentController(controller, ((ControlledActivity)getActivity()));

        }

        return controller;
    }

    public T getController() {
        return controller;
    }

    protected abstract void updateView(View v);

    protected abstract T makeController();


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
        return "["+"controlled-" + getClass().getSimpleName() + "-" + getControllerId()+"]";
    }

    private int getControllerId() {
        return controller != null ? controller.getId() : INVALID_CONTROLLER_ID;
    }

}
