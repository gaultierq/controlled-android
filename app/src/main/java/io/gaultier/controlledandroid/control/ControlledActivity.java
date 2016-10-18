package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Collections;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractActivityController> extends AppCompatActivity {

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


    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        Log.i(TAG, this , " onRestoreInstanceState");
        super.onRestoreInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, this, "onActivityResult(", requestCode, ",", resultCode, ",", data, ")");
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent " , intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, this , " onResume");
        super.onResume();

        Assert.ensureNotNull(controller);
        updateView();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, this , " onPause");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, this , " onSaveInstanceState");
        super.onSaveInstanceState(outState);

        // saving controller
        ControllerManager.getInstance().saveController(outState, controller);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, this , " onStop");
        super.onStop();
    }

    private T obtainController(Bundle savedInstanceState) {
        if (controller == null) {
            //1. is there a managed controller for this activity ?
            int controllerId = obtainControllerId(savedInstanceState);
            Assert.ensure(controllerId != INVALID_CONTROLLER_ID);
            ControllerManager manager = ControllerManager.getInstance();

            if (manager.isManaged(controllerId)) {
                controller = (T) manager.getManagedController(controllerId);
            }
            else {
                //killed activity
                controller = manager.restoreController(savedInstanceState);
                manager.manage(controller);
            }
        }
        return controller;
    }

    private int obtainControllerId(Bundle savedInstanceState) {
        int res = readControllerIdInternal(savedInstanceState);
        Assert.ensure(res != INVALID_CONTROLLER_ID);
        return res;
    }

    private int readControllerIdInternal(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int res = savedInstanceState.getInt(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
            return res;
        }
        return getIntent().getIntExtra(AbstractController.CONTROLLER_ID, INVALID_CONTROLLER_ID);
    }


    @Override
    public void finish() {
        Log.i(TAG, this , " finish");
        super.finish();
        ControllerManager.getInstance().unmanage(controller.getFragmentControllers());

        ControllerManager.getInstance().unmanage(Collections.<AbstractController>singleton(controller));
    }


    protected abstract void createView();

    protected abstract void updateView();

    public T getController() {
        return controller;
    }


    @Override
    public String toString() {
        return "["+"controlled-" + getClass().getSimpleName() + "-" + getControllerId()+"]";
    }

    private int getControllerId() {
        return controller != null ? controller.getId() : INVALID_CONTROLLER_ID;
    }
}
