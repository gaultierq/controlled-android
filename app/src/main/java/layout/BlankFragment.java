package layout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.gaultier.controlledandroid.R;
import io.gaultier.controlledandroid.control.ControlledFragment;

public class BlankFragment extends ControlledFragment<BlankFragmentController> {

    private static final String TAG = "BlankFragment";

    public BlankFragment() {
    }

    @Override
    protected void refresh(View v) {
        ((Button)v.findViewById(R.id.blank_button)).setText("Click number = " + getController().clicknumber);
    }

    @Override
    public BlankFragmentController makeController() {
        return new BlankFragmentController();
    }

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container) {

        View v = inflater.inflate(R.layout.fragment_blank, container, false);
        v.findViewById(R.id.blank_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().clicknumber ++;
                refresh();
            }
        });

        v.setBackgroundColor(getController().color);
        ((TextView)v.findViewById(R.id.creation)).setText(getController().creationDate.toString());
        return v;
    }
}
