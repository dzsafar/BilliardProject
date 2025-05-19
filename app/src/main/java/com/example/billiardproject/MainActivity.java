package com.example.billiardproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int SECRET_KEY = 63;
    private static final String PREF_KEY = register_activity.class.getPackage().toString();
    private static final int RC_SIGN_IN = 66;

    EditText emailET;
    EditText passwordET;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailET = findViewById(R.id.editTextEmail);
        passwordET = findViewById(R.id.editTextTextPassword);

        preferences = getSharedPreferences(PREF_KEY,MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG,"firebaseAuthWithGoogle: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }catch (ApiException e){
                Log.w(LOG_TAG,"Google sign in failed",e);
            }
        }
    }

    public void firebaseAuthWithGoogle(String idtoken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idtoken,null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG,"Google Account bejelentkezés sikeresen megtörtént!");
                    foglalasKezdese();
                }else {
                    Log.d(LOG_TAG,"Google Account bejelentkezés sikertelen!");
                    Toast.makeText(MainActivity.this,"Sikertelen bejelentkezés:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void register(View view) {
        Intent reg = new Intent(this, register_activity.class);
        reg.putExtra("SECRET_KEY",SECRET_KEY);
        startActivity(reg);
    }

    public void login(View view) {
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        Log.i(LOG_TAG,"Bejelentkezett: " + email + ", jelszó: " + password);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG,"Sikeres bejelentkezés!");
                    foglalasKezdese();
                }else {
                    Log.d(LOG_TAG,"Sikertelen bejelentkezés!");
                    Toast.makeText(MainActivity.this,"Sikertelen bejelentkezés:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void foglalasKezdese(){
        Intent fog = new Intent(this, foglalas_activity.class);
        //fog.putExtra("SECRET_KEY",SECRET_KEY);
        startActivity(fog);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email",emailET.getText().toString());
        editor.putString("password",passwordET.getText().toString());
        editor.apply();
    }

    public void loginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    public void loginAsGuest(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(LOG_TAG,"Vendég bejelentkezés!");
                    foglalasKezdese();
                }else {
                    Log.d(LOG_TAG,"Sikertelen belépés!");
                    Toast.makeText(MainActivity.this,"Sikertelen belépés:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}