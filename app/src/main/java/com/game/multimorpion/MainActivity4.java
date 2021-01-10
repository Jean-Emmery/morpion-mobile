package com.game.multimorpion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.DrawableUtils;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
public class MainActivity4 extends AppCompatActivity implements View.OnClickListener {

    Context context;


    String playerName = "";
    String roomName = "";
    String role = "";
    String message = "";

    ValueEventListener buffer;

    FirebaseDatabase database;
    DatabaseReference messageRef;
    DatabaseReference roomRef;

    boolean turn = false;
    boolean soloPlayer = true;

    //Tableau à deux dimensions
    //plateu[colonne][ligne]
    //0 : case vide
    //1 : X
    //2 : O
    private int plateau[][] = new int [3][3];
    int plateauX;
    int plateauY;

    // 1 : X
    // 2 : O
    private int joueurEnCours = 1;

    private TextView tvJoueur;

    private ArrayList<Button> all_buttons = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        tvJoueur = (TextView) findViewById(R.id.joueur);

        Button bt1 = (Button) findViewById(R.id.bt1);
        Button bt2 = (Button) findViewById(R.id.bt2);
        Button bt3 = (Button) findViewById(R.id.bt3);
        Button bt4 = (Button) findViewById(R.id.bt4);
        Button bt5 = (Button) findViewById(R.id.bt5);
        Button bt6 = (Button) findViewById(R.id.bt6);
        Button bt7 = (Button) findViewById(R.id.bt7);
        Button bt8 = (Button) findViewById(R.id.bt8);
        Button bt9 = (Button) findViewById(R.id.bt9);

        this.context = this;

        all_buttons.add(bt1);
        all_buttons.add(bt2);
        all_buttons.add(bt3);
        all_buttons.add(bt4);
        all_buttons.add(bt5);
        all_buttons.add(bt6);
        all_buttons.add(bt7);
        all_buttons.add(bt8);
        all_buttons.add(bt9);

        Log.d("MAINACT4", "onCreate: test1");

        for (Button bt : all_buttons) {
            bt.setBackground(null);
            bt.setOnClickListener(this);
        }

        Log.d("MAINACT4", "onCreate: firebase.getinstance");
        database = FirebaseDatabase.getInstance();

        Log.d("MAINACT4", "gretshared preferences: test3");
        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        playerName = preferences.getString("playerName", "");

