package com.eleks.mailwatcher.authentification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ExchangeAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        ExchangeAuthenticator authenticator = new ExchangeAuthenticator(this);
        return authenticator.getIBinder();
    }
}
