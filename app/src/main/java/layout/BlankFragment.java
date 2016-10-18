package layout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Random;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledFragment;

public class BlankFragment extends ControlledFragment<BlankFragmentController> {

    private static final String TAG = "BlankFragment";

    public BlankFragment() {
    }

    @Override
    protected void updateView(View v) {
        ((Button)v.findViewById(R.id.blank_button)).setText("Click number = " + getController().clicknumber);
    }

    @Override
    protected BlankFragmentController makeController() {
        return new BlankFragmentController();
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_blank, container, false);
        v.findViewById(R.id.blank_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().clicknumber ++;
                updateView();
            }
        });
        Random rand = new Random();
        v.setBackgroundColor(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        return v;
    }
}
