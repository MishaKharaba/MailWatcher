package com.eleks.mailwatcher;

public final class EHelper {
    public static String getMessage(Exception e) {
        String msg = e.getLocalizedMessage();
        if (msg == null && e.getCause() != null) {
            msg = e.getCause().getMessage();
        }
        return msg;
    }
}
