package com.example.dishcision;

import android.content.SharedPreferences;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        loadSavedPreferences();
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

    private void loadSavedPreferences() {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        Set<String> savedTastes = prefs.getStringSet("user_taste_preferences", null);
        if (savedTastes != null) {
            isSpicySelected = savedTastes.contains("🌶️ Chua cay");
            isRichSelected = savedTastes.contains("🍲 Đậm đà");
            isSoupSelected = savedTastes.contains("🍜 Món nước");
            isSourdoughSelected = savedTastes.contains("🥖 Bánh mì sourdough");
        }

        toggleChipStyle(chipSpicy, isSpicySelected, "Chua cay");
        toggleChipStyle(chipRich, isRichSelected, "Đậm đà");
        toggleChipStyle(chipSoup, isSoupSelected, "Món nước");
        toggleChipStyle(chipSourdough, isSourdoughSelected, "Bánh mì sourdough");
        updateTasteCount();
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
            btnAddTastePreference.setOnClickListener(v -> showAddNewTasteDialog());
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

                // Save selected tastes into SharedPreferences for EditProfileActivity
                Set<String> newTastes = new HashSet<>();
                if (isSpicySelected) newTastes.add("🌶️ Chua cay");
                if (isRichSelected) newTastes.add("🍲 Đậm đà");
                if (isSoupSelected) newTastes.add("🍜 Món nước");
                if (isSourdoughSelected) newTastes.add("🥖 Bánh mì sourdough");

                getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit()
                        .putStringSet("user_taste_preferences", newTastes)
                        .apply();

                savePreferencesToApi(newTastes, activeTasteCount, allergyCount);
            });
        }
    }

    private void showAddNewTasteDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập khẩu vị (VD: Ăn chay, Ít ngọt...)");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thêm khẩu vị món ăn")
                .setView(input)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String newTaste = input.getText().toString().trim();
                    if (!newTaste.isEmpty()) {
                        android.content.SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
                        java.util.Set<String> savedSet = prefs.getStringSet("user_taste_preferences", null);
                        java.util.Set<String> updated = savedSet != null ? new java.util.HashSet<>(savedSet) : new java.util.HashSet<>();
                        updated.add("✨ " + newTaste);
                        prefs.edit().putStringSet("user_taste_preferences", updated).apply();
                        Toast.makeText(this, "Đã thêm khẩu vị: " + newTaste, Toast.LENGTH_SHORT).show();
                        loadSavedPreferences();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
        ApiClient.getApiService().getPreferences(ApiClient.getAuthHeader(this)).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {}

            @Override
            public void onFailure(Call<Object> call, Throwable t) {}
        });
    }

    private void savePreferencesToApi(Set<String> newTastes, int activeTasteCount, int allergyCount) {
        Map<String, Object> prefData = new HashMap<>();
        if (newTastes != null) {
            prefData.put("preferences", new java.util.ArrayList<>(newTastes));
        }
        prefData.put("activeTastes", activeTasteCount);
        prefData.put("allergyCount", allergyCount);

        ApiClient.getApiService().updatePreferences(ApiClient.getAuthHeader(this), prefData).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Toast.makeText(TastePreferencesActivity.this, "Đã lưu " + activeTasteCount + " khẩu vị & " + allergyCount + " dị ứng!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(TastePreferencesActivity.this, "Đã lưu " + activeTasteCount + " khẩu vị & " + allergyCount + " dị ứng!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
