package com.ashman.fivehundredpx;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ashman.fivehundredpx.enums.PhotoSize;

public class SettingsActivity extends BaseFragmentActivity {
    private PhotoSize selectedSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Button logoutButton = (Button) findViewById(R.id.settings_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FiveHundredApplication.get().saveAccessToken(null);
                Toast.makeText(SettingsActivity.this,
                        R.string.settings_loggedOut, Toast.LENGTH_SHORT).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.quality_high:
                if (checked)
                    selectedSize = PhotoSize.LARGE;
                    break;
            case R.id.quality_medium:
                if (checked)
                    selectedSize = PhotoSize.MEDIUM;
                    break;
            case R.id.quality_low:
                if (checked)
                    selectedSize = PhotoSize.SMALL;
                    break;
        }
    }

    public void onPause() {
        super.onPause();

        FiveHundredApplication.get().saveDefaultPhotoSize(selectedSize);
    }

    public void onStart() {
        super.onStart();

        selectedSize = FiveHundredApplication.get().getDefaultPhotoSize();
        int selectedRadioButton = 0;
        switch (selectedSize) {
            case LARGE:
                selectedRadioButton = R.id.quality_high;
                break;
            case MEDIUM:
                selectedRadioButton = R.id.quality_medium;
                break;
            case SMALL:
                selectedRadioButton = R.id.quality_low;
                break;
        }
        ((RadioButton)findViewById(selectedRadioButton)).setChecked(true);
    }
}
