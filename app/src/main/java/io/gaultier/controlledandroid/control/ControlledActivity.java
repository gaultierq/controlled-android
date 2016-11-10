package io.gaultier.controlledandroid.control;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import io.gaultier.controlledandroid.util.Assert;
import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractController> extends AppCompatActivity implements ControlledElement<T> {

    private static final String TAG = "ControlledActivity";

    private ControllerAccessor<T> ctrlAccessor = new ControllerAccessor<>();

    // notes: android views do not have their savedStated restored yet (wait on resume)
    @Override
    protected final void onCreate(Bundle savedInstanceState) {

        ctrlAccessor.obtain(this, savedInstanceState, getIntent().getExtras());

        super.onCreate(savedInstanceState);

        createView();

        //controller can be used by view from here
    }

    //inflating the view "state-less" (controller not available yet)
    protected abstract void createView();

    //use the controller and the state-full components (ex: a view pager), to configure the views
    protected void prepareView(T controller) {
        // nothing by default
    }

    public void refreshView() {
    }

    public final void refresh() {
        ControllerManager.refreshPendings(getController());
        refreshView();
    }

    public T getController() {
        return ctrlAccessor.get();
    }

    public String getControllerId() {
        return getController() != null ? getController().getControllerId() : INVALID_CONTROLLER_ID;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, this, " onSaveInstanceState");
        super.onSaveInstanceState(outState);

        // saving controller
        getManager().saveController(outState, getController());
    }

    @Override
    public void finish() {
        super.finish();
        getManager().unmanage(getController());
    }

    @Override
    public String toString() {
        return ControllerManager.toString(this);
    }

    @Override
    protected final void onResume() {
        super.onResume();
        Assert.ensureNotNull(getController());
        prepareViewInternal(getController());
    }

    private void prepareViewInternal(T controller) {
        prepareView(controller);
        refresh();
    }

    public ControllerManager getManager() {
        return ControllerManager.getInstance(this);
    }

    @Override
    public final T makeController() {
        T res = makeActivityController();
        if (res.getParentController() == null) {
            res.assignParentController(getManager().getMainController());
        }
        return res;
    }

    public abstract T makeActivityController();


    @Override
    public ControlledActivity getControlledActivity() {
        return this;
    }

    @Override
    public void onBackPressed() {
        if (!getController().onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean interceptBackPressed() {
        return false;
    }

    @Override
    public FragmentManager obtainFragmentManager() {
        return getSupportFragmentManager();
    }
}
