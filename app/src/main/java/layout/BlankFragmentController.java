package layout;

import android.graphics.Color;

import org.parceler.Parcel;

import java.util.Date;
import java.util.Random;

import io.gaultier.controlledandroid.control.AbstractController;

/**
 * Created by q on 17/10/16.
 */

@Parcel
public class BlankFragmentController extends AbstractController {

    int clicknumber = 0;
    Date creationDate = new Date();

    public int color;
    {
        Random rand = new Random();
        color = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }

}
