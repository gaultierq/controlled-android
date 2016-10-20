package io.gaultier.controlledandroid.control;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Collections;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractController> extends AppCompatActivity implements ControlledElement<T> {

    private static final String TAG = "ControlledActivity";

    protected T controller;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {

        controller = obtainController(savedInstanceState);

        super.onCreate(savedInstanceState);

        //inflating the view
        createView();

        updateView();
    }

    protected abstract void createView();

    protected abstract void updateView();

    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {
            controller = ControllerManager.obtainController(
                    savedInstanceState,
                    getIntent().getExtras(),
                    this,
                    ControllerManager.getInstance()
            );
            controller.assignStatus(ControllerStatus.ACTIVE);
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
        ControllerManager.getInstance().saveController(outState, controller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Assert.ensureNotNull(controller);
        updateView();
    }

    @Override
    public void finish() {
        super.finish();
        ControllerManager.getInstance().unmanage(Collections.<AbstractController>singleton(controller));
    }

    @Override
    public String toString() {
        return ControllerManager.toString(this);
    }

}
