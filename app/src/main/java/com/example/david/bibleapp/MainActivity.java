package com.example.david.bibleapp;
/*
This Activity allows the user to login.
If the user already logged in previously, the user will be redirected to UserActivity.
If the user has no account the user can click on the text:
"click here if you have no account"
And if clicked the user will be redirected to RegisterActivity to register.
*/
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
    EditText user_textview, password_textview;
    private FirebaseAuth the_auth;
    private FirebaseAuth.AuthStateListener the_authListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing the references
        user_textview = findViewById(R.id.usernameED);
        password_textview = findViewById(R.id.passwordED);
        the_auth = FirebaseAuth.getInstance();

        // set a listener that checks if the user is already logged in
        setListener();

    }

    /*
    on opening the app will set a listener to check if the user is previously logged in
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        the_auth.addAuthStateListener(the_authListener);
    }

    /*
    will remove the listener onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        if (the_authListener != null) {
            the_auth.removeAuthStateListener(the_authListener);
        }
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
    TODO anonomous listener
     */
    public void loginUser(View view) {
        String password = password_textview.getText().toString();
        String username = user_textview.getText().toString();


        the_auth.signInWithEmailAndPassword(username, password)
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
    the function that sets a listener to check if the user is already logged in previously
    if so go to UserActivity
     */
    private void setListener() {
        the_authListener = new FirebaseAuth.AuthStateListener() {
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
