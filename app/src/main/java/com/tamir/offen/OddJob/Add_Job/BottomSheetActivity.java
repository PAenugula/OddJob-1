package com.tamir.offen.OddJob.Add_Job;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tamir.offen.OddJob.AddJob;
import com.tamir.offen.OddJob.Add_Job.AddJobHandler;
import com.tamir.offen.OddJob.Map.map;
import com.tamir.offen.OddJob.Messaging.ChatSelectionActivity;
import com.tamir.offen.OddJob.Messaging.ChattingActivity;
import com.tamir.offen.OddJob.R;
import com.tamir.offen.OddJob.User_Registration.User;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;

public class BottomSheetActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textViewTitle, textViewPrice, textViewStartDate, textViewEndDate, textViewStartTime, textViewEndTime, textViewDesc;
    private ImageView imageViewIcon;
    private Button btnAcceptJob, btnMessage, btnBackToMap, btnDelete, btnMessageOnly;
    private map mMap = new map();
    private AddJobHandler curJob = mMap.curJob;
    private String sender = curJob.getSender();
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseJobs = mMap.databaseReference;
    private DatabaseReference databaseUsers;
    private String databaseID;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder alertDialogBuilder, acceptJobDialog;
    private DatabaseReference NotificationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        textViewStartTime = findViewById(R.id.textViewStartTime);
        textViewEndTime = findViewById(R.id.textViewEndTime);
        textViewDesc = findViewById(R.id.textViewDesc);
        btnAcceptJob = findViewById(R.id.btnAcceptJob);
        btnMessage = findViewById(R.id.btnMessage);
        btnMessageOnly = findViewById(R.id.btnMessageOnly);
        btnBackToMap = findViewById(R.id.btnBackToMap);
        btnDelete = findViewById(R.id.btnDelete);
        imageViewIcon = findViewById(R.id.imageViewIcon);
        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notification");
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("users");
        NotificationsReference.keepSynced(true);

        alertDialogBuilder = new AlertDialog.Builder(this);
        setUpAlertDialog();
        acceptJobDialog = new AlertDialog.Builder(this);
        setUpAcceptJobDialog();

        textViewTitle.setText(curJob.getTitle());
        textViewPrice.setText(curJob.getPrice());
        textViewStartDate.setText(curJob.getDates().get(0));
        textViewEndDate.setText(curJob.getDates().get(1));
        //textViewStartTime.setText(curJob.gettime().get(0));
        //textViewEndTime.setText(curJob.gettime().get(1));
        textViewDesc.setText(curJob.getDesc());

        progressDialog = new ProgressDialog(this);

        updateTagIcon();

        firebaseAuth = FirebaseAuth.getInstance();

        if(userIsOwnerOfJob()) {
            btnDelete.setVisibility(View.VISIBLE);
            btnAcceptJob.setVisibility(View.INVISIBLE);
            btnMessage.setVisibility(View.INVISIBLE);
            btnMessageOnly.setVisibility(View.INVISIBLE);
        }else if(curJob.getAccepterID().equals(firebaseAuth.getCurrentUser().getUid())){
            btnMessageOnly.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            btnAcceptJob.setVisibility(View.INVISIBLE);
            btnMessage.setVisibility(View.INVISIBLE);
        }else{
            btnMessageOnly.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            btnAcceptJob.setVisibility(View.VISIBLE);
            btnMessage.setVisibility(View.VISIBLE);
        }

        btnBackToMap.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnAcceptJob.setOnClickListener(this);
        btnMessage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnBackToMap) onBackPressed();
        if(view == btnDelete) alertDialogBuilder.show();
        if(view == btnAcceptJob) acceptJobDialog.show();
        if(view == btnMessage) {
            Toast.makeText(BottomSheetActivity.this, "Message", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTagIcon() {
        String tag = curJob.getTag();
        if(tag.equals("Technology")) imageViewIcon.setImageResource(R.drawable.rtech);
        if(tag.equals("Transportation")) imageViewIcon.setImageResource(R.drawable.rtrans);
        if(tag.equals("Home / Yard")) imageViewIcon.setImageResource(R.drawable.rhome);
        if(tag.equals("Child / Pet Care")) imageViewIcon.setImageResource(R.drawable.rcare);
        if(tag.equals("Education")) imageViewIcon.setImageResource(R.drawable.redu);
        if(tag.equals("Other")) imageViewIcon.setImageResource(R.drawable.rother);
    }

    private boolean userIsOwnerOfJob() {
        if(firebaseAuth.getCurrentUser().getEmail().equals(sender)) return true;
        return false;
    }

    private void deleteJob() {
        progressDialog.setMessage("Deleting '" + curJob.getTitle() + "'");
        progressDialog.show();

        databaseJobs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    AddJobHandler newJob = jobSnapshot.getValue(AddJobHandler.class);
                    if(newJob.getID().equals(curJob.getID())) {
                        databaseID = jobSnapshot.getKey();
                        DatabaseReference databaseReferenceJobs = FirebaseDatabase.getInstance().getReference("Jobs").child(databaseID);
                        databaseReferenceJobs.removeValue();
                        return;
                    }
                    progressDialog.dismiss();
                    Intent intent = new Intent(BottomSheetActivity.this, map.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setUpAlertDialog() {
        alertDialogBuilder.setTitle("Delete '" + curJob.getTitle() + "'?");
        alertDialogBuilder.setMessage("Are you sure you want to delete your OddJob?");
        alertDialogBuilder.setIcon(R.drawable.ic_delete);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteJob();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private void setUpAcceptJobDialog() {
        acceptJobDialog.setTitle("Accept '" + curJob.getTitle() + "'?");
        acceptJobDialog.setMessage("Are you sure you want to accept this OddJob?");
        acceptJobDialog.setIcon(R.drawable.ic_check);

        acceptJobDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptOddJob();
            }
        });

        acceptJobDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
    }

    private void acceptOddJob() {

        curJob.setAccepterID(firebaseAuth.getCurrentUser().getUid());
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot: dataSnapshot.getChildren()){
                    User user = userSnapshot.getValue(User.class);
                    String chat_id = userSnapshot.getKey();
                    if(user.getId ().equals(curJob.getOjID())){
                        Toast.makeText(BottomSheetActivity.this, user.getName(), Toast.LENGTH_SHORT).show();
                        Intent chatIntent = new Intent(BottomSheetActivity.this, ChattingActivity.class);
                        chatIntent.putExtra("chat_id",user);
                        chatIntent.putExtra("oddjob_id",curJob.getAccepterID());
                        chatIntent.putExtra("oddjob",curJob.getID());
                        startActivity(chatIntent);
                        break;
                    }else{

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public String oddjobIDd(final String string){
        final String[] id = {};
        databaseJobs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot jobsnapshot: dataSnapshot.getChildren()){
                    if(jobsnapshot.child("id").equals(string)){
                        id[0] = jobsnapshot.toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return id[0];
    }
}
