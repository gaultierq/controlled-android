package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.gaultier.controlledandroid.Assert;
import io.gaultier.controlledandroid.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractActivityController> extends AppCompatActivity {

    private static final String TAG = "ControlledActivity";

    protected T controller;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //inflating the view
        createView();

        controller = obtainController(savedInstanceState);

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
        ControllerManager.getInstance().unmanage(controller);
        super.finish();
    }


    protected abstract void createView();

    protected abstract void updateView();
}
