package com.arron.passwordviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.arron.passwordview.PasswordView;

public class MainActivity extends AppCompatActivity implements PasswordView.PasswordListener, View.OnClickListener {
    private PasswordView passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passwordView = (PasswordView) findViewById(R.id.passwordView);
        passwordView.setPasswordListener(this);
    }

    @Override
    public void passwordChange(String password, boolean isComplete) {
        Log.d("Test", "password = " + password + " isComplete =  " + isComplete);
    }

    @Override
    public void passwordEnter(String password, boolean isComplete) {
        Log.d("Test", "password = " + password + " isComplete =  " + isComplete);
    }

    @Override
    public void onClick(View v) {

    }
}
