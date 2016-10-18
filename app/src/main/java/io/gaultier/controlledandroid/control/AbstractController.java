package io.gaultier.controlledandroid.control;

import org.parceler.Transient;

/**
 * Created by q on 16/10/16.
 */
public class AbstractController {

    public static final int INVALID_CONTROLLER_ID = -1;
    public static final String CONTROLLER_ID = "CONTROLLER_ID";
    public static final String CONTROLLER = "CONTROLLER";

    @Transient
    int id = INVALID_CONTROLLER_ID;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "["+ getClass().getSimpleName() + "-" + id + "]";
    }

}
