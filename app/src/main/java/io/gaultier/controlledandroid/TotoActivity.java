package io.gaultier.controlledandroid;

import io.gaultier.controlledandroid.control.ControlledActivity;

public class TotoActivity extends ControlledActivity<TotoActivityController> {

    @Override
    protected void createView() {
        setContentView(R.layout.activity_main);
    }
}
