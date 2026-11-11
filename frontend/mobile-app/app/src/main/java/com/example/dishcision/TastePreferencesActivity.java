package com.example.dishcision;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.AuthHelper;
import com.example.dishcision.network.ProfileSyncHelper;
import com.example.dishcision.network.TokenManager;
import com.example.dishcision.network.UserPreferencesResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TastePreferencesActivity extends AppCompatActivity {

    private static final String TASTE_SPICY = "🌶️ Chua cay";
    private static final String TASTE_RICH = "🍲 Đậm đà";
    private static final String TASTE_SOUP = "🍜 Món nước";
    private static final String TASTE_SOURDOUGH = "🥖 Bánh mì sourdough";

    private ImageView btnBack;
    private TextView tvTasteCountSubtext;
    private TextView chipSpicy;
    private TextView chipRich;
    private TextView chipSoup;
    private TextView chipSourdough;
    private LinearLayout layoutCustomTastes;
    private TextView btnAddTastePreference;

    private CheckBox chkPeanuts;
    private CheckBox chkShellfish;
    private CheckBox chkMilk;
    private CheckBox chkEggs;
    private CheckBox chkGluten;
    private CheckBox chkSoy;
    private LinearLayout btnSaveTasteSettings;

    private TokenManager tokenManager;
    private boolean isSpicySelected = false;
    private boolean isRichSelected = false;
    private boolean isSoupSelected = false;
    private boolean isSourdoughSelected = false;
    private final Set<String> customSelectedTastes = new LinkedHashSet<>();
    private Set<String> baselineTastes = new LinkedHashSet<>();
    private Set<String> baselineAllergies = new HashSet<>();
    private boolean isTastesEdited = false;
    private boolean isAllergiesEdited = false;
    private boolean isPopulatingAllergies = false;
    private boolean skipFirstResumeFetch = true;
    private boolean isSaving = false;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (skipFirstResumeFetch) {
            skipFirstResumeFetch = false;
            return;
        }
        if (!isSaving) {
            fetchPreferencesApi();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTasteCountSubtext = findViewById(R.id.tvTasteCountSubtext);
        chipSpicy = findViewById(R.id.chipSpicy);
        chipRich = findViewById(R.id.chipRich);
        chipSoup = findViewById(R.id.chipSoup);
        chipSourdough = findViewById(R.id.chipSourdough);
        layoutCustomTastes = findViewById(R.id.layoutCustomTastes);
        btnAddTastePreference = findViewById(R.id.btnAddTastePreference);

        chkPeanuts = findViewById(R.id.chkPeanuts);
        chkShellfish = findViewById(R.id.chkShellfish);
        chkMilk = findViewById(R.id.chkMilk);
        chkEggs = findViewById(R.id.chkEggs);
        chkGluten = findViewById(R.id.chkGluten);
        chkSoy = findViewById(R.id.chkSoy);
        btnSaveTasteSettings = findViewById(R.id.btnSaveTasteSettings);
    }

    private String getAllergyPrefsKey() {
        int uid = AuthHelper.getCurrentUserId(this);
        return uid > 0 ? "user_allergies_" + uid : "user_allergies_guest";
    }

    private void loadSavedPreferences() {
        SharedPreferences prefs = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE);
        Set<String> savedTastes = prefs.getStringSet("user_taste_preferences", null);
        applyTasteSet(savedTastes);
        baselineTastes = collectSelectedTastes();

        Set<String> savedAllergies = prefs.getStringSet(getAllergyPrefsKey(), null);
        applyAllergySet(savedAllergies);
        baselineAllergies = collectSelectedAllergies();
    }

    private void applyAllergySet(java.util.Collection<String> allergies) {
        Set<String> normalized = ProfileSyncHelper.normalizeLowerStringSet(allergies);
        isPopulatingAllergies = true;
        try {
            if (chkPeanuts != null) chkPeanuts.setChecked(normalized.contains("peanuts"));
            if (chkShellfish != null) chkShellfish.setChecked(normalized.contains("shellfish"));
            if (chkMilk != null) chkMilk.setChecked(normalized.contains("milk"));
            if (chkEggs != null) chkEggs.setChecked(normalized.contains("eggs"));
            if (chkGluten != null) chkGluten.setChecked(normalized.contains("gluten"));
            if (chkSoy != null) chkSoy.setChecked(normalized.contains("soy"));
        } finally {
            isPopulatingAllergies = false;
        }
    }

    private Set<String> canonicalizeTasteSet(java.util.Collection<String> tastes) {
        Set<String> out = new LinkedHashSet<>();
        boolean spicy = false;
        boolean rich = false;
        boolean soup = false;
        boolean sourdough = false;
        Set<String> custom = new LinkedHashSet<>();
        if (tastes != null) {
            for (String item : tastes) {
                if (item == null) continue;
                String trimmed = item.trim();
                if (trimmed.isEmpty()) continue;
                if (TASTE_SPICY.equals(trimmed) || "Chua cay".equalsIgnoreCase(trimmed)) {
                    spicy = true;
                } else if (TASTE_RICH.equals(trimmed) || "Đậm đà".equalsIgnoreCase(trimmed)) {
                    rich = true;
                } else if (TASTE_SOUP.equals(trimmed) || "Món nước".equalsIgnoreCase(trimmed)) {
                    soup = true;
                } else if (TASTE_SOURDOUGH.equals(trimmed) || "Bánh mì sourdough".equalsIgnoreCase(trimmed)) {
                    sourdough = true;
                } else {
                    custom.add(trimmed);
                }
            }
        }
        if (spicy) out.add(TASTE_SPICY);
        if (rich) out.add(TASTE_RICH);
        if (soup) out.add(TASTE_SOUP);
        if (sourdough) out.add(TASTE_SOURDOUGH);
        out.addAll(custom);
        return out;
    }

    private Set<String> canonicalizeKnownAllergies(java.util.Collection<String> allergies) {
        Set<String> normalized = ProfileSyncHelper.normalizeLowerStringSet(allergies);
        Set<String> known = new HashSet<>();
        if (normalized.contains("peanuts")) known.add("peanuts");
        if (normalized.contains("shellfish")) known.add("shellfish");
        if (normalized.contains("milk")) known.add("milk");
        if (normalized.contains("eggs")) known.add("eggs");
        if (normalized.contains("gluten")) known.add("gluten");
        if (normalized.contains("soy")) known.add("soy");
        return known;
    }

    private void applyTasteSet(Set<String> tastes) {
        isSpicySelected = false;
        isRichSelected = false;
        isSoupSelected = false;
        isSourdoughSelected = false;
        customSelectedTastes.clear();

        if (tastes != null) {
            for (String item : tastes) {
                if (item == null) continue;
                String trimmed = item.trim();
                if (trimmed.isEmpty()) continue;
                if (TASTE_SPICY.equals(trimmed) || "Chua cay".equalsIgnoreCase(trimmed)) {
                    isSpicySelected = true;
                } else if (TASTE_RICH.equals(trimmed) || "Đậm đà".equalsIgnoreCase(trimmed)) {
                    isRichSelected = true;
                } else if (TASTE_SOUP.equals(trimmed) || "Món nước".equalsIgnoreCase(trimmed)) {
                    isSoupSelected = true;
                } else if (TASTE_SOURDOUGH.equals(trimmed) || "Bánh mì sourdough".equalsIgnoreCase(trimmed)) {
                    isSourdoughSelected = true;
                } else {
                    customSelectedTastes.add(trimmed);
                }
            }
        }

        toggleChipStyle(chipSpicy, isSpicySelected, "Chua cay");
        toggleChipStyle(chipRich, isRichSelected, "Đậm đà");
        toggleChipStyle(chipSoup, isSoupSelected, "Món nước");
        toggleChipStyle(chipSourdough, isSourdoughSelected, "Bánh mì sourdough");
        renderCustomTastes();
        updateTasteCount();
    }

    private void renderCustomTastes() {
        if (layoutCustomTastes == null) return;
        layoutCustomTastes.removeAllViews();
        int gap = (int) (8 * getResources().getDisplayMetrics().density);
        int padH = (int) (14 * getResources().getDisplayMetrics().density);
        int padV = (int) (6 * getResources().getDisplayMetrics().density);

        for (String taste : new ArrayList<>(customSelectedTastes)) {
            TextView chip = new TextView(this);
            chip.setBackgroundResource(R.drawable.bg_chip_selected_orange);
            chip.setTextColor(0xFFFF5A36);
            chip.setTextSize(13);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setPadding(padH, padV, padH, padV);
            chip.setText("✔ " + taste + " ✕");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(gap);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                if (customSelectedTastes.remove(taste)) {
                    isTastesEdited = true;
                    renderCustomTastes();
                    updateTasteCount();
                }
            });
            layoutCustomTastes.addView(chip);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (chipSpicy != null) {
            chipSpicy.setOnClickListener(v -> {
                isSpicySelected = !isSpicySelected;
                isTastesEdited = true;
                toggleChipStyle(chipSpicy, isSpicySelected, "Chua cay");
                updateTasteCount();
            });
        }

        if (chipRich != null) {
            chipRich.setOnClickListener(v -> {
                isRichSelected = !isRichSelected;
                isTastesEdited = true;
                toggleChipStyle(chipRich, isRichSelected, "Đậm đà");
                updateTasteCount();
            });
        }

        if (chipSoup != null) {
            chipSoup.setOnClickListener(v -> {
                isSoupSelected = !isSoupSelected;
                isTastesEdited = true;
                toggleChipStyle(chipSoup, isSoupSelected, "Món nước");
                updateTasteCount();
            });
        }

        if (chipSourdough != null) {
            chipSourdough.setOnClickListener(v -> {
                isSourdoughSelected = !isSourdoughSelected;
                isTastesEdited = true;
                toggleChipStyle(chipSourdough, isSourdoughSelected, "Bánh mì sourdough");
                updateTasteCount();
            });
        }

        CompoundButton.OnCheckedChangeListener allergyListener = (buttonView, isChecked) -> {
            if (!isPopulatingAllergies) {
                isAllergiesEdited = true;
            }
        };
        if (chkPeanuts != null) chkPeanuts.setOnCheckedChangeListener(allergyListener);
        if (chkShellfish != null) chkShellfish.setOnCheckedChangeListener(allergyListener);
        if (chkMilk != null) chkMilk.setOnCheckedChangeListener(allergyListener);
        if (chkEggs != null) chkEggs.setOnCheckedChangeListener(allergyListener);
        if (chkGluten != null) chkGluten.setOnCheckedChangeListener(allergyListener);
        if (chkSoy != null) chkSoy.setOnCheckedChangeListener(allergyListener);

        if (btnAddTastePreference != null) {
            btnAddTastePreference.setOnClickListener(v -> showAddNewTasteDialog());
        }

        if (btnSaveTasteSettings != null) {
            btnSaveTasteSettings.setOnClickListener(v -> {
                Set<String> newAllergies = collectSelectedAllergies();
                Set<String> newTastes = collectSelectedTastes();
                savePreferencesToApi(newTastes, newAllergies);
            });
        }
    }

    private Set<String> collectSelectedTastes() {
        Set<String> set = new LinkedHashSet<>();
        if (isSpicySelected) set.add(TASTE_SPICY);
        if (isRichSelected) set.add(TASTE_RICH);
        if (isSoupSelected) set.add(TASTE_SOUP);
        if (isSourdoughSelected) set.add(TASTE_SOURDOUGH);
        set.addAll(customSelectedTastes);
        return set;
    }

    private Set<String> collectSelectedAllergies() {
        Set<String> set = new HashSet<>();
        if (chkPeanuts != null && chkPeanuts.isChecked()) set.add("peanuts");
        if (chkShellfish != null && chkShellfish.isChecked()) set.add("shellfish");
        if (chkMilk != null && chkMilk.isChecked()) set.add("milk");
        if (chkEggs != null && chkEggs.isChecked()) set.add("eggs");
        if (chkGluten != null && chkGluten.isChecked()) set.add("gluten");
        if (chkSoy != null && chkSoy.isChecked()) set.add("soy");
        return set;
    }

    private void showAddNewTasteDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Nhập khẩu vị (VD: Ăn chay, Ít ngọt...)");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Thêm khẩu vị món ăn")
                .setView(input)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String newTaste = input.getText().toString().trim();
                    if (!newTaste.isEmpty()) {
                        String entry = newTaste.startsWith("✨") ? newTaste : "✨ " + newTaste;
                        if (customSelectedTastes.add(entry)) {
                            isTastesEdited = true;
                            renderCustomTastes();
                            updateTasteCount();
                        }
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
        int count = customSelectedTastes.size();
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
        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) return;

        ApiClient.getApiService().getPreferences(authHeader).enqueue(new Callback<UserPreferencesResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserPreferencesResponse> call, @NonNull Response<UserPreferencesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    SharedPreferences.Editor editor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
                    if (response.body().data != null) {
                        Set<String> serverTastes = new LinkedHashSet<>();
                        for (UserPreferencesResponse.PreferenceItem item : response.body().data) {
                            if (item != null && item.name != null && !item.name.trim().isEmpty()) {
                                serverTastes.add(item.name.trim());
                            }
                        }
                        Set<String> canonicalTastes = canonicalizeTasteSet(serverTastes);
                        baselineTastes = canonicalTastes;
                        if (!isTastesEdited) {
                            editor.putStringSet("user_taste_preferences", new HashSet<>(canonicalTastes));
                            applyTasteSet(canonicalTastes);
                        }
                    }
                    if (response.body().allergies != null) {
                        Set<String> serverAllergies = canonicalizeKnownAllergies(response.body().allergies);
                        baselineAllergies = serverAllergies;
                        if (!isAllergiesEdited) {
                            editor.putStringSet(getAllergyPrefsKey(), new HashSet<>(serverAllergies));
                            applyAllergySet(serverAllergies);
                        }
                    }
                    editor.apply();
                } else if (response.code() == 401) {
                    AuthHelper.handleUnauthorized(TastePreferencesActivity.this, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserPreferencesResponse> call, @NonNull Throwable t) {}
        });
    }

    private void savePreferencesToApi(Set<String> newTastes, Set<String> newAllergies) {
        if (isSaving) return;

        String authHeader = ApiClient.getAuthHeader(this);
        if (authHeader.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để lưu tùy chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> prefData = ProfileSyncHelper.buildPreferencesDiffPayload(
                baselineTastes, newTastes, baselineAllergies, newAllergies
        );
        if (prefData.isEmpty()) {
            Toast.makeText(this, "Không có thay đổi mới cần lưu.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        boolean savedTastes = prefData.containsKey("preferences");
        boolean savedAllergies = prefData.containsKey("allergies");
        int activeTasteCount = newTastes.size();
        int allergyCount = newAllergies.size();

        isSaving = true;
        if (btnSaveTasteSettings != null) btnSaveTasteSettings.setEnabled(false);

        ApiClient.getApiService().updatePreferences(authHeader, prefData).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                isSaving = false;
                if (btnSaveTasteSettings != null) btnSaveTasteSettings.setEnabled(true);
                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = getSharedPreferences("DishcisionPrefs", MODE_PRIVATE).edit();
                    if (savedTastes) {
                        baselineTastes = new LinkedHashSet<>(newTastes);
                        isTastesEdited = false;
                        editor.putStringSet("user_taste_preferences", new HashSet<>(newTastes));
                    }
                    if (savedAllergies) {
                        baselineAllergies = new HashSet<>(newAllergies);
                        isAllergiesEdited = false;
                        editor.putStringSet(getAllergyPrefsKey(), new HashSet<>(newAllergies));
                    }
                    editor.apply();

                    String msg;
                    if (savedTastes && savedAllergies) {
                        msg = "Đã lưu " + activeTasteCount + " khẩu vị & " + allergyCount + " dị ứng!";
                    } else if (savedTastes) {
                        msg = "Đã lưu " + activeTasteCount + " khẩu vị!";
                    } else {
                        msg = "Đã lưu " + allergyCount + " mục dị ứng!";
                    }
                    Toast.makeText(TastePreferencesActivity.this, msg, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 401) {
                    AuthHelper.handleUnauthorized(TastePreferencesActivity.this, null);
                } else {
                    Toast.makeText(TastePreferencesActivity.this,
                            "Không thể lưu tùy chọn (Mã " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                isSaving = false;
                if (btnSaveTasteSettings != null) btnSaveTasteSettings.setEnabled(true);
                Toast.makeText(TastePreferencesActivity.this,
                        "Lỗi kết nối máy chủ, chưa thể lưu tùy chọn",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
