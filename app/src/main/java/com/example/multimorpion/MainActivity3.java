package com.example.multimorpion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

    boolean gameOver = false;
    boolean soloPlayer = true;
    ValueEventListener buffer;

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

        gameOver = false;
        soloPlayer = true;

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
                startActivity(new Intent(getApplicationContext(), MainActivity4.class));
                MainActivity3.this.finish();
            }
        });

        button2.setEnabled(false);
        button2.postDelayed(new Runnable() {
            @Override
            public void run() {
                button2.setEnabled(true);
            }
        }, 1000);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soloPlayer) {
                    messageRef.removeEventListener(buffer);
                    gameOver = true;
                    messageRef.setValue("EXITED");
                    startActivity(new Intent(getApplicationContext(), MainActivity2.class));
                    MainActivity3.this.finish();
                }
                messageRef.removeEventListener(buffer);
                messageRef.setValue("EXIT");
                addRoomEventListener();
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

    private void waiting() {
        ;
    }

    private void addRoomEventListener() {
        messageRef.addValueEventListener(buffer = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!gameOver) {
                    if (snapshot.getValue(String.class).contains("EXIT") && role.equals("O")) {
                        gameOver = true;
                        messageRef.setValue("EXITED");
                        startActivity(new Intent(getApplicationContext(), MainActivity2.class));
                        MainActivity3.this.finish();
                        Toast.makeText(MainActivity3.this, "YOU WON !", Toast.LENGTH_SHORT).show();
                    }
                    if (snapshot.getValue(String.class).contains("EXITED")) {
                        gameOver = true;
                        startActivity(new Intent(getApplicationContext(), MainActivity2.class));
                        MainActivity3.this.finish();
                        Toast.makeText(MainActivity3.this, "YOU LOST !", Toast.LENGTH_SHORT).show();

                    }
                    //message received
                    if (role.equals("X")) {
                        if (snapshot.getValue(String.class).contains("O:")) {
                            button.setEnabled(true);
                            Toast.makeText(MainActivity3.this, "" + snapshot.getValue(String.class).replace("O:", "fdp"), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        soloPlayer = false;
                        if (snapshot.getValue(String.class).contains("X:")) {
                            button.setEnabled(true);
                            Toast.makeText(MainActivity3.this, "" + snapshot.getValue(String.class).replace("X:", "conar"), Toast.LENGTH_SHORT).show();
                        }
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