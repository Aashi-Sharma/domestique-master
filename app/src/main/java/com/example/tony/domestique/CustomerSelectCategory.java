package com.example.tony.domestique;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class CustomerSelectCategory extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    Spinner sp;
    TextView item;
    ArrayAdapter <String> adapter;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<String> mySerivce = new ArrayList<>();
    LinearLayout linearLayout;
    LinearLayout ll;
    CheckBox checkBox;
    TableLayout myTable;
    TableLayout totaltable;
    String text;
    Integer totalCost = 0;
    Boolean dateSelected = false;
    String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_select_category);

        sp = (Spinner) findViewById(R.id.spinner);
        item = (TextView) findViewById(R.id.item);
        //linearLayout = (LinearLayout) findViewById(R.id.container);
        myTable = (TableLayout) findViewById(R.id.container);
        totaltable = (TableLayout) findViewById(R.id.totalcost);


        // ll = (LinearLayout) findViewById(R.id.container2);



        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,arrayList);

        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
            {
                Toast.makeText(CustomerSelectCategory.this,"Fetching Details", Toast.LENGTH_SHORT).show();

                 text = parentView.getItemAtPosition(position).toString();
                myTable.removeAllViews();
                totaltable.removeAllViews();
                totalCost = 0;
                mySerivce.clear();
                //ll.removeAllViews();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("services").child(text);
                ref.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {


                                    TableRow row;
                                    row = new TableRow(getApplicationContext());

                                    checkBox = new CheckBox(getApplicationContext());
                                    checkBox.setText(snapshot.getKey().toString());
                                    checkBox.setWidth(800);
                                    checkBox.setOnCheckedChangeListener(clickListener);
                                    row.addView(checkBox);


                                    final TextView rowTextView = new TextView(getApplicationContext());

                                    // set some properties of rowTextView or something
                                    rowTextView.setText(snapshot.getValue().toString());
                                    rowTextView.setWidth(400);
                                    // add the textview to the linearlayout
                                    row.addView(rowTextView);

                                    myTable.addView(row, new TableLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                                }
                                insertCalendarButton();
                                insertButton();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });



            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

//



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

    }

    CompoundButton.OnCheckedChangeListener clickListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                String service = buttonView.getText().toString();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("services").child(text).child(service);
                ref.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Toast.makeText(CustomerSelectCategory.this,dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                                totaltable.removeAllViews();
                               Integer Cost = Integer.valueOf(dataSnapshot.getValue().toString());
                                totalCost = totalCost+Cost;

                                final TextView totalView = new TextView(getApplicationContext());
                                totalView.setText(totalCost.toString());

                                final TextView MyText = new TextView(getApplicationContext());
                                MyText.setText("Total Cost");
                                MyText.setWidth(400);

                                TableRow totalrow;
                                totalrow = new TableRow(getApplicationContext());

                                totalrow.addView(MyText);
                                totalrow.addView(totalView);

                                totaltable.addView(totalrow, new TableLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });


                mySerivce.add((String) service);
            } else {

                String service = buttonView.getText().toString();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("services").child(text).child(service);
                ref.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Toast.makeText(CustomerSelectCategory.this,dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                                totaltable.removeAllViews();
                                Integer Cost = Integer.valueOf(dataSnapshot.getValue().toString());
                                totalCost = totalCost-Cost;

                                final TextView totalView = new TextView(getApplicationContext());
                                totalView.setText(totalCost.toString());

                                final TextView MyText = new TextView(getApplicationContext());
                                MyText.setText("Total Cost");
                                MyText.setWidth(400);

                                TableRow totalrow;
                                totalrow = new TableRow(getApplicationContext());

                                totalrow.addView(MyText);
                                totalrow.addView(totalView);

                                totaltable.addView(totalrow, new TableLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });


                mySerivce.remove((String) service);

            }
        }
    };




    CompoundButton.OnClickListener insertCalendar = new CompoundButton.OnClickListener() {
        @Override
        public void onClick(View view) {

            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }
    };

private void insertCalendarButton(){
    TableRow row;
    row = new TableRow(getApplicationContext());

    Button myButton = new Button(getApplicationContext());
    myButton.setText("Choose Date");
    myButton.setOnClickListener(insertCalendar);
    row.addView(myButton);
    myTable.addView(row, new TableLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
}

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
     Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            currentDate = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());

        final Calendar cal = Calendar.getInstance();
        String today =  DateFormat.getDateInstance(DateFormat.SHORT).format(cal.getTime());

        if (currentDate.compareTo(today) < 0) {
            Toast.makeText(CustomerSelectCategory.this,"Inappropriate Date Selected", Toast.LENGTH_SHORT).show();
            dateSelected = false;
        }else{
            dateSelected = true;
        }



    }

    private void insertButton(){

        TableRow row;
        row = new TableRow(getApplicationContext());

        Button myButton = new Button(getApplicationContext());
        myButton.setText("Confirm And Proceed");
        myButton.setOnClickListener(goForward);
        row.addView(myButton);
        myTable.addView(row, new TableLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    }

    CompoundButton.OnClickListener goForward = new CompoundButton.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!mySerivce.isEmpty()){
                if(dateSelected){
                    Toast.makeText(CustomerSelectCategory.this,"Processing", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CustomerSelectCategory.this, CustomerMapActivity.class);
                    intent.putExtra("category", text);
                    intent.putExtra("date", currentDate);
                    intent.putExtra("services", mySerivce);
                    intent.putExtra("total", totalCost.toString());
                    startActivity(intent);
                    finish();
                    return;
                }else{
                    Toast.makeText(CustomerSelectCategory.this,"You must select a date", Toast.LENGTH_SHORT).show();

                }


            }else{
                Toast.makeText(CustomerSelectCategory.this,"You must select at least one service", Toast.LENGTH_SHORT).show();



            }
        }
    };

    private void collectData(Map<String,Object> services) {



        //iterate through each user, ignoring their UID
//        for (Map.Entry<String, Object> entry : services.entrySet()){
//
//            //Get user map
//            Map singleUser = (Map) entry.getValue();
//            //Get phone field and append to list
//            arrayList.add((String) singleUser.toString());
//        }
        arrayList.add((String) services.toString());
        System.out.println(arrayList.toString());
    }
}
