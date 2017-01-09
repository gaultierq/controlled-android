package layout;

import org.parceler.Parcel;

import io.gaultier.controlledandroid.control.AbstractActivityController;
import io.gaultier.controlledandroid.control.ControlledActivity;

/**
 * Created by q on 16/10/16.
 */
@Parcel
public class SecondActivityController extends AbstractActivityController {
    boolean progress;

    @Override
    public ControlledActivity makeElement() {
        return new SecondActivity();
    }
}
