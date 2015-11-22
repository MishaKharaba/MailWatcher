package com.eleks.mailwatcher.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;

import ExchangeActiveSync.EasMessage;

public class MailMessageRec implements Parcelable {
    private final String to;
    private final String from;
    private final String subject;

    public MailMessageRec(String to, String from, String subject) {
        this.to = to;
        this.from = from;
        this.subject = subject;
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
    }

    protected MailMessageRec(Parcel in) {
        to = in.readString();
        from = in.readString();
        subject = in.readString();
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
}
