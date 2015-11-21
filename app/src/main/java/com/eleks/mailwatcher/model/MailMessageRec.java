package com.eleks.mailwatcher.model;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;

import ExchangeActiveSync.EasMessage;

public class MailMessageRec {
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

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

}
