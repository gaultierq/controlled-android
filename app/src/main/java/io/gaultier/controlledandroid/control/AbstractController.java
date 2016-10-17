package io.gaultier.controlledandroid.control;

/**
 * Created by q on 16/10/16.
 */
public class AbstractController {

    public static final int INVALID_CONTROLLER_ID = 0;
    public static final String CONTROLLER_ID = "CONTROLLER_ID";
    public static final String CONTROLLER = "CONTROLLER";


    int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
