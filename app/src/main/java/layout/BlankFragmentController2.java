package layout;

import android.graphics.Color;

import org.parceler.Parcel;

import java.util.Date;
import java.util.Random;

import io.gaultier.controlledandroid.control.AbstractFragmentController;

/**
 * Created by q on 17/10/16.
 */

@Parcel
public class BlankFragmentController2 extends AbstractFragmentController {

    int clicknumber = 0;
    Date creationDate = new Date();

    public int color2;
    {
        Random rand = new Random();
        color2 = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }

}
