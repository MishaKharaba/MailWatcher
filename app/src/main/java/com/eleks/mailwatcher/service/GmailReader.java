package com.eleks.mailwatcher.service;

import android.content.res.Resources;
import android.util.Log;

import com.eleks.mailwatcher.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class GmailReader
{
    public static final String TAG = "gmail-service";
    private Gmail mService;
    private Exception mLastError;

    public GmailReader(GoogleAccountCredential credential)
    {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Resources res = credential.getContext().getResources();
        mService = new Gmail.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(res.getString(R.string.app_name))
                .build();
    }

    public Exception getLastError()
    {
        return mLastError;
    }

    public List<Label> getLabelListSafe()
    {
        try
        {
            return getLabelList();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public List<Label> getLabelList() throws IOException
    {
        mLastError = null;
        try
        {
            ListLabelsResponse listResponse = mService.users().labels().list("me").execute();
            return listResponse.getLabels();
        }
        catch (Exception e)
        {
            mLastError = e;
            Log.e(TAG, "getLabelList", e);
            throw e;
        }
    }

    public List<Message> getMessages(int maxCount) throws IOException
    {
        mLastError = null;
        try
        {
            long maxResult = (maxCount > 100) ? 100 : maxCount;
            ListMessagesResponse response = mService.users().messages().list("me")
                    .setMaxResults(maxResult)
                    .setFields("messages(historyId,id,threadId)").execute();
            List<Message> messages = new ArrayList<>();
            while (response.getMessages() != null)
            {
                messages.addAll(response.getMessages());
                if (messages.size() >= maxCount)
                    break;
                if (response.getNextPageToken() != null)
                {
                    String pageToken = response.getNextPageToken();
                    response = mService.users().messages().list("me")
                            .setMaxResults(maxResult).setPageToken(pageToken)
                            .setFields("messages(historyId,id,threadId)").execute();
                }
                else
                {
                    break;
                }
            }
//            for (Message message : messages)
//            {
//                Log.i("gmail-service-messages", message.toPrettyString());
//            }
            return messages;
        }
        catch (Exception e)
        {
            mLastError = e;
            Log.e(TAG, "getMessages", e);
            throw e;
        }
    }

    public Message getMessage(String id) throws IOException
    {
        mLastError = null;
        try
        {
            return mService.users().messages().get("me", id).execute();
        }
        catch (Exception e)
        {
            mLastError = e;
            Log.e(TAG, "getMessage", e);
            throw e;
        }
    }

    public void checkLastError() throws Exception
    {
        if (mLastError != null)
        {
            throw mLastError;
        }
    }

    public class HistoryRec
    {
        List<History> list;
        BigInteger historyId;

        public HistoryRec(List<History> list, BigInteger historyId)
        {
            this.list = list;
            this.historyId = historyId;
        }
    }

    public HistoryRec getHistory(BigInteger startHistoryId, String labelId, int maxCount) throws IOException
    {
        mLastError = null;
        try
        {
            long maxResult = (maxCount > 100) ? 100 : maxCount;
            List<History> histories = new ArrayList<>();
            Gmail.Users.History.List historyList = mService.users().history().list("me");
            if (labelId != null)
            {
                historyList = historyList.setLabelId(labelId);
            }
            ListHistoryResponse response = historyList
                    .setMaxResults(maxResult).setStartHistoryId(startHistoryId).execute();
            while (response.getHistory() != null)
            {
                histories.addAll(response.getHistory());
                if (histories.size() >= maxCount)
                    break;
                if (response.getNextPageToken() != null)
                {
                    String pageToken = response.getNextPageToken();
                    response = historyList.setPageToken(pageToken)
                            .setMaxResults(maxResult).setStartHistoryId(startHistoryId).execute();
                }
                else
                {
                    break;
                }
            }

//            for (History history : histories)
//            {
//                Log.i("gmail-service-history", history.toPrettyString());
//            }
            return new HistoryRec(histories, response.getHistoryId());
        }
        catch (Exception e)
        {
            mLastError = e;
            Log.e(TAG, "getHistory", e);
            throw e;
        }
    }
}
