package com.chrisdwyerperkins.www.terminalhack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.optimizely.Optimizely;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    // constance
    private final String FILE_NAME = "hack.dat";
    private final int COLUMN = 27, MAX = 459, ROWS = 17;

    // private vars
    private int intWidth, intHeight, intWidthP, intHeightP;

    // View vars
    private TextView lblAttempts;
    private ArrayList<TextView> arrColHeaderTViews, arrFeedBackTViews;
    private Button btnNew;

    // custom classes
    private Hack hack;
    private UIHelp uiHelp;
    private MyCustomProgressDialog myCustomProgressDialog;

    // other vars
    protected File file;
    private GridLayout gridLayout;
    private ArrayList<ArrayList<String>> arrButtonValues, arrHistory;
    private ArrayList<String> arrColHeaderList;
    private Bundle msBunbun;
    private Intent intent;

    // ============================================================ INTERFACE SETUP AND CONSTRUCTION
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_main);
        // get reference to file/create file
        file = getFileStreamPath(FILE_NAME);

        // get screen dimensions and do some work with it
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        intWidth = size.x;
        intHeight = size.y;
        intWidthP = (int)(Math.floor(intWidth / 36));
        intHeightP = (int)(Math.floor(intHeight / 20));

        /*
        Log.d("chris", "textSize " + (intHeightP / 3));
        Log.d("chris", "intWidthP " + (intWidthP));
        Log.d("chris", "intHeightP " + (intHeightP));
        Log.d("chris", "intWidth " + (intWidth));
        Log.d("chris", "intHeight " + (intHeight));
        */

        // set up needed views and populate the grid
        initialize();
        populateGrid();
        //// TODO: 12/10/2015 complete work on custom progress dialog 
        /*
        myCustomProgressDialog = new MyCustomProgressDialog(MainActivity.this);
        myCustomProgressDialog.setMessage("Loading please wait.");
        myCustomProgressDialog.show();*/
        try {
            if (file.exists()) {
                // load the existing file
                FileInputStream fileInputStream = openFileInput(FILE_NAME);
                ObjectInputStream objectinputstream = new ObjectInputStream(fileInputStream);
                hack = (Hack) objectinputstream.readObject();
                objectinputstream.close();

                // get current game information
                hack.setContext(getBaseContext());
                arrHistory = hack.getHistory();
                arrColHeaderList = hack.getColHeaders();
                arrButtonValues = hack.getCurrentButtonValues();

                // update the ui
                updateButtons();
                updateFeedback();
                updateHeaders();
                updateAttempts();
                if (hack.getGameState()) { btnEnableState(!hack.getGameState()); }

            } else {

                // construct new hack object if file dose not exist
                // and start a new game
                hack = new Hack();
                hack.setContext(getBaseContext());
                hack.setUp();
                startNew();
                arrHistory = hack.getHistory();
            }
        } catch (Exception e) {  Log.d("chris", "!!! Exception", e);
        } finally {
            // // TODO: 12/11/2015 is there anything i need to do here?
        }
        // leave me at the end of the onCreate. used for Crashlytics and such..
        Optimizely.startOptimizelyWithAPIToken("AANJbMQBzJOOrVogDSJGXhYShCEx32WF~4059546151", getApplication());
    }

    private void initialize(){
        // set up my UIHelper class
        uiHelp = new UIHelp(getBaseContext());

        // set up my ArrayLists
        arrColHeaderTViews = new ArrayList<>();
        arrFeedBackTViews = new ArrayList<TextView>();

        // store layout and view objects for later use
        gridLayout = (GridLayout) findViewById(R.id.grid_layout);
        lblAttempts = (TextView) findViewById(R.id.lblAttempts);
        btnNew = (Button) findViewById(R.id.btnNew);

        // populate app headers
        TextView lblTitle = (TextView) findViewById(R.id.lblTitle);
        lblTitle.setText(" " + getResources().getString(R.string.lbl_header_line_1));
        lblTitle.setText(lblTitle.getText() + "\n" + " " + getResources().getString(R.string.lbl_header_line_2));

        // wire up event handlers and styles via UIHelp
        btnNew.setOnClickListener(onClickListener);
        uiHelp.newTxtSize(bestSize(), btnNew);
        uiHelp.newColor(btnNew);
        uiHelp.newBColor(btnNew);
        uiHelp.newTypeface(btnNew);

        uiHelp.newTxtSize(bestSize(), lblTitle, lblAttempts);
        uiHelp.newColor(lblTitle, lblAttempts);
        uiHelp.newBColor(lblTitle, lblAttempts);
        uiHelp.newTypeface(lblTitle, lblAttempts);

        // construct MsBunbun! (aka my bundle) and my intent object
        msBunbun = new Bundle();
        intent = new Intent("com.chrisdwyerperkins.www.PopUp");

        // define column and row limits on the GridView
        gridLayout.setColumnCount(COLUMN);
        gridLayout.setRowCount(ROWS);

        //construct a few (53?) TextViews for the column headers and for the feedback area
        TextView textView;
        for (int h = 0; h < 36; h++) {
            textView = new TextView(this);
            //textView.setText(" " + arrColHeaderList.get(h) + " ");
            uiHelp.newColor(textView);
            uiHelp.newBColor(textView);
            uiHelp.newTypeface(textView);
            uiHelp.newTxtSize(bestSize(), textView);
            arrColHeaderTViews.add(textView);
        }

        for (int i = 0; i < ROWS; i++) {
            textView = new TextView(this);
            uiHelp.newColor(textView);
            uiHelp.newBColor(textView);
            uiHelp.newTypeface(textView);
            uiHelp.newTxtSize(bestSize(), textView);
            arrFeedBackTViews.add(textView);
        }

    }

    private void populateGrid(){
        // set up and populate the GridLayout
        // loop contains all the vales i need to track
        // i = total iterations, h = header, m1 = first half button, m2 = second half button, f = feedback, c = column, r = row
        for (int i = 0, h = 0, m1 = 0, m2 = 204, f = 0, c = 0, r = 0; i < MAX; i++) {

            //Log.d("chris", "Column: " + c + " Row: " + r);

            //construct new LayoutParams for each View being added (happens once per iteration)
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(-5, -5, 0, 0);
            params.setGravity(Gravity.CENTER);
            params.columnSpec = GridLayout.spec(c);
            params.rowSpec = GridLayout.spec(r);


            if ((c == 0) || (c == 13)) {
                // prepare a header TextView to be added and then add it to GridLayout
                params.columnSpec = GridLayout.spec(0);
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                gridLayout.addView(arrColHeaderTViews.get(h));
                h++;
                c++;
            } else if (c == COLUMN - 1){
                // prepare a feedback TextView to be added and then add it to GridLayout
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                gridLayout.addView(arrFeedBackTViews.get(f));
                f++;
                r++;
                c = 0;
            } else {
                // prepare a Button to be added and then add it to GridLayout
                Button tB = new Button(this);
                params.height = intHeightP;
                params.width = intWidthP;
                uiHelp.newTxtSize(bestSize(), tB);
                tB.setLayoutParams(params);
                tB.setPadding(0, 0, 0, 0);
                tB.setOnClickListener(onClickListener);
                tB.setText(".");
                uiHelp.newColor(tB);
                uiHelp.newBColor(tB);
                uiHelp.newTypeface(tB);
                // check to see what id to give the button
                // buttons in c 1 - 12 get a index between 0 and 203, buttons in c 14 - 26 get a index between 204-407
                if ((c > 0) && (c < 13)){
                    tB.setId(m1);
                    m1++;
                } else {
                    tB.setId(m2);
                    m2++;
                }
                gridLayout.addView(tB);
                c++;
            }
        }
    }

    // ====================================================================================== SAVING
    @Override
    public void onPause() {
        super.onPause();
        try {
            // construct output streams and save hack object
            FileOutputStream fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(hack);
            // make sure all data is written to the file
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==============================================================================PRIVATE METHODS
    private void startNew() {
        // construct new worker thread
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // create new game from the worker thread
                    hack.newGame();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // update ui elements after the new game has been created
                            arrColHeaderList = hack.getColHeaders();
                            arrButtonValues = hack.getCurrentButtonValues();
                            clearBackGrounds();
                            updateButtons();
                            updateAttempts();
                            updateHeaders();
                            //for (TextView textView : arrFeedBackTViews) { textView.setText("             "); }
                            clearFeedback();
                            btnEnableState(!hack.getGameState());
                        }
                    });
                } catch (Exception e){
                    Log.d("chris", "!!! Exception", e);
                }
            }
        });
        // start the thread
        thread.start();
    }

    private void validate(final String tempy){
        // prepare data to be used by the worker thread
        final int[] response = new int[1];

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    // comapre the guess to the target password on worker thread
                    response[0] = hack.compare(tempy);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d("chris", "responce: " + String.valueOf(response[0]) + " gameState: " + String.valueOf(hack.getGameState()));
                            // update feed back to user and check for winning guess
                            if ((response[0] > -1) && (!hack.getGameState())){
                                // update feedback on a non-winning guess
                                updateFeedback();
                                updateAttempts();
                            } else if ((response[0] > -1) && (hack.getGameState())) {
                                // call popup activity on winning game and update feedback
                                updateFeedback();
                                msBunbun.putBoolean("mrBunbun", true);
                                startActivityForResult(intent.putExtras(msBunbun), 2);
                            }
                        }
                    });
                } catch (Exception e){
                    Log.d("chris", "!!! Exception", e);
                }
            }
        });
        // start thread
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // make sure request was from out popup activity
        if (requestCode == 2) {
            // check to see if user click outside of activity
            if (data != null) {
                if (data.getStringExtra("newGameRequest").equals("yes")) {
                    // start a new game if the user clicked ok
                    startNew();
                } else {
                    // disable buttons if user click no
                    btnEnableState(hack.getGameState());
                }
            }
        }
    }

    private List<View> findViewWithTag(ViewGroup root, Object tag){
        // prepare list to hold views with requested tag
        List<View> allViews = new ArrayList<View>();
        // get count of all children in the provided ViewGroup
        int childCount = root.getChildCount();
        // loop thought all children
        for(int i=0; i<childCount; i++){
            View childView = root.getChildAt(i);
            if(childView instanceof ViewGroup){
                // look into children for views with the same tag (children of children)
                allViews.addAll(findViewWithTag((ViewGroup) childView, tag));
            }
            else{
                // add view with provided tag to List
                Object tagView = childView.getTag();
                if(tagView != null && tagView.equals(tag))
                    allViews.add(childView);
            }
        }
        return allViews;
    }

    private int bestSize(){
        // hacky stuff needs major fixing
        // TODO: 12/11/2015 come up with better solution to different screen sizes
        if (intWidth > intHeight) return (intHeight / 50);
        else return (intWidth / 60);
    }

    // interesting method to convert a px value into a dp value
    // TODO: 12/11/2015 this method may help me fix the problems with the above method, keeping it here for when I have time to review further
    private int toDP(int I) { return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, I, getResources().getDisplayMetrics()); }

    // ============================================================================== EVENT HANDLING
    private View.OnClickListener onClickListener = new View.OnClickListener(){
        public void onClick(View v){
            // get reference to clicked view
            Button button = (Button) v;

            // find out what button triggered the event
            if (button == btnNew) {
                startNew();
            } else {
                // get reference to the tag of the clicked button and reset the backgrounds of all buttons
                String tag = button.getTag().toString();
                Log.d("chris", tag);
                clearBackGrounds();


                if (tag.equals("null")) {
                    // button was not part of a dud or word group, highlight only this button
                    highlight(button);
                } else if (tag.startsWith("dud")) {
                    // button was part of a dud group
                    if ((hack.dudCheck(button.getText().toString()))) {
                        // button was a leading or trailing bracket, highlight all from same dud group
                        highlightViews(tag);
                    } else {
                        // button was not leading for trailing, highlight only this button
                        highlight(button);
                    }
                } else {
                    // button was part of a word group, highlight all from the same group and check
                    // for winning guess and provide user feedback
                    highlightViews(tag);
                    validate(tag);
                    updateAttempts();
                }
            }
            // keep buttons enable state up to date
            btnEnableState(!hack.getGameState());
        }
    };

    // ========================================================================== INTERFACE UPDATING
    private void updateButtons(){
        // loop though all buttons and set there default text (for constant visual look)
        for (int i = 0; i < 408; i++) { ((Button) findViewById(i)).setText("."); }

        // disable buttons while values are being updated
        btnEnableState(false);
        btnNew.setEnabled(false);

        // construct a CountDownTimer to animate the population of button vaules
        new CountDownTimer(30000, 1) {

            // index's to track both columns of buttons
            int temp1 = 0, temp2 = 204;

            public void onTick(long millisUntilFinished) {

                if (temp1 >= 204) {
                    // manual cancel after all buttons have been updated
                    cancel();
                } else {
                    // update two buttons on every tick
                    View v = findViewById(temp1);
                    ((Button) v).setText(arrButtonValues.get(0).get(temp1));
                    v.setTag(arrButtonValues.get(1).get(temp1));
                    temp1++;

                    v = findViewById(temp2);
                    ((Button) v).setText(arrButtonValues.get(0).get(temp2));
                    v.setTag(arrButtonValues.get(1).get(temp2));
                    temp2++;
                }
            }

            public void onFinish() {
                // nothing to do here as I manually cancel the CountDownTimer
            }
        }.start();

        // now that buttons are done populating, enable buttons
        btnEnableState(!hack.getGameState());
        btnNew.setEnabled(true);
        //// TODO: 12/11/2015 dismis custom progress dialog here to be added 
        //myCustomProgressDialog.dismiss();
    }

    private void updateHeaders() {
        // populate column headers
        for (int h = 0; h < arrColHeaderTViews.size() - 1; h++) {
            arrColHeaderTViews.get(h).setText(" " + arrColHeaderList.get(h) + " ");
        }
    }

    private void updateFeedback() {
        //Log.d("chris", "wtf?");
        // get reference to most resent guess history from hack object
        arrHistory = hack.getHistory();
        int temp = arrFeedBackTViews.size() - 1;

        for (int i = arrHistory.get(0).size() - 1; i > -1; i--) {

            // manually exit loop to make sure bounds aren't exceeded
            // populate feed back views bottom to top

            if (temp < 0) break;
            arrFeedBackTViews.get(temp--).setText(" >" + arrHistory.get(1).get(i) + "/7 " + getString(R.string.feedback_likeness));
            if (temp < 0) break;
            // don't mind this ugly =D it dose stuff =D
            arrFeedBackTViews.get(temp--).setText(" " + (Boolean.parseBoolean(arrHistory.get(2).get(i)) ? getString(R.string.Feedback_match_1) : getString(R.string.feedback_denied)));
            if (temp < 0) break;
            arrFeedBackTViews.get(temp--).setText(" >" + arrHistory.get(0).get(i).toUpperCase());
        }
    }

    private void updateAttempts() {

        // get reference to the amount of attempts
        int amount = hack.getAttempts();


        if (amount == 0) {
            // no more attempts, disable buttons and display popup
            btnEnableState(false);
            msBunbun.putBoolean("mrBunbun", false);
            startActivityForResult(intent.putExtras(msBunbun), 2);
        }

        // update the attempts TextView
        lblAttempts.setText(" " + amount + " " + getResources().getString(R.string.lbl_attempts));
        for (int i = 0; i < amount; i++) { lblAttempts.setText(lblAttempts.getText() + " " + getResources().getString(R.string.attempt_block)); }
    }

    // empty the Attempts TextView
    private void clearFeedback() { for (TextView textView : arrFeedBackTViews) { textView.setText(""); }}

    private void clearBackGrounds(){
        // loop though all buttons and set there background to default
        for (int i = 0; i < 407; i++) {
            Button temp = (Button) findViewById(i);
            uiHelp.newBColor(temp);
        }
    }

    private void btnEnableState(Boolean state){
        // loop though all buttons and set there enable state to the value passed in
        for (int i = 0; i < 407; i++) {
            Button temp = (Button) findViewById(i);
            temp.setEnabled(state);
        }
    }

    private void highlightViews(String tag) {
        // highlight all views with the same tag
        List<View> tagedViews = findViewWithTag(gridLayout, tag);
        for (View temp : tagedViews) { highlight((Button)temp); }
    }

    // highlight the button(s) passed into this method
    private void highlight(Button... params){for (Button button : params) button.setBackgroundColor(getResources().getColor(R.color.highlight)); }

    //// TODO: 12/10/2015 clean up before sean sees it!
/*
    private class MyCustomProgressDialog extends ProgressDialog {
        public MyCustomProgressDialog(Context context) {
            super(context, R.style.PopupTheme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.custom_progress_dialog);
        }
    }*/
    

    // getColor(int) used because getColor(int, theme) dose not support api lower then 21 current min is 19
    /*
    private void newColor(TextView... params){ for (TextView textView : params) textView.setTextColor(getResources().getColor(R.color.new_color)); }
    private void newColor(Button... params){for (Button button : params) button.setTextColor(getResources().getColor(R.color.new_color)); }

    private void newBColor(TextView... params){ for (TextView textView : params) textView.setBackgroundColor(getResources().getColor(R.color.new_b_color)); }
    private void newBColor(Button... params){for (Button button : params) button.setBackgroundColor(getResources().getColor(R.color.new_b_color)); }

    private void newTxtSize(TextView... params){ for (TextView textView : params) textView.setTextSize(bestSize()); }
    private void newTxtSize(Button... params){for (Button button : params) button.setTextSize(bestSize()); }

    private void newTypeface(TextView... params){ for (TextView textView : params) textView.setTypeface(typeface); }
    private void newTypeface(Button... params){ for (Button button : params) button.setTypeface(typeface); }
*/


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
