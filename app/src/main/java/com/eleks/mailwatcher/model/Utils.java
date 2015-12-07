package com.eleks.mailwatcher.model;

import java.util.regex.Pattern;

public final class Utils {
    public static String[] splitMalList(String mails) {
        if (mails == null) {
            return null;
        }
        return mails.split("[\\r\\n\\t,; ]+");
    }

    public static Pattern makePattern(String subject) {
        if (subject == null) {
            return null;
        }
        String[] parts = subject.split("\\*");
        StringBuilder sb = new StringBuilder();
        boolean bFirst = true;
        for (String part : parts) {
            if (!bFirst) {
                sb.append(".*");
            }
            sb.append(Pattern.quote(part));
            bFirst = false;
        }
        if (subject.length() > 1 && subject.endsWith("*"))
            sb.append(".*");
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

}
