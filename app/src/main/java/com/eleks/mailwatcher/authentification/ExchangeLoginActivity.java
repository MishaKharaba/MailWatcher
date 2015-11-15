package com.eleks.mailwatcher.authentification;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eleks.mailwatcher.R;

import ExchangeActiveSync.EasConnection;

public class ExchangeLoginActivity extends AccountAuthenticatorActivity {
    private static final String TAG = ExchangeLoginActivity.class.getName();

    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private AccountManager mAccountManager;
    private UserLoginTask mAuthTask = null;
    // UI references.
    private EditText mEmailView;
    private EditText mServerView;
    private EditText mUserView;
    private EditText mPasswordView;
    private CheckBox mIgnoreCert;
    private View mProgressView;

    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_login);
        mAccountManager = AccountManager.get(getBaseContext());

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mUserView = (EditText) findViewById(R.id.user);
        mUserView.setText(getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME));

        mServerView = (EditText) findViewById(R.id.server);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mIgnoreCert = (CheckBox) findViewById(R.id.ignore_cert);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mServerView.setError(null);
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String server = mServerView.getText().toString();
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();
        Boolean ignoreCert = mIgnoreCert.isChecked();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(server)) {
            mServerView.setError(getString(R.string.error_field_required));
            focusView = mServerView;
            cancel = true;
        } else if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, server, user, password, ignoreCert);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email);
    }

    private boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String email;
        private String server;
        private String user;
        private String password;
        private Boolean ignoreCert;
        private String error;
        private Long policyKey;

        UserLoginTask(String email, String server, String user, String password, Boolean ignoreCert) {
            this.email = email;
            this.server = server;
            this.user = user;
            this.password = password;
            this.ignoreCert = ignoreCert;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, "Started authenticating");

            try {
                EasConnection con = new EasConnection();
                con.setServer(server);
                con.setCredential(user, password);
                con.setIgnoreCertificate(ignoreCert);
                policyKey = con.getPolicyKey();
                return true;
            } catch (Exception e) {
                error = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean bOk) {
            mAuthTask = null;
            showProgress(false);

            if (!bOk) {
                Toast.makeText(getBaseContext(), error, Toast.LENGTH_SHORT).show();
            } else {
                finishLogin();
            }
        }

        private void finishLogin() {
            Log.d(TAG, "finishLogin");

            String accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
            String authToken = getIntent().getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);

            Account account = new Account(email, accountType);

            if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                Log.d(TAG, "finishLogin > addAccountExplicitly");

                // Creating the account on the device and setting the auth token we got
                // (Not setting the auth token will cause another call to the server to authenticate the user)
                mAccountManager.addAccountExplicitly(account, password, null);
                mAccountManager.setUserData(account, ExchangeAuthenticator.KEY_USER, user);
                mAccountManager.setUserData(account, ExchangeAuthenticator.KEY_SERVER, server);
                mAccountManager.setUserData(account, ExchangeAuthenticator.KEY_IGNORE_CERT,
                        ignoreCert ? "1" : "0");
                mAccountManager.setUserData(account, ExchangeAuthenticator.KEY_POLICY_KEY,
                        policyKey.toString());
                if (authToken != null) {
                    mAccountManager.setAuthToken(account, authTokenType, authToken);
                }
            } else {
                Log.d(TAG, "finishLogin > setPassword");
                mAccountManager.setPassword(account, password);
            }

            Intent intent = new Intent();
            intent.putExtra(ExchangeAuthenticator.KEY_USER, user);
            intent.putExtra(ExchangeAuthenticator.KEY_SERVER, server);
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
            intent.putExtra(AccountManager.KEY_PASSWORD, password);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
            intent.putExtra(ARG_AUTH_TYPE, authTokenType);
            intent.putExtra(ExchangeAuthenticator.KEY_IGNORE_CERT, ignoreCert);
            intent.putExtra(ExchangeAuthenticator.KEY_POLICY_KEY, policyKey);

            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

