package layout;

import android.app.Activity;
import android.view.View;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledActivity;
import io.gaultier.controlledandroid.control.ControllerManager;

public class FirstActivity extends ControlledActivity<FirstActivityController> {


    @Override
    protected void createView() {
        setContentView(R.layout.activity_first);
    }


    public void lauchSecondActivity(View v) {
        SecondActivityController ctrl = new SecondActivityController();
        ControllerManager.startActivity((Activity) this, SecondActivity.class, ctrl);
    }

    @Override
    public FirstActivityController makeController() {
        return new FirstActivityController();
    }
}
