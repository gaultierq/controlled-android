package io.gaultier.controlledandroid;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import io.gaultier.controlledandroid.control.ControlledActivity;
import layout.BlankFragment;

public class TotoActivity extends ControlledActivity<TotoActivityController> {

    @Override
    protected void createView() {
        setContentView(R.layout.activity_toto);
    }

    @Override
    protected void updateView() {
        ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(controller.progress ? View.VISIBLE : View.GONE);
        ((Button) findViewById(R.id.btnConnect)).setText(controller.progress ? "disconnect" : "connect");

    }

    public void doConnect(View view) {
        //do async stuff
        controller.progress = !controller.progress;
        updateView();
    }

    public void doAddFragment(View view) {
        Fragment newFragment = new BlankFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.toto_container, newFragment).commit();

    }
}
