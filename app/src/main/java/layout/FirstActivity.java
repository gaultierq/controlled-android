package layout;

import android.os.Bundle;
import android.view.View;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledActivity;

public class FirstActivity extends ControlledActivity<FirstActivityController> {


    @Override
    protected void createView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_first);
    }


    public void lauchSecondActivity(View v) {
        SecondActivityController ctrl = new SecondActivityController();
        //ControllerManager.startActivity(this, SecondActivity.class, ctrl);
    }

    @Override
    public FirstActivityController makeActivityController() {
        return new FirstActivityController();
    }
}
