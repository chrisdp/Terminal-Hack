package com.chrisdwyerperkins.www.terminalhack;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Kaanu on 12/8/2015.
 */
public class UIHelp {

    // private vars
    private Context context;
    private Typeface typeface;

    public UIHelp(Context context){
        // set up defaults
        this.context = context;
        // TODO: 12/11/2015 move this to a parameter to be passed in
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Overseer.ttf");
    }

    // font colors
    public void newColor(TextView... params){ for (TextView textView : params) textView.setTextColor(context.getResources().getColor(R.color.new_color)); }
    public void newColor(Button... params){for (Button button : params) button.setTextColor(context.getResources().getColor(R.color.new_color)); }

    // background colors
    public void newBColor(TextView... params){ for (TextView textView : params) textView.setBackgroundColor(context.getResources().getColor(R.color.new_b_color)); }
    public void newBColor(Button... params){for (Button button : params) button.setBackgroundColor(context.getResources().getColor(R.color.new_b_color)); }

    // test size
    public void newTxtSize(int I, TextView... params){ for (TextView textView : params) textView.setTextSize(I); }
    public void newTxtSize(int I, Button... params){for (Button button : params) button.setTextSize(I); }

    // typefaces
    public void newTypeface(TextView... params){for (TextView textView : params) textView.setTypeface(typeface); }
    public void newTypeface(Button... params){ for (Button button : params) button.setTypeface(typeface); }
}
