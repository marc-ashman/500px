package com.ashman.fivehundredpx;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class ShareDialogFragment extends DialogFragment {
    private String url;

    public static ShareDialogFragment newInstance(String url) {
        ShareDialogFragment fragment = new ShareDialogFragment();
        fragment.url = url;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_share, container, false);
        Button shareFacebook = (Button) root.findViewById(R.id.share_fb);
        Button shareEmail = (Button) root.findViewById(R.id.share_email);
        shareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFacebookShare(url);
            }
        });
        shareEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doEmailShare(url);
            }
        });
        return root;
    }



    private void doFacebookShare(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage("com.facebook.katana");

            intent.putExtra(Intent.EXTRA_TEXT, url);
            startActivityForResult(intent, 9001);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.share_noFacebook),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void doEmailShare(String url) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_TEXT, url);

        startActivityForResult(Intent.createChooser(emailIntent, "Email:"), 9003);
    }
}
