package com.example.david.bibleapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText userT, passwordT;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userT = findViewById(R.id.usernameED);
        passwordT = findViewById(R.id.passwordED);
        mAuth = FirebaseAuth.getInstance();
        // set a listener that checks if the user is already logged in
        setListener();

    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        Toast.makeText(MainActivity.this, "redirect succesfull",
                Toast.LENGTH_SHORT).show();

        // starts the new activity
        startActivity(intent);
    }

    public void LoginUser(View view) {
        String password = passwordT.getText().toString();
        String username = userT.getText().toString();

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success
                            SignedIn();
                        }
                        else {

                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SignedIn() {
        Intent intent = new Intent(this, UserActivity.class);
        Toast.makeText(MainActivity.this, "redirect succesfull",
                Toast.LENGTH_SHORT).show();

        // starts the new activity
        startActivity(intent);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    // if the user is singed in

                    Toast.makeText(getApplicationContext(), "logging in again",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,
                            UserActivity.class);

                    // starts the new activity
                    startActivity(intent);
                    finish();
                }
            }
        };
    }


}
