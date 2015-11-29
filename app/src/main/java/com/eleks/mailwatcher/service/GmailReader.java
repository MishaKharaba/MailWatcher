package com.eleks.mailwatcher.service;

import android.accounts.Account;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.eleks.mailwatcher.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GmailReader {
    public static final String TAG = GmailReader.class.getSimpleName();

    private Gmail mService;
    private Exception mLastError;

    public GmailReader(Context context, String accountName) throws Exception {
        String[] SCOPES = {
                GmailScopes.MAIL_GOOGLE_COM,
                GmailScopes.GMAIL_LABELS,
                GmailScopes.GMAIL_READONLY};

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(accountName);

        if (credential.getSelectedAccount() == null)
            throw new Exception(String.format("Account '%s' not found", accountName));

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Resources res = credential.getContext().getResources();
        mService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(res.getString(R.string.app_name))
                .build();
    }

    public GmailReader(GoogleAccountCredential credential){
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Resources res = credential.getContext().getResources();
        mService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(res.getString(R.string.app_name))
                .build();
    }

    public Exception getLastError() {
        return mLastError;
    }

    public List<Label> getLabelListSafe() {
        try {
            return getLabelList();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Label> getLabelList() throws IOException {
        mLastError = null;
        try {
            ListLabelsResponse listResponse = mService.users().labels().list("me").execute();
            Log.d(TAG, "getLabelList");
            return listResponse.getLabels();
        } catch (Exception e) {
            mLastError = e;
            Log.e(TAG, "getLabelList", e);
            throw e;
        }
    }

    public List<Message> getMessages(String labelId, int maxCount) throws IOException {
        mLastError = null;
        try {
            long maxResult = (maxCount > 100) ? 100 : maxCount;
            List<String> labelIds = null;
            if (labelId != null) {
                labelIds = new ArrayList<>();
                labelIds.add(labelId);
            }
            ListMessagesResponse response = mService.users().messages().list("me")
                    .setLabelIds(labelIds)
                    .setMaxResults(maxResult)
                    .setFields("messages(historyId,id,threadId)").execute();
            List<Message> messages = new ArrayList<>();
            while (response.getMessages() != null) {
                messages.addAll(response.getMessages());
                if (messages.size() >= maxCount)
                    break;
                if (response.getNextPageToken() != null) {
                    String pageToken = response.getNextPageToken();
                    response = mService.users().messages().list("me")
                            .setMaxResults(maxResult).setPageToken(pageToken)
                            .setFields("messages(historyId,id,threadId)").execute();
                } else {
                    break;
                }
            }
//            for (Message message : messages)
//            {
//                Log.i("gmail-service-messages", message.toPrettyString());
//            }
            Log.d(TAG, "getMessages");
            return messages;
        } catch (Exception e) {
            mLastError = e;
            Log.e(TAG, "getMessages", e);
            throw e;
        }
    }

    public Message getMessage(String id) throws IOException {
        mLastError = null;
        try {
            Message message = mService.users().messages()
                    .get("me", id).setFormat("metadata")
                    .setFields("historyId,id,internalDate,payload")
                    .execute();
            Log.d(TAG, "getMessage");
            return message;
        } catch (Exception e) {
            mLastError = e;
            Log.e(TAG, "getMessage", e);
            throw e;
        }
    }

    public Message getLastMessage(String labelId) throws IOException {
        List<Message> messages = getMessages(labelId, 1);
        if (messages.size() > 0) {
            Message message = getMessage(messages.get(0).getId());
            Log.d(TAG, "getLastMessage");
            return message;
        } else {
            return null;
        }
    }

    public void checkLastError() throws Exception {
        if (mLastError != null) {
            throw mLastError;
        }
    }

    public class HistoryRec {
        List<History> list;
        BigInteger historyId;

        public HistoryRec(List<History> list, BigInteger historyId) {
            this.list = list;
            this.historyId = historyId;
        }
    }

    public HistoryRec getHistory(String startHistoryId, String labelId, int maxCount) throws IOException {
        mLastError = null;
        try {
            BigInteger historyId = new BigInteger(startHistoryId);
            long maxResult = (maxCount > 100) ? 100 : maxCount;
            List<History> histories = new ArrayList<>();
            Gmail.Users.History.List historyList = mService.users().history().list("me");
            if (labelId != null) {
                historyList = historyList.setLabelId(labelId);
            }
            ListHistoryResponse response = historyList
                    .setMaxResults(maxResult).setStartHistoryId(historyId).execute();
            while (response.getHistory() != null) {
                histories.addAll(response.getHistory());
                if (histories.size() >= maxCount)
                    break;
                if (response.getNextPageToken() != null) {
                    String pageToken = response.getNextPageToken();
                    response = historyList.setPageToken(pageToken)
                            .setMaxResults(maxResult).setStartHistoryId(historyId).execute();
                } else {
                    break;
                }
            }

//            for (History history : histories)
//            {
//                Log.i("gmail-service-history", history.toPrettyString());
//            }
            Log.d(TAG, "getHistory");
            return new HistoryRec(histories, response.getHistoryId());
        } catch (Exception e) {
            mLastError = e;
            Log.e(TAG, "getHistory", e);
            throw e;
        }
    }
}
