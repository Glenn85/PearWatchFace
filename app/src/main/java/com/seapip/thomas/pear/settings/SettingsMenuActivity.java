package com.seapip.thomas.pear.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.seapip.thomas.pear.R;
import com.seapip.thomas.pear.sport_digital.SettingsActivity;

public class SettingsMenuActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsMenuActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }
}
