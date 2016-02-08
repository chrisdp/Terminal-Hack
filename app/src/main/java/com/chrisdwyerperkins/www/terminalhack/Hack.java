package com.chrisdwyerperkins.www.terminalhack;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Kaanu on 12/2/2015.
 */
public class Hack implements Serializable {

    // constance
    private final int INT_DUD_GROUP_LENGTH = 8, INT_BUTTON_MAX = 408;

    // ArrayLists
    private ArrayList<String> arrChars, arrColHeaders, arrWordList, arrDudGroups, arrCurrentWords;
    private ArrayList<ArrayList<String>> arrButtonValues, arrHistory;

    // private vars
    private String strTargetPassword;
    private Boolean bolGameState;
    private Random random;
    private int intDudNumber, intCurrentLength, intAttempts;

    // non-serializable vars
    private transient Context context;

    // --------------------------------------------------------------------------- CONSTRUCTOR METHODS
    public Hack() { /* separate setUp method used because context can not be saved across runs */ }

    // --------------------------------------------------------------------------- PUBLIC METHODS
    public void setUp() {

        // populate main data ArrayLists with there string values
        arrChars = new ArrayList<String>(Arrays.asList("'", "|", "!", "@", "#", "$", "%", "^", "&", "*", "-", "_", "+", "=", ".", ";", ":", "?", ",", "/"));
        arrColHeaders = new ArrayList<String>(Arrays.asList("0x23B6", "0x7F9D", "0x6A6A", "0x8899", "0x6D3E", "0x83EA", "0x7E74", "0x320F", "0x05AB", "0x0D6D", "0x158A", "0x7C64", "0x456F", "0x4673", "0x4D4E", "0x17D1",
                "0x1A08", "0x601F", "0x676F", "0x4716", "0x1BAE", "0x1751", "0x6848", "0x3614", "0x1B57", "0x0451", "0x797D", "0x49FF", "0x244F", "0x653B", "0x66A5", "0x3EA2", "0x2A0E", "0x47E3", "0x54V2", "0xH1N1"));
        context.getResources().getStringArray(R.array.word_list);
        arrWordList = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.word_list)));
        arrDudGroups = new ArrayList<String>(Arrays.asList("<>", "[]", "{}", "()"));

        // construct random object
        random = new Random();
    }

    public void newGame() {

        // TODO: 12/11/2015 move this to a changeable value at the time of construction
        // set up default attempts
        intAttempts = 5;

        // create new ArrayLists (hacky way to empty... don't mind me)
        // TODO: 12/11/2015 setup arrays for clear() method
        arrButtonValues = new ArrayList<ArrayList<String>>();
        arrButtonValues.add(new ArrayList<String>());
        arrButtonValues.add(new ArrayList<String>());

        arrHistory = new ArrayList<ArrayList<String>>();
        arrHistory.add(new ArrayList<String>());
        arrHistory.add(new ArrayList<String>());
        arrHistory.add(new ArrayList<String>());

        // default game state and shuffle column headers and garbage chars
        bolGameState = false;
        shuffle(arrColHeaders);
        shuffle(arrChars);

        // make new ArrayList on first new game else clear the old one
        if (arrCurrentWords != null) { arrCurrentWords.clear(); }
        else { arrCurrentWords = new ArrayList<String>(); }

        // select random group of 12 words
        int temp = random.nextInt(3);
        //Log.d("chris", temp + "");
        if (temp == 0) {
            newWordList(0);
            //Log.d("chris", "case 1");
        } else if (temp == 1) {
            newWordList(12);
            //Log.d("chris", "case 1");
        } else {
            newWordList(24);
            //Log.d("chris", "case 1");
        }

        // shuffle the current list of words to be used then use the first one as the target password and shuffle again
        shuffle(arrCurrentWords);
        //Log.d("chris", arrCurrentWords.toString());
        strTargetPassword = arrCurrentWords.get(0);
        Log.d("chris", " ====================================================== " + strTargetPassword);
        shuffle(arrCurrentWords);

        // index to track the number of dud groups in this current game(amount varies per game)
        intDudNumber = 0;
        int currentWordIndex = arrCurrentWords.size();

        // populate the button values ArrayList
        // TODO: 12/11/2015 clear up loop so that manual breaking isn't required
        do {
            genStuff();
            if (intCurrentLength >= INT_BUTTON_MAX) { break; }

            if (currentWordIndex != 0) {
                currentWordIndex--;
                genWordGroup(currentWordIndex);
                updateCurrentLength();
            }

            genStuff();
            if (intCurrentLength >= INT_BUTTON_MAX) { break; }

            // 25% chance to "spawn" a dud group
            if (random.nextInt(100) < 25){
                genDud();
                updateCurrentLength();
            } else {
                genStuff();
            }
            if (intCurrentLength >= INT_BUTTON_MAX) { break; }

            genStuff();
            if (intCurrentLength >= INT_BUTTON_MAX)  { break; }
        } while (arrButtonValues.get(0).size() < INT_BUTTON_MAX);
    }

    public Boolean dudCheck(String tempy) {
        // check to see if the supplied string is a member of the Dud Groups
        Boolean flag = false;
        for (String test : arrDudGroups) { if (test.contains(tempy)) { flag = true; } }
        return flag;
    }

    public int compare(String tempy) {
        // var to track the amount of matching letters (FERMI!)
        int match = 0;
        // check for invalid incoming string
        if (tempy.length() != strTargetPassword.length()) { return match = -1; }
        else {
            // for every "Fermi" match ( :P ) increment match by one
            // check every index of target password to every index of the supplied string
            for (int i = 0; i < strTargetPassword.length(); i++) {
                if (strTargetPassword.charAt(i) == tempy.charAt(i)) { match++; }
            }
        }

        // if a full match is found change the game state to true
        // if not remove one attempt
        if (match == strTargetPassword.length()) {
            bolGameState = true;
        } else { if (intAttempts > 0) { intAttempts--; } }

        // add results the history of guesses
        arrHistory.get(0).add(tempy);
        arrHistory.get(1).add(String.valueOf(match));
        arrHistory.get(2).add(String.valueOf(bolGameState));

        return match;
    }

    // --------------------------------------------------------------------------- PRIVATE METHODS
    private void genStuff() {
        // check to see if we are about to exceed the total bounds and if we are gen crap for the amount
        // of free space remaining
        if (INT_BUTTON_MAX - intCurrentLength > 12) { genNormalCrap(); }
        else { genCrap(INT_BUTTON_MAX - intCurrentLength); }
    }

    private void genNormalCrap(){

        // create 5 to 10 random crap values
        int tempy;
        do {
            tempy = random.nextInt(10);
        } while (tempy < 4);
        genCrap(tempy);
        updateCurrentLength();
    }

    private void genWordGroup(int index){
        // get the requested word to make a group from
        String tempy = arrCurrentWords.get(index);

        // loop though every char and create a value attached to it
        for (int k = 0; k < tempy.length(); k++) {
            arrButtonValues.get(0).add(String.valueOf(tempy.charAt(k)));
            arrButtonValues.get(1).add(tempy);
        }
    }

    private void genDud() {

        // increment the dud index and get a random group to use
        intDudNumber++;
        shuffle(arrDudGroups);
        String group = arrDudGroups.get(random.nextInt(arrDudGroups.size()));

        // manually create the first and last vales but randomly fill its contents
        arrButtonValues.get(0).add(String.valueOf(group.charAt(0)));
        arrButtonValues.get(1).add("dud" + intDudNumber);
        for (int l = 0; l < INT_DUD_GROUP_LENGTH - 2; l++){
            arrButtonValues.get(0).add(arrChars.get(random.nextInt(arrChars.size())));
            arrButtonValues.get(1).add("dud" + intDudNumber);
        }
        arrButtonValues.get(0).add(String.valueOf(group.charAt(1)));
        arrButtonValues.get(1).add("dud" + intDudNumber);
    }

    private void genCrap(int amount){
        // generate a number of random crap chars based on the value passed in
        for (int i = 0; i < amount; i++) {
            arrButtonValues.get(0).add(arrChars.get(random.nextInt(arrChars.size())));
            arrButtonValues.get(1).add("null");
        }
    }

    private void newWordList(int range) {
        // currently I get a range of words from the ArrayList of all words
        // TODO: 12/11/2015 change this logic so that I get words similar to the target password. this would allow for less repetition
        //Log.d("chris", range + " " + (range + 12));
        for (int i = range; i < (range + 12); i++) {
            Log.d("chris", arrWordList.get(i).toString());
            arrCurrentWords.add(arrWordList.get(i));
        }
    }

    private void updateCurrentLength() { intCurrentLength = INT_BUTTON_MAX - arrButtonValues.get(0).size(); }

    private void shuffle(List<String> T){
        // loop though all index's of array
        int i = T.size()-1;
        while (i>0) { swap(T,i--,(int)(Math.random()*i)); }
    }

    private void swap(List<String> T, int I, int J) {
        // swap the current index item's place with the provided index
        String tempy = T.get(I);
        T.set(I, T.get(J));
        T.set(J, tempy);
    }
    // --------------------------------------------------------------------------- GET/SET METHODS
    public ArrayList<String> getColHeaders(){ return arrColHeaders; }
    public ArrayList<ArrayList<String>> getCurrentButtonValues() { return arrButtonValues; }
    public ArrayList<ArrayList<String>> getHistory() { return arrHistory;}
    public Boolean getGameState() { return bolGameState; }
    public int getAttempts(){ return intAttempts; }
    public void setAttempts(int amount) { intAttempts = amount; }
    public void setContext(Context context) { this.context = context; }
}
