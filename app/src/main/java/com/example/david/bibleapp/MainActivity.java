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

public class MainActivity extends AppCompatActivity {
    EditText userT, passwordT;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userT = findViewById(R.id.usernameED);
        passwordT = findViewById(R.id.passwordED);

        mAuth = FirebaseAuth.getInstance();
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
}
