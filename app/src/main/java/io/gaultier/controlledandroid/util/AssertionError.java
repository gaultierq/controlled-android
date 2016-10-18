package io.gaultier.controlledandroid.util;

/**
 * Created by q on 17/10/16.
 */


final class AssertionException extends RuntimeException {

    private static final long serialVersionUID = 4977623941689043087L;

    AssertionException() {
        super();
    }

    AssertionException(String message) {
        super(message);
    }
}

