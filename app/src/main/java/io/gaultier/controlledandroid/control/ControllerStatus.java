package io.gaultier.controlledandroid.control;

/**
 * Created by q on 20/10/16.
 */
public enum ControllerStatus {
    CREATE,
    WITH_ID,
    MANAGED,
    UNACTIVE,
    UNMANAGED
    ;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public ControllerStatus previous() {
        return ControllerStatus.values()[ordinal() - 1];
    }

    public boolean isAtLeast(ControllerStatus compared) {
        return compareTo(compared) >= 0;
    }
}
