package com.example.tony.domestique;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private Button nProfessional, nCustomer;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nProfessional = (Button) findViewById(R.id.professional);
        nCustomer = (Button) findViewById(R.id.customer);

                nProfessional.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, MobProfessionalLogin.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                });

                nCustomer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MobCustomerLogin.class);
                    startActivity(intent);
                    finish();
                    return;
                    }
                });

                }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            LinearLayout mainLayout = findViewById(R.id.mainlayout);

            mainLayout.removeAllViews();
            ProgressBar mainpgbar = findViewById(R.id.mainpgbar);
            mainpgbar.setVisibility(View.VISIBLE);

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference current_user = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(user_id).child("service");
            current_user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Intent intent = new Intent(MainActivity.this, ProfessionalMapActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            DatabaseReference current_user2 = FirebaseDatabase.getInstance().getReference().child("users").child("customers").child(user_id);
            current_user2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        Intent intent = new Intent(MainActivity.this, CustomerSelectCategory.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }
}
