package com.ashman.fivehundredpx;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ashman.fivehundredpx.api.FiveHundredException;
import com.ashman.fivehundredpx.api.auth.AccessToken;
import com.ashman.fivehundredpx.api.tasks.XAuth500pxTask;
import com.ashman.fivehundredpx.util.Util;


public class LoginActivity extends BaseFragmentActivity {
    private static final String INVALID_PASS_TAG = "invalidPass";

    private View contentLayout;
    private View loadingLayout;
    private EditText email;
    private EditText password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);
        contentLayout = findViewById(R.id.login_contentLayout);
        loadingLayout = findViewById(R.id.login_loadingLayout);
        Button login = (Button) findViewById(R.id.login_submit);
        Button register = (Button) findViewById(R.id.login_register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getString(R.string.login_register_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String errorMessage = getErrorsWithForm();
                if(errorMessage == null) {
                    login();
                } else {
                    showError(errorMessage);
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void login() {
        showView(loadingLayout);
        XAuth500pxTask loginTask = new XAuth500pxTask(new XAuth500pxTask.Delegate() {
            @Override
            public void onSuccess(AccessToken token) {
                FiveHundredApplication.get().saveAccessToken(token);
                finish();
            }

            @Override
            public void onFail(FiveHundredException e) {
                if(e.getStatusCode() == 403) {
                    showError(getString(R.string.login_auth_fail));
                } else {
                    showError(getString(R.string.error_generic));
                }
                showView(contentLayout);
            }
        });
        loginTask.execute(getString(R.string.oauth_consumer_key),
                getString(R.string.oauth_consumer_secret),
                email.getText().toString(), password.getText().toString());
    }

    private void showError(String message) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(
                INVALID_PASS_TAG);
        if(oldFragment != null)
            transaction.remove(oldFragment);
        SimpleDialogFragment dialog = SimpleDialogFragment.newInstance(message);
        dialog.show(transaction, INVALID_PASS_TAG);
    }

    private String getErrorsWithForm() {
            String errorMessage = null;
            if(email.getText().length() == 0 && password.getText().length() == 0) {
                errorMessage = getString(R.string.login_no_email_password);
            } else if(email.getText().length() == 0) {
                errorMessage = getString(R.string.login_no_email);
            } else if(password.getText().length() == 0) {
                errorMessage = getString(R.string.login_no_password);
            } else if(!email.getText().toString().matches(Util.EMAIL_REGEX)) {
                errorMessage = getString(R.string.login_invalid_email);
            }
        return errorMessage;
    }

    private void showView(final View viewToShow) {
        if(viewToShow != contentLayout && viewToShow != loadingLayout)
            throw new IllegalArgumentException("Illegal view requested to be shown");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);

                viewToShow.setVisibility(View.VISIBLE);
            }
        });
    }
}
