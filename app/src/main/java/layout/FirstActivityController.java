package layout;

import org.parceler.Parcel;

import io.gaultier.controlledandroid.control.AbstractController;
import io.gaultier.controlledandroid.control.ControlledElement;

/**
 * Created by q on 18/10/16.
 */

@Parcel
public class FirstActivityController extends AbstractController {
    @Override
    public ControlledElement makeElement() {
        return new FirstActivity();
    }
}
