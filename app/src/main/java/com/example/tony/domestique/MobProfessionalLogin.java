package com.example.tony.domestique;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MobProfessionalLogin extends AppCompatActivity  {

    EditText editTextPhone, editTextCode;

    FirebaseAuth mAuth;

    String codeSent;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    String code;

    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mob_professional_login);



        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                codeSent = s;

            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
               // Log.d("Phone Authention Success", "onVerificationCom leted:" + phoneAuthCredential);
               // Toast.makeText(getApplicationContext(), phoneAuthCredential.getSmsCode(), Toast.LENGTH_LONG).show();
                //Toast.makeText(MobProfessionalLogin.this,"verification completed",Toast.LENGTH_SHORT).show();
                code = phoneAuthCredential.getSmsCode();
                if(code != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    editTextCode.setText(code);
                     verifySignInCode(code);
                }else{
                    signInWithPhoneAuthCredential(phoneAuthCredential);
                }


            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getApplicationContext(),
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }


        };

        editTextCode = findViewById(R.id.code);
        editTextPhone = findViewById(R.id.phone);

        progressBar = findViewById(R.id.progressbar);

        findViewById(R.id.getCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });


        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = editTextCode.getText().toString();
                verifySignInCode(code);
            }
        });
    }

    private void verifySignInCode(String code){
       // String code = editTextCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //here you can open new activity
                            FirebaseUser user = task.getResult().getUser();
                            String UID = user.getUid();
                            final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(UID).child("service");


                            current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()) {
                                        Toast.makeText(getApplicationContext(),
                                                "Welcome Back ", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(MobProfessionalLogin.this, ProfessionalMapActivity.class);
                                        startActivity(intent);
                                        finish();
                                        return;

                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),
                                                "Login Successfull", Toast.LENGTH_LONG).show();
                                        current_user_db.setValue(true);
                                        Intent intent = new Intent(MobProfessionalLogin.this, ProfessionalInputService.class);
                                        startActivity(intent);
                                        finish();
                                        return;
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode(){

        String phone = editTextPhone.getText().toString();

        if(phone.isEmpty()){
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        if(phone.length() < 10 ){
            editTextPhone.setError("Please enter a valid phone");
            editTextPhone.requestFocus();
            return;
        }


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
               "+91"+ phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
               MobProfessionalLogin.this,              // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }



}