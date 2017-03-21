package com.arron.passwordviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.arron.passwordview.PasswordView;

public class MainActivity extends AppCompatActivity implements PasswordView.PasswordListener, View.OnClickListener {
    private String tag = getClass().getSimpleName();
    private PasswordView passwordView;
    private Button btnChangeMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passwordView = (PasswordView) findViewById(R.id.passwordView);
        btnChangeMode = (Button) findViewById(R.id.btn_change_mode);
        btnChangeMode.setOnClickListener(this);
        passwordView.setPasswordListener(this);
    }

    @Override
    public void passwordChange(String changeText) {
        Log.d(tag, "changeText = " + changeText);
    }

    @Override
    public void passwordComplete() {
        Log.d(tag, "passwordComplete");
    }

    @Override
    public void keyEnterPress(String password, boolean isComplete) {
        Log.d(tag, "password = " + password + " isComplete = " + isComplete);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btn_change_mode) {
            passwordView.setMode(passwordView.getMode() == PasswordView.Mode.RECT ? PasswordView.Mode.UNDERLINE : PasswordView.Mode.RECT);
        }
    }
}
