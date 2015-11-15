package com.eleks.mailwatcher;

import android.content.Intent;

import com.eleks.mailwatcher.model.LabelRec;

import java.util.List;

public interface IAccountSelector {

    public interface Result{
        void Selected(String accountName);
    }

    String getAccountName();

    void Select(String accountName);

    void setAccount(String accountName);

    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    List<LabelRec> getFolders();
}
