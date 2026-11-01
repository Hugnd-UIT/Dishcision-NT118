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

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.TokenManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TastePreferencesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTasteCountSubtext;
    private TextView chipSpicy;
    private TextView chipRich;
    private TextView chipSoup;
    private TextView chipSourdough;
    private TextView btnAddTastePreference;

    private CheckBox chkPeanuts;
    private CheckBox chkShellfish;
    private CheckBox chkMilk;
    private CheckBox chkEggs;
    private CheckBox chkGluten;
    private CheckBox chkSoy;
    private LinearLayout btnSaveTasteSettings;

    private TokenManager tokenManager;
    private boolean isSpicySelected = true;
    private boolean isRichSelected = true;
    private boolean isSoupSelected = true;
    private boolean isSourdoughSelected = true;

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

        tokenManager = new TokenManager(this);

        initViews();
        setupListeners();
        fetchPreferencesApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTasteCountSubtext = findViewById(R.id.tvTasteCountSubtext);
        chipSpicy = findViewById(R.id.chipSpicy);
        chipRich = findViewById(R.id.chipRich);
        chipSoup = findViewById(R.id.chipSoup);
        chipSourdough = findViewById(R.id.chipSourdough);
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

        if (chipSpicy != null) {
            chipSpicy.setOnClickListener(v -> {
                isSpicySelected = !isSpicySelected;
                toggleChipStyle(chipSpicy, isSpicySelected, "Chua cay");
                updateTasteCount();
            });
        }

        if (chipRich != null) {
            chipRich.setOnClickListener(v -> {
                isRichSelected = !isRichSelected;
                toggleChipStyle(chipRich, isRichSelected, "Đậm đà");
                updateTasteCount();
            });
        }

        if (chipSoup != null) {
            chipSoup.setOnClickListener(v -> {
                isSoupSelected = !isSoupSelected;
                toggleChipStyle(chipSoup, isSoupSelected, "Món nước");
                updateTasteCount();
            });
        }

        if (chipSourdough != null) {
            chipSourdough.setOnClickListener(v -> {
                isSourdoughSelected = !isSourdoughSelected;
                toggleChipStyle(chipSourdough, isSourdoughSelected, "Bánh mì sourdough");
                updateTasteCount();
            });
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

                int activeTasteCount = getActiveTasteCount();
                savePreferencesToApi(activeTasteCount, allergyCount);
            });
        }
    }

    private void toggleChipStyle(TextView chipView, boolean isSelected, String label) {
        if (chipView == null) return;
        if (isSelected) {
            chipView.setBackgroundResource(R.drawable.bg_chip_selected_orange);
            chipView.setTextColor(0xFFFF5A36);
            chipView.setText("✔ " + label);
        } else {
            chipView.setBackgroundResource(R.drawable.bg_chip_preference);
            chipView.setTextColor(0xFF141B2B);
            chipView.setText("+ " + label);
        }
    }

    private int getActiveTasteCount() {
        int count = 0;
        if (isSpicySelected) count++;
        if (isRichSelected) count++;
        if (isSoupSelected) count++;
        if (isSourdoughSelected) count++;
        return count;
    }

    private void updateTasteCount() {
        if (tvTasteCountSubtext != null) {
            tvTasteCountSubtext.setText(getActiveTasteCount() + " khẩu vị từ hồ sơ của bạn");
        }
    }

    private void fetchPreferencesApi() {
        ApiClient.getApiService().getPreferences(tokenManager.getFormattedToken()).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {}

            @Override
            public void onFailure(Call<Object> call, Throwable t) {}
        });
    }

    private void savePreferencesToApi(int activeTasteCount, int allergyCount) {
        Map<String, Object> prefData = new HashMap<>();
        prefData.put("activeTastes", activeTasteCount);
        prefData.put("allergyCount", allergyCount);

        ApiClient.getApiService().updatePreferences(tokenManager.getFormattedToken(), prefData).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Toast.makeText(TastePreferencesActivity.this, "Đã lưu " + activeTasteCount + " khẩu vị & " + allergyCount + " dị ứng lên Server!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(TastePreferencesActivity.this, "Đã lưu " + activeTasteCount + " khẩu vị & " + allergyCount + " dị ứng!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
