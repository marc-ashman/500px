package com.ashman.fivehundredpx;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class NeedLoginDialogFragment extends DialogFragment {
    private LoginPressedListener listener;

    public static NeedLoginDialogFragment newInstance(LoginPressedListener listener) {
        NeedLoginDialogFragment fragment = new NeedLoginDialogFragment();
        fragment.listener = listener;
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
        dialogBuilder.setMessage(R.string.login_need_login);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton(R.string.login_login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(intent);
                if(listener != null)
                    listener.onLoginPressed();
            }
        });
        dialogBuilder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return dialogBuilder.create();
    }

    public interface LoginPressedListener {
        public void onLoginPressed();
    }
}
