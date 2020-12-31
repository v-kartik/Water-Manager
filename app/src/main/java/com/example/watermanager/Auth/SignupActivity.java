package com.example.watermanager.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.watermanager.MainActivity;
import com.example.watermanager.Others.Details;
import com.example.watermanager.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextEmail;
    private EditText editTextPassword;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextEmail = (EditText) findViewById(R.id.email_text);
        editTextPassword = (EditText) findViewById(R.id.password_text);

        progressBar = (ProgressBar) findViewById(R.id.pBarSignUp);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignUp).setOnClickListener(this);
        findViewById(R.id.toSignIn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.toSignIn :
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btnSignUp :
                registerUser();
                break;
        }
    }

    private void registerUser(){
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(email.isEmpty()) {
            editTextEmail.setError("Email cannot be empty");
            editTextEmail.requestFocus();
            return;
        }

        if(Patterns.EMAIL_ADDRESS.matcher(email).matches() == false){
            editTextEmail.setError("Enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("Password cannot be empty");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            editTextPassword.setError("Password should be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext()
                            , "User Registered Successfully"
                            ,Toast.LENGTH_SHORT)
                    .show();

                    Intent intent = new Intent(SignupActivity.this, Details.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }else if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(getApplicationContext()
                            , "Email already registered !!"
                            ,Toast.LENGTH_SHORT)
                            .show();
                }else{
                    Toast.makeText(getApplicationContext()
                            , "Error Occurred"
                            ,Toast.LENGTH_SHORT)
                    .show();
                }
            }
        });

    }
}