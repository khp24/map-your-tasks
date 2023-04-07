package com.example.map_your_tasks.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.map_your_tasks.R;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener {
    private Button msignOutButton;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        msignOutButton = findViewById(R.id.signOutButton);
        msignOutButton.setOnClickListener(this);

        userProfile();
    }


    private void userProfile(){
        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(getApplicationContext(),  GoogleSignInOptions.DEFAULT_SIGN_IN);
        // Initialize firebase user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null)
        {
            displayToast(firebaseUser.getDisplayName());
            displayToast(firebaseUser.getUid());
        }else{
            displayToast("User not logged in");
        }
    }

    private void logoutUser(){
        FirebaseAuth.getInstance().signOut();
        googleSignInClient.signOut();
        displayToast("completed sign out Intent");
        Intent intent = new Intent(SecondActivity.this, MainActivity.class);
        startActivity(intent);
    }
    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signOutButton:
                logoutUser();
                break;
            default: break;
        }}
}
