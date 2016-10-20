package layout;

import android.view.View;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledActivity;

public class FirstActivity extends ControlledActivity<FirstActivityController> {


    @Override
    protected void createView() {
        setContentView(R.layout.activity_first);
    }

    @Override
    protected void updateView() {
        //nothing to update
    }

    public void lauchSecondActivity(View v) {
        SecondActivityController ctrl = new SecondActivityController();
        ctrl.startActivity(this, SecondActivity.class);
    }

    @Override
    public FirstActivityController makeController() {
        return new FirstActivityController();
    }
}
