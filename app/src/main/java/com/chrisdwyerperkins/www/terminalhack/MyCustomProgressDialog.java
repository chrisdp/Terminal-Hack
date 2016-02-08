package com.chrisdwyerperkins.www.terminalhack;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Kaanu on 12/10/2015.
 */
public class MyCustomProgressDialog extends ProgressDialog {
    //// TODO: 12/10/2015 work on custom progress dialog class 
    public MyCustomProgressDialog(Context context) {
        super(context);
        setContentView(R.layout.custom_progress_dialog);
    }
}
