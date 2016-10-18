package layout;

import android.view.View;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledActivity;

public class MainActivity extends ControlledActivity<MainActivityController> {


    @Override
    protected void createView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void updateView() {
        //nothing to update
    }

    public void lauchToto(View v) {
        TotoActivityController ctrl = new TotoActivityController();
        ctrl.startActivity(this, TotoActivity.class);
    }

    @Override
    public MainActivityController makeController() {
        return new MainActivityController();
    }
}
