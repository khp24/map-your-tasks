package com.example.map_your_tasks.activity;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.map_your_tasks.R;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SignInButton msignInButton;
    private ImageView mImageView;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageViewAvatar);
        mImageView.setImageResource(R.mipmap.ic_launcher);

        msignInButton = findViewById(R.id.signInButton);
        msignInButton.setOnClickListener(this);

        createRequest();

        // Initialize firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        // Initialize firebase user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        //displayToast(firebaseUser.toString());
        // Check condition
        if (firebaseUser != null) {
            // When user already sign in redirect to profile activity
            displayToast("User Already Signed In");
            displayTasks();
        }
    }

    private void createRequest(){
        // Initialize sign in options the client-id is copied form google-services.json file
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("706590232546-2alc8li74mkrhb84h7mghl5pk14jn7fj.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, googleSignInOptions);
    }

    private void loginUser(){
        Intent intent = googleSignInClient.getSignInIntent();
        activityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK){
            Intent data = result.getData();
            Task<GoogleSignInAccount> tasks = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = tasks.getResult(ApiException.class);
                auth(account.getIdToken());

            }catch (ApiException e){
                throw new RuntimeException(e);
            }

        }

    });

    private void auth(String token)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                   if(task.isSuccessful()){
                        displayToast("Login Successful");
                        displayTasks();
                   }else displayToast("Login Failed");
                });
    }

    private void userProfile(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {
            displayToast(user.getDisplayName());
        }else{
            displayToast("User not logged in");
        }
    }

    private void logoutUser(){
        FirebaseAuth.getInstance().signOut();
        displayToast("completed sign out Intent");
    }
    private void displayToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signInButton:
                loginUser();
                break;
            default: break;
    }}

    private void displayTasks() {
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(intent);
    }
}