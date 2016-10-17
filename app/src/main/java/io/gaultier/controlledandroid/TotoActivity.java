package io.gaultier.controlledandroid;

import android.view.View;
import android.widget.ProgressBar;

import io.gaultier.controlledandroid.control.ControlledActivity;

public class TotoActivity extends ControlledActivity<TotoActivityController> {

    @Override
    protected void createView() {
        setContentView(R.layout.activity_toto);
    }

    @Override
    protected void updateView() {
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(controller.isProgress() ? View.VISIBLE : View.GONE);
    }

    public void doConnect(View view) {
        //do async stuff
        controller.setProgress(true);
        updateView();
    }
}
