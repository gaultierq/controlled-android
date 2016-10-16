package io.gaultier.controlledandroid.control;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by q on 16/10/16.
 */

public abstract class ControlledActivity<T extends AbstractActivityController> extends AppCompatActivity {

    T mController;


    @Override
    protected final void onCreate(Bundle bundle) {

        super.onCreate(bundle);

        createView();

        mController = obtainController(bundle);

        initializeControllerFromId();

        // if the controller couldn't be initialized using the id found in the intent, it means the application has been killed
        if (_controller == null) {
            initializeControllerFromBundle(bundle);
        }

        if (_controller != null) {
            // controller initialization may be synchronous, so we can try to initialize the view right away
            if (_controller.initialized()) {
                initializeView();
            } else if (_controller.initializationFailed()) {
                onInitializationFailure();
            }
        }
    }


    private T obtainController(Bundle bundle) {
        if (mController == null) {

        }
        return mController;
    }


    protected abstract void createView();


    @Override
    public void finish() {
        ControllerManager.getInstance().unregister(mController);
        super.finish();
    }
}
