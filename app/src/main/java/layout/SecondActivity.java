package layout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledActivity;

public class SecondActivity extends ControlledActivity<SecondActivityController> {

    @Override
    protected void createView() {
        setContentView(R.layout.activity_second);
    }

    @Override
    protected void refreshView() {
        findViewById(R.id.progressBar).setVisibility(controller.progress ? View.VISIBLE : View.GONE);
        ((Button) findViewById(R.id.btnConnect)).setText(controller.progress ? "disconnect" : "connect");
    }

    public void doConnect(View view) {
        //do async stuff
        controller.progress = !controller.progress;
        refreshView();
    }

    public void doAddFragment(View view) {
        Fragment newFragment = new BlankFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.toto_container, newFragment).commit();

    }

    @Override
    public SecondActivityController makeController() {
        return new SecondActivityController();
    }
}
