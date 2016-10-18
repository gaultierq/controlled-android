package layout;

import android.graphics.Color;

import org.parceler.Parcel;

import java.util.Random;

import io.gaultier.controlledandroid.control.AbstractFragmentController;

/**
 * Created by q on 17/10/16.
 */
@Parcel
public class BlankFragmentController extends AbstractFragmentController {

    int clicknumber = 0;



    public int color;
    {
        Random rand = new Random();
        color = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }

}
