package com.example.tony.domestique;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfessionalInputService extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    ArrayList<String> arrayList = new ArrayList<>();
    Spinner sp;
    LinearLayout myLayout;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_input_service);

        sp = (Spinner) findViewById(R.id.spinner);
        myLayout = (LinearLayout) findViewById(R.id.btnlayout);


        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,arrayList);

        sp.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("services");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            arrayList.add((String) snapshot.getKey().toString());
                            adapter.notifyDataSetChanged();
                        }

                        //Get map of users in datasnapshot
                        //collectData((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                myLayout.removeAllViews();

                text = parentView.getItemAtPosition(position).toString();

                Button myButton = new Button(getApplicationContext());
                myButton.setText("Confirm And Proceed");
                myButton.setOnClickListener(goForward);
                myLayout.addView(myButton);

 }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        }

    CompoundButton.OnClickListener goForward = new CompoundButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Toast.makeText(ProfessionalInputService.this,"ok", Toast.LENGTH_SHORT).show();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("serviceprofessionals").child(text).child(userId);
            current_user_db.setValue(true);
            DatabaseReference current_user = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(userId).child("service");
            current_user.setValue(text);
            Intent intent = new Intent(ProfessionalInputService.this, ProfessionalMapActivity.class);
            intent.putExtra("serviceCategory", text);
            startActivity(intent);
            finish();
            return;

        }
    };

}
