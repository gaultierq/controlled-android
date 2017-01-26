package io.gaultier.controlledandroid.control;

/**
 * Created by qg on 26/01/17.
 */

public interface ActivityLauncher {

    <L extends AbstractActivityController> void launchActivity(L ctrl);

    <L extends AbstractActivityController> void launchActivityForResult(L ctrl, int requestCode);
}
