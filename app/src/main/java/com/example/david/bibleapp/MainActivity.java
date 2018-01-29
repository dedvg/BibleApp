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

    // creating references
    EditText userT, passwordT;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing the references
        userT = findViewById(R.id.usernameED);
        passwordT = findViewById(R.id.passwordED);
        mAuth = FirebaseAuth.getInstance();

        // set a listener that checks if the user is already logged in
        setListener();

    }

    /*
    will go to RegisterActivity to enable the user to register
     */
    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    /*
    will handle the login
    if succesfull go to UserActivity
     */
    public void loginUser(View view) {
        String password = passwordT.getText().toString();
        String username = userT.getText().toString();

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // sign in success
                            signedIn();
                        }
                        else {

                            // if sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    /*
    go to UserActivity
     */
    private void signedIn() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
    }

    /*
    on opening the app will set a listener to check if the user is previously logged in
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        mAuth.addAuthStateListener(mAuthListener);
    }

    /*
    will remove the listener onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /*
    the function that sets a listener to check if the user is already logged in previously
    if so go to UserActivity
     */
    private void setListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    signedIn();
                }
            }
        };
    }
}
