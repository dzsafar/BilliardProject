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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class register_activity extends AppCompatActivity {

    private static final String LOG_TAG = register_activity.class.getName();
    private static final String PREF_KEY = register_activity.class.getPackage().toString();
    private static final int SECRET_KEY = 63;

    EditText nameET;
    EditText userEmailET;
    EditText passwordET;
    EditText passwordConfirmET;
    EditText phoneNumberET;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int secret_key = getIntent().getIntExtra("SECRET_KEY",0);

        if (secret_key != 63){
            finish();
        }

        nameET = findViewById(R.id.editTextName);
        userEmailET = findViewById(R.id.editTextEmail);
        passwordET = findViewById(R.id.editTextPassword);
        passwordConfirmET = findViewById(R.id.editTextPasswordAgain);
        phoneNumberET = findViewById(R.id.editTextPhoneNumber);

        preferences = getSharedPreferences(PREF_KEY,MODE_PRIVATE);

        String email = preferences.getString("email","");
        String password = preferences.getString("password","");

        userEmailET.setText(email);
        passwordET.setText(password);
        passwordConfirmET.setText(password);

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String name = nameET.getText().toString();
        String email = userEmailET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = passwordConfirmET.getText().toString();
        String phoneNumber = phoneNumberET.getText().toString();

        if (!password.equals(passwordConfirm)){
            Log.e(LOG_TAG,"Nem egyeznek meg a jelszavak!");
            return;
        }
            Log.i(LOG_TAG,"Regisztrált: " + name + " (email:" + email + "), telefonszáma: " + phoneNumber);

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Log.d(LOG_TAG,"Sikeres regisztráció!");
                        foglalasKezdese();
                    }else {
                        Log.d(LOG_TAG,"Sikertelen regisztráció!");
                        Toast.makeText(register_activity.this,"Sikertelen regisztráció:" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
    public void cancel(View view) {
        finish();
    }

    private void foglalasKezdese(){
        Intent fog = new Intent(this, foglalas_activity.class);
        //fog.putExtra("SECRET_KEY",SECRET_KEY);
        startActivity(fog);
    }
}