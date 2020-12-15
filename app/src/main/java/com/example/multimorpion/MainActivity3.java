package com.example.multimorpion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity3 extends AppCompatActivity {

    Button button;
    Button button2;

    String playerName = "";
    String roomName = "";
    String role = "";
    String message = "";

    FirebaseDatabase database;
    DatabaseReference messageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        button = findViewById(R.id.button);
        button.setEnabled(false);

        button2 = findViewById(R.id.button2);

        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");

        Log.v("MainActivity3", "onCreate: TEST");
        Log.v("MainActivity3", "playerName: " + playerName);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            roomName = extras.getString("roomName");
            if (roomName.equals(playerName)) {
                role = "X";
            } else {
                role = "O";
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send message
                button.setEnabled(false);
                message = role + ":Poked!";
                messageRef.setValue(message);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MAINACTIVITY3", "onClick: setting message to EXIT");
                message = "EXIT";
                messageRef.setValue(message);
                Log.d("MAINACTIVITY3", "onClick: finish()");
                MainActivity3.this.finish();
            }
        });

        //listen for incoming messages
        messageRef = database.getReference("rooms/" + roomName + "/message");
        message = role  + ":Poked!";
        messageRef.setValue(message);
        Log.d("MAINACTIVITY3", "onCreate: 4");
        addRoomEventListener();
        Log.d("MAINACTIVITY3", "onCreate: 5");

    }

    private void addRoomEventListener() {
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue(String.class).contains("EXIT") && role.equals("O")) {
                    finish();
                }
                //message received
                if (role.equals("X") && !(snapshot.getValue(String.class).contains("EXIT"))) {
                    if (snapshot.getValue(String.class).contains("O:")) {
                        button.setEnabled(true);
                        Toast.makeText(MainActivity3.this, "" + snapshot.getValue(String.class).replace("O:", ""), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (snapshot.getValue(String.class).contains("X:")) {
                        button.setEnabled(true);
                        Toast.makeText(MainActivity3.this, "" + snapshot.getValue(String.class).replace("X:", ""), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // error - retry
                messageRef.setValue(message);
            }
        });
    }
}