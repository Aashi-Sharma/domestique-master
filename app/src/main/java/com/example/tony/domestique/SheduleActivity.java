package com.example.tony.domestique;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import com.google.firebase.database.DatabaseReference;

public class SheduleActivity extends AppCompatActivity {

    private RecyclerView mPeopleRV;
    private DatabaseReference mDatabase;
    //private FirebaseRecyclerAdapter<Shedules, SheduleActivity.SheduleViewHolder> mPeopleRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_shedule);
//
//        setTitle("News");
//
//        //"News" here will reflect what you have called your database in Firebase.
//        mDatabase = FirebaseDatabase.getInstance().getReference().child("News");
//        mDatabase.keepSynced(true);
//
//        mPeopleRV = (RecyclerView) findViewById(R.id.myRecycleView);
//
//        DatabaseReference personsRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals");
//        Query personsQuery = personsRef.orderByKey();
//
//        mPeopleRV.hasFixedSize();
//        mPeopleRV.setLayoutManager(new LinearLayoutManager(this));
//
//        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<Shedules>().setQuery(personsQuery, Shedules.class).build();
//
//        mPeopleRVAdapter = new FirebaseRecyclerAdapter<Shedules, SheduleActivity.SheduleViewHolder>(personsOptions) {
//            @Override
//            protected void onBindViewHolder(SheduleActivity.SheduleViewHolder holder, final int position, final Shedules model) {
//                holder.setTitle(model.getTitle());
//                holder.setDesc(model.getDesc());
//
//
//                holder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });
//            }
//
//            @Override
//            public SheduleActivity.SheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.shedule_rows, parent, false);
//
//                return new SheduleActivity.SheduleViewHolder(view);
//            }
//        };
//
//        mPeopleRV.setAdapter(mPeopleRVAdapter);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        mPeopleRVAdapter.startListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mPeopleRVAdapter.stopListening();
//
//
//    }
//
//    public static class SheduleViewHolder extends RecyclerView.ViewHolder{
//        View mView;
//        public SheduleViewHolder(View itemView){
//            super(itemView);
//            mView = itemView;
//        }
//        public void setTitle(String title){
//            TextView post_title = (TextView)mView.findViewById(R.id.post_title);
//            post_title.setText(title);
//        }
//        public void setDesc(String desc){
//            TextView post_desc = (TextView)mView.findViewById(R.id.post_desc);
//            post_desc.setText(desc);
//        }
//        public void setTotal(String desc){
//            TextView post_total = (TextView)mView.findViewById(R.id.post_total);
//            post_total.setText(desc);
//        }
//
//    }
    }
}
