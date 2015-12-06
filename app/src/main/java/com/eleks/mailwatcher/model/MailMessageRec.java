package com.eleks.mailwatcher.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

import ExchangeActiveSync.EasMessage;

public class MailMessageRec implements Parcelable {
    private final String to;
    private final String from;
    private final String subject;

    private HashSet<String> toArr;
    private HashSet<String> fromArr;

    public MailMessageRec(String to, String from, String subject) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        toArr = extractMailAddresses(to);
        fromArr = extractMailAddresses(from);
    }

    public static HashSet<String> extractMailAddresses(String addr) {
        if (TextUtils.isEmpty(addr)) {
            return null;
        }
        Pattern p = Pattern.compile("<([^<]+@[^>]+)>");
        Matcher m = p.matcher(addr);

        HashSet<String> addrList = new HashSet<>();
        while (m.find()) {
            addrList.add(m.group(1).toLowerCase());
        }

        return addrList;
    }

    public MailMessageRec(EasMessage easMsg) {
        this(easMsg.getTo(), easMsg.getFrom(), easMsg.getSubject());
    }

    public MailMessageRec(Message msg) {
        String to = null;
        String from = null;
        String subject = null;
        List<MessagePartHeader> headers = msg.getPayload().getHeaders();
        for (MessagePartHeader header : headers) {
            switch (header.getName()) {
                case "To":
                    to = header.getValue();
                    break;
                case "From":
                    from = header.getValue();
                    break;
                case "Subject":
                    subject = header.getValue();
                    break;
            }
        }
        this.to = to;
        this.from = from;
        this.subject = subject;
        toArr = extractMailAddresses(to);
        fromArr = extractMailAddresses(from);
    }

    protected MailMessageRec(Parcel in) {
        to = in.readString();
        from = in.readString();
        subject = in.readString();
        toArr = extractMailAddresses(to);
        fromArr = extractMailAddresses(from);
    }

    public static final Creator<MailMessageRec> CREATOR = new Creator<MailMessageRec>() {
        @Override
        public MailMessageRec createFromParcel(Parcel in) {
            return new MailMessageRec(in);
        }

        @Override
        public MailMessageRec[] newArray(int size) {
            return new MailMessageRec[size];
        }
    };

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(to);
        dest.writeString(from);
        dest.writeString(subject);
    }

    public boolean checkFrom(String[] mails) {
        if (mails == null || mails.length == 0) {
            return true;
        }
        if (fromArr == null) {
            return false;
        }
        for (String mail : mails) {
            if (fromArr.contains(mail.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTo(String[] mails) {
        if (mails == null || mails.length == 0) {
            return true;
        }
        if (toArr == null) {
            return false;
        }
        for (String mail : mails) {
            if (toArr.contains(mail.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkSubject(Pattern p) {
        if (p == null) {
            return true;
        }
        if (TextUtils.isEmpty(subject)) {
            return false;
        }
        Matcher m = p.matcher(subject);
        return m.matches();
    }

}
