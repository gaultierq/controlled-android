package layout;

import org.parceler.Parcel;

import io.gaultier.controlledandroid.control.AbstractController;
import io.gaultier.controlledandroid.control.ControlledElement;

/**
 * Created by q on 16/10/16.
 */
@Parcel
public class SecondActivityController extends AbstractController {
    boolean progress;

    @Override
    public ControlledElement makeElement() {
        return new SecondActivity();
    }
}
