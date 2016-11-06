package io.gaultier.controlledandroid.control;

/**
 * Created by q on 06/11/16.
 */
class PendingOperation {

    public PendingOperation(PendingOperationType type, int addIn) {
        this.type = type;
        this.addIn = addIn;
    }

    PendingOperationType type;
    int addIn;
}
