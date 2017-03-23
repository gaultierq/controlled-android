package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;

import io.gaultier.controlledandroid.util.Log;

import static io.gaultier.controlledandroid.control.AbstractController.INVALID_CONTROLLER_ID;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractActivityController> extends AppCompatActivity
        implements ControlledElement<T> {

    private static final String TAG = "ControlledActivity";

    private ControllerAccessor<T> ctrlAccessor = new ControllerAccessor<>();

    private LayoutInflater inflater;

    private boolean viewCreated;

    // notes: android views do not have their savedStated restored yet (wait on resume)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        boolean obtained = ctrlAccessor.obtain(this, savedInstanceState, getIntent().getExtras());


        if (obtained) {
            int theme = getController().getOverrideTheme();
            if (theme > 0) {
                setTheme(theme);
            }
        }

        super.onCreate(savedInstanceState);

        if (!obtained) {
            Log.w(tag(), "impossible to obtain controller, finishing activity");
            finish();
            return;
        }

        createView(savedInstanceState);

        viewCreated = true;
    }

    //inflating the view "state-less" (controller not available yet)
    protected abstract void createView(Bundle savedInstanceState);

    //TODO: protected
    public void refreshView() {
    }

    public final void refresh() {
        AbstractController.update(
                getController(),
                new Runnable() {

                    @Override
                    public void run() {
                        refreshView();
                    }
                });
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
        T controller = getController();

        //controlled activities are finished (in onCreate) when controller is null.
        //this is why controller can be null here
        if (controller != null) {
            //overridePendingTransition(0, controller.animation[1]);
            getManager().unmanage(controller);
        }
    }

    @Override
    public String toString() {
        return ControllerManager.toString(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getController().onResume();
    }

    public ControllerManager getManager() {
        return ControllerManager.getInstance();
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
    public final void onBackPressed() {
        if (!getController().onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getController().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getController().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean interceptBackPressed() {
        return false;
    }

    @Override
    public FragmentManager obtainFragmentManager(AbstractController child) {
        return getSupportFragmentManager();
    }

    public String tag() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean isViewCreated() {
        return viewCreated;
    }
}
