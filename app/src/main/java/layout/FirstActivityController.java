package layout;

import org.parceler.Parcel;

import io.gaultier.controlledandroid.control.AbstractActivityController;
import io.gaultier.controlledandroid.control.ControlledActivity;

/**
 * Created by q on 18/10/16.
 */

@Parcel
public class FirstActivityController extends AbstractActivityController {
    @Override
    public ControlledActivity makeElement() {
        return new FirstActivity();
    }
}
