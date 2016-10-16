package io.gaultier.controlledandroid.control;

import android.content.Intent;
import android.widget.EditText;

import io.gaultier.controlledandroid.R;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * Created by q on 16/10/16.
 */

public abstract class AbstractActivityController extends AbstractController {


    public void startActivity() {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