        Log.d("MAINACT4", "onCreate: test4");
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            roomName = extras.getString("roomName");
            if (roomName.equals(playerName)) {
                role = "X";
                turn = true;
            } else {
                role = "O";
                turn = false;
            }
            soloPlayer = extras.getBoolean("_soloPlayer");
            if (soloPlayer)
                turn = true;
        }
        Log.d("MAINACT4", "onCreate: test5, role = " + role);
        messageRef = database.getReference("rooms/" + roomName + "/message");
        addRoomEventListener();
        Log.d("MAINACT4", "onCreate: test6");
    }

    @Override
    public void onClick(View v) {
        Log.d("MAINACT3", "onClick: turn = " + turn);

        if (!turn)
            return;

        Log.d("MA4", "onClick: view " + v);
        if (v.getBackground() != null)
            return;

        switch (v.getId()) {
            case R.id.bt1:
                plateau [0][0] = joueurEnCours;
                plateauX = 0;
                plateauY = 0;
                break;
            case R.id.bt2:
                plateau [1][0] = joueurEnCours;
                plateauX = 1;
                plateauY = 0;
                break;
            case R.id.bt3:
                plateau [2][0] = joueurEnCours;
                plateauX = 2;
                plateauY = 0;
                break;
            case R.id.bt4:
                plateau [0][1] = joueurEnCours;
                plateauX = 0;
                plateauY = 1;
                break;
            case R.id.bt5:
                plateau [1][1] = joueurEnCours;
                plateauX = 1;
                plateauY = 1;
                break;
            case R.id.bt6:
                plateau [2][1] = joueurEnCours;
                plateauX = 2;
                plateauY = 1;
                break;
            case R.id.bt7:
                plateau [0][2] = joueurEnCours;
                plateauX = 0;
                plateauY = 2;
                break;
            case R.id.bt8:
                plateau [1][2] = joueurEnCours;
                plateauX = 1;
                plateauY = 2;
                break;
            case R.id.bt9:
                plateau [2][2] = joueurEnCours;
                plateauX = 2;
                plateauY = 2;
                break;
            default:
                return;
        }

        //drawPlayer(plateauX, plateauY, joueurEnCours);

        Drawable drawableJoueur;
        if (joueurEnCours == 1)
            drawableJoueur = ContextCompat.getDrawable(this, R.drawable.croix);
        else
            drawableJoueur = ContextCompat.getDrawable(this, R.drawable.rond);

        v.setBackground(drawableJoueur);

        Log.d("MAINACT4", "onClick: plateau X Y: " +plateauX + plateauY);
        if (joueurEnCours == 1) { //passe au joueur d'apres
            joueurEnCours = 2;
            tvJoueur.setText("O");
            message = plateauX + ":" + plateauY+":X";
            messageRef.setValue(message);
        }
        else {
            joueurEnCours = 1;
            tvJoueur.setText("X");
            message = plateauX + ":" + plateauY+":O";
            messageRef.setValue(message);
        }


        int res = checkWinner();
        displayAlertDialog(res);

    }

    // 0 : Partie non fini
    // 1 : X
    // 2 : O
    private int checkWinner() {

        for (int col = 0; col <= 2; col++) {
            if (plateau [col][0] != 0 && plateau[col][0] == plateau [col][1] && plateau [col][0] == plateau [col][2])
                return plateau[col][0];
        }
        for (int line = 0; line <= 2; line++) {
            if (plateau[0][line] != 0 && plateau[0][line] == plateau[1][line] && plateau[0][line] == plateau[2][line])
                return plateau[0][line];
        }

        if (plateau [0][0] != 0 && plateau [0][0] == plateau [1][1] && plateau [0][0] == plateau [2][2])
            return plateau [0][0];

        if (plateau [2][0] != 0 && plateau [2][0] == plateau [1][1] && plateau [2][0] == plateau [0][2])
            return plateau [2][0];

        boolean isPlateauPlein = true;
        for (int col = 0; col <= 2; col++) {
            for (int line = 0; line <= 2; line++) {
                if (plateau[col][line] == 0){
                    isPlateauPlein = false;
                    break;
                }
            }
            if (!isPlateauPlein)
                break;
        }

        if (isPlateauPlein)
            return 3;

        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("Résultat : ");
        //myRef.setValue(plateau);

        return 0;

    }

    private void displayAlertDialog(int res) {
        if (res == 0)
            return;

        String strToDisplay = "";

        if (res == 1) {
            strToDisplay = "Les X ont gagnées !";
        }

        if (res == 2)
            strToDisplay = "Les O ont gagnées !";

        if (res == 3)
            strToDisplay = "Egalité !";

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Fin de la partie");
        alertDialog.setMessage(strToDisplay);

        alertDialog.setNeutralButton("Recommencer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetGame();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    private void resetGame() {

        for (int col = 0; col <= 2; col++) {
            for (int line = 0; line <= 2; line++) {
                plateau[col][line] = 0;
            }
        }

        for (Button bt : all_buttons)  {
            bt.setBackground(null);
        }
    }
    private void addRoomEventListener() {
        Log.d("MAINACT4", "addRoomEventListener: test 1, role = " + role);
        messageRef.addValueEventListener(buffer = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String snapbuf = snapshot.getValue(String.class);
                Log.d("MA45", "onDataChange: snapbuf: " +snapbuf + " :role " + role + " turn = " + turn);
                //message received
                if (role.equals("X")) { // UR PHONE
                    if (snapbuf.contains(":O")) { //other player action
                        turn = true;
                        Log.d("MAINACT4", "onDataChange: string: " + snapshot.getValue(String.class));
                        plateauX = snapbuf.charAt(0) - 48;
                        plateauY = snapbuf.charAt(2) - 48;
                        Log.d("MAINACT4", "onDataChange: plateau XY: " + plateauX + plateauY);

                        Button buttonBuf = null;
                        if (plateauX == 0) {
                            if (plateauY == 0)
                                buttonBuf = (Button) findViewById(R.id.bt1);
                            else if (plateauY == 1)
                                buttonBuf = (Button) findViewById(R.id.bt4);
                            else
                                buttonBuf = (Button) findViewById(R.id.bt7);
                        } else if (plateauX == 1) {
                            if (plateauY == 0)
                                buttonBuf = (Button) findViewById(R.id.bt2);
                            else if (plateauY == 1)
                                buttonBuf = (Button) findViewById(R.id.bt5);
                            else
                                buttonBuf = (Button) findViewById(R.id.bt8);
                        } else if (plateauX == 2){
                            if (plateauY == 0)
                                buttonBuf = (Button) findViewById(R.id.bt3);
                            else if (plateauY == 1)
                                buttonBuf = (Button) findViewById(R.id.bt6);
                            else
                                buttonBuf = (Button) findViewById(R.id.bt9);
                        }
                        buttonBuf.performClick();

                        turn = true;
                        Toast.makeText(MainActivity4.this, "" + snapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
                    }
                    else if (snapbuf.contains(":X"))
                    {
                        //ur action
                        turn = false;
                    }
                } else { // if role == "O"
                    if (snapbuf.contains(":X")) {
                        turn = true;
                        Log.d("MAINACT4", "onDataChange: string: " + snapshot.getValue(String.class));
                        plateauX = snapbuf.charAt(0) - 48;
                        plateauY = snapbuf.charAt(2) - 48;
                        Log.d("MAINACT4", "onDataChange: plateau XY: " + plateauX + plateauY);

                        Button buttonBuf = null;
                        if (plateauX == 0) {
                            if (plateauY == 0)
                                buttonBuf = (Button) findViewById(R.id.bt1);
                            else if (plateauY == 1)
                                buttonBuf = (Button) findViewById(R.id.bt4);
                            else
                                buttonBuf = (Button) findViewById(R.id.bt7);
                        } else if (plateauX == 1) {
                            if (plateauY == 0)
                                buttonBuf = (Button) findViewById(R.id.bt2);
                            else if (plateauY == 1)
                                buttonBuf = (Button) findViewById(R.id.bt5);
                            else
                                buttonBuf = (Button) findViewById(R.id.bt8);
                        } else if (plateauX == 2){
                            if (plateauY == 0)
                                buttonBuf = (Button) findViewById(R.id.bt3);
                            else if (plateauY == 1)
                                buttonBuf = (Button) findViewById(R.id.bt6);
                            else
                                buttonBuf = (Button) findViewById(R.id.bt9);
                        }
                        buttonBuf.performClick();

                        Toast.makeText(MainActivity4.this, "" + snapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
                    }
                    else if (snapbuf.contains(":O")){
                        //other player phone other player turn
                        turn = false;
                        Toast.makeText(MainActivity4.this, "" + snapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Voulez vous vraiment quitter ?")
                .setTitle("Attention !")
                .setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        dialog.dismiss();
                    }
                }).setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();    }
}