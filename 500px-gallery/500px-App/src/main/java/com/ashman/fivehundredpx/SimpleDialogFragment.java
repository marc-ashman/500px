package com.ashman.fivehundredpx;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SimpleDialogFragment extends DialogFragment {
    private String message;

    public static SimpleDialogFragment newInstance(String message) {
        SimpleDialogFragment fragment = new SimpleDialogFragment();
        fragment.message = message;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage(message);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setNeutralButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return dialogBuilder.create();
    }
}
