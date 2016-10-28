package io.gaultier.controlledandroid.control;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractController> extends AppCompatActivity implements ControlledElement<T> {

    private static final String TAG = "ControlledActivity";

    protected T controller;

    // notes: android views do not have their savedStated restored yet (wait on resume)
    @Override
    protected final void onCreate(Bundle savedInstanceState) {

        controller = obtainController(savedInstanceState);

        super.onCreate(savedInstanceState);

        createView();

        controller.viewCreationCount ++;

        //controller can be used by view from here
    }

    //inflating the view "state-less" (controller not available yet)
    protected abstract void createView();

    //use the controller and the state-full components (ex: a view pager), to configure the views
    protected void prepareView(T controller) {
        // nothing by default
    }

    public void refresh() {
        //nothing
    }

    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {
            controller = ControllerManager.obtainController(
                    savedInstanceState,
                    getIntent().getExtras(),
                    this,
                    getManager()
            );
        }
        return controller;
    }

    public T getController() {
        return controller;
    }

    public int getControllerId() {
        return controller != null ? controller.getId() : INVALID_CONTROLLER_ID;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, this , " onSaveInstanceState");
        super.onSaveInstanceState(outState);

        // saving controller
        getManager().saveController(outState, controller);
    }

    @Override
    public void finish() {
        super.finish();
        getManager().unmanage(controller);
    }

    @Override
    public String toString() {
        return ControllerManager.toString(this);
    }

    @Override
    protected final void onResume() {
        super.onResume();
        Assert.ensureNotNull(controller);
        prepareViewInternal(controller);
    }
    private void prepareViewInternal(T controller) {
        prepareView(controller);
        refresh();
    }


    public ControllerManager getManager() {
        return ControllerManager.getInstance(this);
    }
}
