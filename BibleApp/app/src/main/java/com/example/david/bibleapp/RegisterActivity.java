package com.example.david.bibleapp;

/*
By David van Grinsven, Minor Programmeren, 2018, UvA

This Activity will allow the user to register a new account or go back to log in.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    // initializing references
    String email, password;
    EditText user_textview, password_textview;
    DatabaseReference sql_database;
    private FirebaseAuth the_auth;

    public RegisterActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // creating the references
        user_textview = findViewById(R.id.usernameED);
        password_textview = findViewById(R.id.passwordED);
        sql_database = FirebaseDatabase.getInstance().getReference();
        the_auth = FirebaseAuth.getInstance();
    }

    /*
    creates the user with use of an onCompleteListener
    */
    public void create_user(final String email, String password) {
        the_auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new registerListener());
    }

    /*
    will add a userclass to firebase when registering to make sure it is not null
    */
    public void addUserToFirebase(){
        FirebaseUser user = the_auth.getCurrentUser();
        assert user != null;
        String Uid = user.getUid();
        sql_database.child("users").child(Uid).setValue(new UserClass(email, null));
    }

    /*
    will go to the UserActivity
    */
    private void login() {
        Intent intent = new Intent(this, UserActivity.class);
        // starts the new activity

        startActivity(intent);
        finish();
    }

    /*
    checks if the password is 7 or longer and the email is valid
    if both are valid create the user otherwise set hints
    */
    public void register(View view) {
        email = user_textview.getText().toString();
        password = password_textview.getText().toString();
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            if (password.length() >6 ){
                create_user(email,password);
            }
            else{
                password_textview.setHint("needs at least 6 characters");
                password_textview.setText("");
            }
        }
        else{
            user_textview.setHint("fill in a valid email");
            user_textview.setText("");
        }
    }

    /*
    will go back to MainActivity to enable the user to login
    */
    public void goToLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);

        // starts the new activity
        startActivity(intent);
        finish();
    }

    /*
    will check if the user is registered if so loggin
    */
    private class registerListener implements OnCompleteListener<AuthResult> {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    // toast it, add user to firebase and go to the next activity
                    Toast.makeText(RegisterActivity.this,
                              "Authentication succes.", Toast.LENGTH_SHORT).show();
                    addUserToFirebase();
                    login();
                } else {

                    // If sign in fails, display a message to the user.
                    Toast.makeText(RegisterActivity.this,
                              "Authentication failed, check your connection",
                                   Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
