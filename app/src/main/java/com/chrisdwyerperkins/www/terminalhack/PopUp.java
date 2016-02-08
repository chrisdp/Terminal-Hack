package com.chrisdwyerperkins.www.terminalhack;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Kaanu on 12/7/2015.
 */
public class PopUp extends AppCompatActivity {

    // views
    private Button btnBack, btnNo;
    private TextView txtOutput, txtTitle, txtDot;

    // private vars
    private int intWidth, intHeight;
    private String strMessage, strDot, strNewGame;

    // constance
    private final long OUTPUT_TICK = 10, DOT_TICK = 150, NEWGAME_TICK = 70;

    // custom class(s)
    private UIHelp uiHelp;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        // construct custom UIHelper object
        uiHelp = new UIHelp(getBaseContext());

        // get screen dimensions and do some work with it
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        intWidth = size.x;
        intHeight = size.y;

        // store view references for later use
        btnBack = (Button) findViewById(R.id.btnBack);
        btnNo = (Button) findViewById(R.id.btn_no);
        txtOutput = (TextView) findViewById(R.id.txtOutput);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtDot = (TextView) findViewById(R.id.txtDot);

        // construct MrBunbun!!!! (aka intent)
        Bundle mrBunbun = getIntent().getExtras();
        Boolean result = mrBunbun.getBoolean("mrBunbun");

        // check for winning game or losing game and use appropriate message
        if (result) {
            strMessage = getString(R.string.pop_up_win);
        } else {
            strMessage = getString(R.string.pop_up_lose);
        }

        // more string references
        strDot = "\n> . . . . .";
        strNewGame = getString(R.string.pop_up_new_game);

        // wire up event handlers
        btnBack.setOnClickListener(onClickListener);
        btnNo.setOnClickListener(onClickListener);

        // set up ui styles
        uiHelp.newColor(btnBack, btnNo);
        uiHelp.newColor(txtOutput, txtTitle, txtDot);
        uiHelp.newTypeface(btnBack, btnNo);
        uiHelp.newTypeface(txtOutput, txtTitle, txtDot);
        uiHelp.newBColor(btnBack, btnNo);
        uiHelp.newBColor(txtOutput, txtTitle, txtDot);
        uiHelp.newTxtSize(bestSize(), btnBack, btnNo);
        uiHelp.newTxtSize(bestSize(), txtOutput, txtTitle, txtDot);

        // display message to user
        populateMessage();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            // construct intent to return result
            Intent intent = new Intent();

            if (v == btnBack) {
                intent.putExtra("newGameRequest","yes");
            } else if (v == btnNo) {
                intent.putExtra("newGameRequest", "no");
            }

            // add the intent with the result and finish this dialog
            setResult(2,intent);
            finish();
        }
    };

    private void populateMessage(){

        // construct CountDownTimer to animate the output message
        new CountDownTimer(3000, OUTPUT_TICK) {

            int i = 0;
            public void onTick(long millisUntilFinished) {

                // manually cancel CountDownTime and start the next animation
                if (i == strMessage.length()) {
                    cancel();
                    dotDotDot();
                } else {
                    // add one character at a time...
                    txtOutput.setText(txtOutput.getText() + String.valueOf(strMessage.charAt(i++)));
                }
            }

            public void onFinish() {
                // // TODO: 12/11/2015 need to find a way to get around manually canceling the timer
                dotDotDot();
            }
        }.start();
    }

    private void dotDotDot(){

        // construct CountDownTimer to animate the output message
        new CountDownTimer(3000, DOT_TICK) {
            int i = 0;

            public void onTick(long millisUntilFinished) {

                // manually cancel CountDownTime and start the next animation
                if (i == strDot.length()) {
                    cancel();
                    newGame();
                } else {
                    // add one character at a time...
                    txtDot.setText(txtDot.getText() + String.valueOf(strDot.charAt(i++)));
                }
            }

            public void onFinish() {
                // // TODO: 12/11/2015 need to find a way to get around manually canceling the timer
                newGame();
            }
        }.start();
    }

    private void newGame(){

        // construct CountDownTimer to animate the output message
        new CountDownTimer(30000, NEWGAME_TICK) {

            // i = index of current char, d = delay before starting animation
            int i = 0, d = 0;
            Boolean flag = true;
            public void onTick(long millisUntilFinished) {


                if (d > 20) {
                    // once delay is complete clear the last TextArea
                    if (flag) {
                        txtDot.setText("\n");
                        flag = false;
                    }
                    // manually cancel CountDownTime and show/enable the buttons
                    if (i == strNewGame.length()) {
                        cancel();
                        btnBack.setVisibility(View.VISIBLE);
                        btnBack.setEnabled(true);
                        btnNo.setVisibility(View.VISIBLE);
                        btnNo.setEnabled(true);
                    } else {
                        // add one character at a time...
                        txtDot.setText(txtDot.getText() + String.valueOf(strNewGame.charAt(i++)));
                    }
                } else {
                    d++;
                }
            }

            public void onFinish() {
                // // TODO: 12/11/2015 need to find a way to get around manually canceling the timer
            }
        }.start();
    }

    private int bestSize(){
        // hacky stuff needs major fixing
        // TODO: 12/11/2015 come up with better solution to different screen sizes
        if (intWidth > intHeight) return (intHeight / 50);
        else return (intWidth / 60);
    }
}
