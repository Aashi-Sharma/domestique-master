package com.example.tony.domestique;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfessionalShedules extends AppCompatActivity {

    LinearLayout sheduleView;
    //CardView sheduleViewCard;
    TextView rowTextView,rowTextView2;
    CardView card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_shedules);
        sheduleView = findViewById(R.id.sheduleList);
        //sheduleViewCard = findViewById(R.id.sheduleListCard);


        final String MyuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference shedulesRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(MyuserId).child("Requests");
        shedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshots : dataSnapshot.getChildren()) {
                    card = new CardView(getApplicationContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    card.setLayoutParams(params);
                    card.setRadius(9);

                    // Set cardView content padding
                    card.setContentPadding(15, 15, 15, 15);

                    // Set a background color for CardView
                    card.setCardBackgroundColor(Color.parseColor("#FFC6D6C3"));

                    // Set the CardView maximum elevation
                    card.setMaxCardElevation(15);

                    // Set CardView elevation
                    card.setCardElevation(9);
                DatabaseReference shedulesRef2 = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(MyuserId).child("Requests").child(snapshots.getKey());
                shedulesRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                             rowTextView = new TextView(getApplicationContext());

                            rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);

                            rowTextView.setTextColor(Color.WHITE);

                            rowTextView.setPadding(25, 25, 25, 25);

                            rowTextView.setGravity(Gravity.CENTER);

                             rowTextView2 = new TextView(getApplicationContext());

                            rowTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);

                            rowTextView2.setTextColor(Color.WHITE);

                            rowTextView2.setPadding(25, 25, 25, 25);

                            rowTextView2.setGravity(Gravity.CENTER);


                            if (snapshot.getKey().toString().equals("date")) {

                                 rowTextView.setText(snapshot.getValue().toString());
                                 rowTextView.setText(snapshot.getValue().toString());

                                      //card.addView(rowTextView);


                            }

                            if (snapshot.getKey().toString().equals("services")) {
                                for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                    rowTextView2.setText(snapshot2.getValue().toString());
                                   // card.addView(rowTextView);
                                }

                            }

                        }
                    }

                    @Override


                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
                TextView rowTextView1 = new TextView(getApplicationContext());
                rowTextView1.setText(rowTextView.getText().toString()+rowTextView2.getText().toString());
                card.addView(rowTextView1);

                sheduleView.addView(card);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
