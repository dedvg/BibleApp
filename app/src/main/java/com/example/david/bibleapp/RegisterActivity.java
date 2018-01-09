package com.example.david.bibleapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    EditText userT, passwordT;
    Button loginB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userT = findViewById(R.id.usernameED);
        passwordT = findViewById(R.id.passwordED);
    }

    public void GoToLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        Toast.makeText(RegisterActivity.this, "redirect succesfull",
                Toast.LENGTH_SHORT).show();

        // starts the new activity
        startActivity(intent);
        finish();
    }
}
