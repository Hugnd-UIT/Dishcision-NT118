package com.example.dishcision;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TastePreferencesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnAddTastePreference;
    private CheckBox chkPeanuts;
    private CheckBox chkShellfish;
    private CheckBox chkMilk;
    private CheckBox chkEggs;
    private CheckBox chkGluten;
    private CheckBox chkSoy;
    private LinearLayout btnSaveTasteSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_taste_preferences);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddTastePreference = findViewById(R.id.btnAddTastePreference);
        chkPeanuts = findViewById(R.id.chkPeanuts);
        chkShellfish = findViewById(R.id.chkShellfish);
        chkMilk = findViewById(R.id.chkMilk);
        chkEggs = findViewById(R.id.chkEggs);
        chkGluten = findViewById(R.id.chkGluten);
        chkSoy = findViewById(R.id.chkSoy);
        btnSaveTasteSettings = findViewById(R.id.btnSaveTasteSettings);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddTastePreference != null) {
            btnAddTastePreference.setOnClickListener(v -> {
                Intent intent = new Intent(TastePreferencesActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (btnSaveTasteSettings != null) {
            btnSaveTasteSettings.setOnClickListener(v -> {
                int allergyCount = 0;
                if (chkPeanuts != null && chkPeanuts.isChecked()) allergyCount++;
                if (chkShellfish != null && chkShellfish.isChecked()) allergyCount++;
                if (chkMilk != null && chkMilk.isChecked()) allergyCount++;
                if (chkEggs != null && chkEggs.isChecked()) allergyCount++;
                if (chkGluten != null && chkGluten.isChecked()) allergyCount++;
                if (chkSoy != null && chkSoy.isChecked()) allergyCount++;

                Toast.makeText(this, "Đã lưu khẩu vị & " + allergyCount + " dị ứng!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
