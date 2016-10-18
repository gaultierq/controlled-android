package layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledFragment;
import io.gaultier.controlledandroid.util.Assert;

public class BlankFragment2 extends ControlledFragment<BlankFragmentController2> {

    private static final String TAG = "BlankFragment2";

    public BlankFragment2() {
    }

    @Override
    protected void updateView(View v) {
        ((Button)v.findViewById(R.id.blank_button)).setText("Click number = " + getController().clicknumber);
    }

    @Override
    public BlankFragmentController2 makeController() {
        return new BlankFragmentController2();
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

        v.setBackgroundColor(getController().color2);
        ((TextView)v.findViewById(R.id.creation)).setText(getController().creationDate.toString());
        return v;
    }

    @Override
    protected void myassert() {
        getController();
        Assert.ensure(getController() instanceof BlankFragmentController2);
    }
}
