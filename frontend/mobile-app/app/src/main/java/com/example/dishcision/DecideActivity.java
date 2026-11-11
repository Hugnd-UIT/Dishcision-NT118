package com.example.dishcision;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.DishModel;
import com.example.dishcision.network.DishesResponse;
import com.example.dishcision.network.FoodDecideRequest;
import com.example.dishcision.network.FoodDecideResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DecideActivity extends AppCompatActivity {

    private ImageView btnNotification;

    private WheelView wheelView;
    private RelativeLayout btnSpinCenter;

    private TextView btnDecreaseItem;
    private TextView btnIncreaseItem;
    private TextView tvItemCountDisplay;
    private TextView tvDishCountBadge;

    private View btnAiSuggest;
    private TextView chip3Items;
    private TextView chip5Items;
    private TextView chip8Items;
    private TextView chip10Items;
    private TextView chip12Items;
    private TextView chip15Items;

    private EditText edtDishList;
    private TextView btnClearDishList;
    private RelativeLayout btnUpdateWheel;
    private TextView tvBtnUpdateWheel;

    private final List<String> backendDishPool = new ArrayList<>();
    private final List<String> currentDishes = new ArrayList<>();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_decide);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_decide), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ApiClient.init(this);
        initViews();
        setupListeners();
        updateUI();
        fetchDishesFromBackend();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);

        wheelView = findViewById(R.id.wheelView);
        btnSpinCenter = findViewById(R.id.btnSpinCenter);

        btnDecreaseItem = findViewById(R.id.btnDecreaseItem);
        btnIncreaseItem = findViewById(R.id.btnIncreaseItem);
        tvItemCountDisplay = findViewById(R.id.tvItemCountDisplay);
        tvDishCountBadge = findViewById(R.id.tvDishCountBadge);

        btnAiSuggest = findViewById(R.id.btnAiSuggest);
        chip3Items = findViewById(R.id.chip3Items);
        chip5Items = findViewById(R.id.chip5Items);
        chip8Items = findViewById(R.id.chip8Items);
        chip10Items = findViewById(R.id.chip10Items);
        chip12Items = findViewById(R.id.chip12Items);
        chip15Items = findViewById(R.id.chip15Items);

        edtDishList = findViewById(R.id.edtDishList);
        btnClearDishList = findViewById(R.id.btnClearDishList);
        btnUpdateWheel = findViewById(R.id.btnUpdateWheel);
        tvBtnUpdateWheel = findViewById(R.id.tvBtnUpdateWheel);
    }

    private void fetchDishesFromBackend() {
        ApiClient.getApiService().getDishes(null, null, null, 20).enqueue(new Callback<DishesResponse>() {
            @Override
            public void onResponse(@NonNull Call<DishesResponse> call, @NonNull Response<DishesResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    backendDishPool.clear();
                    for (DishModel dish : response.body().data) {
                        if (dish != null && dish.name != null && !dish.name.trim().isEmpty()) {
                            String clean = dish.name.trim();
                            if (!backendDishPool.contains(clean)) {
                                backendDishPool.add(clean);
                            }
                        }
                    }
                    if (currentDishes.isEmpty() && !backendDishPool.isEmpty()) {
                        int initCount = Math.min(5, backendDishPool.size());
                        for (int i = 0; i < initCount; i++) {
                            currentDishes.add(backendDishPool.get(i));
                        }
                        updateUI();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DishesResponse> call, @NonNull Throwable t) {}
        });
    }

    private void updateUI() {
        int count = currentDishes.size();

        if (edtDishList != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentDishes.size(); i++) {
                sb.append(currentDishes.get(i));
                if (i < currentDishes.size() - 1) sb.append("\n");
            }
            edtDishList.setText(sb.toString());
            edtDishList.setSelection(edtDishList.getText().length());
        }

        if (tvItemCountDisplay != null) {
            tvItemCountDisplay.setText(count + " món");
        }
        if (tvDishCountBadge != null) {
            tvDishCountBadge.setText(count + " món đã nhập");
        }

        if (wheelView != null) {
            wheelView.setItems(currentDishes);
            wheelView.setRotation(0f);
        }

        updateChipStyles(count);
    }

    private void updateChipStyles(int activeCount) {
        setChipStyle(chip3Items, activeCount == 3);
        setChipStyle(chip5Items, activeCount == 5);
        setChipStyle(chip8Items, activeCount == 8);
        setChipStyle(chip10Items, activeCount == 10);
        setChipStyle(chip12Items, activeCount == 12);
        setChipStyle(chip15Items, activeCount == 15);
    }

    private void setChipStyle(TextView chip, boolean isActive) {
        if (chip == null) return;
        if (isActive) {
            chip.setBackgroundResource(R.drawable.bg_chip_active);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            chip.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void setDishPresetCount(int count) {
        if (count > 15) {
            Toast.makeText(this, "Bạn đã đạt giới hạn vòng quay", Toast.LENGTH_SHORT).show();
            return;
        }
        if (backendDishPool.isEmpty()) {
            Toast.makeText(this, "Chưa tải được món từ hệ thống, bạn có thể nhập trực tiếp hoặc dùng AI gợi ý", Toast.LENGTH_SHORT).show();
            return;
        }
        currentDishes.clear();
        for (int i = 0; i < count && i < backendDishPool.size(); i++) {
            currentDishes.add(backendDishPool.get(i));
        }
        updateUI();
        Toast.makeText(this, "Đã cập nhật vòng quay với " + currentDishes.size() + " món ăn!", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        if (btnAiSuggest != null) {
            btnAiSuggest.setOnClickListener(v -> showAiSuggestDialog());
        }

        if (chip3Items != null) chip3Items.setOnClickListener(v -> setDishPresetCount(3));
        if (chip5Items != null) chip5Items.setOnClickListener(v -> setDishPresetCount(5));
        if (chip8Items != null) chip8Items.setOnClickListener(v -> setDishPresetCount(8));
        if (chip10Items != null) chip10Items.setOnClickListener(v -> setDishPresetCount(10));
        if (chip12Items != null) chip12Items.setOnClickListener(v -> setDishPresetCount(12));
        if (chip15Items != null) chip15Items.setOnClickListener(v -> setDishPresetCount(15));

        if (btnDecreaseItem != null) {
            btnDecreaseItem.setOnClickListener(v -> {
                if (currentDishes.size() > 2) {
                    currentDishes.remove(currentDishes.size() - 1);
                    updateUI();
                    Toast.makeText(this, "Hiện có " + currentDishes.size() + " món trên vòng quay", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Tối thiểu 2 món trên vòng quay!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnIncreaseItem != null) {
            btnIncreaseItem.setOnClickListener(v -> {
                if (currentDishes.size() >= 15) {
                    Toast.makeText(this, "Bạn đã đạt giới hạn vòng quay", Toast.LENGTH_SHORT).show();
                    return;
                }
                String added = null;
                for (String candidate : backendDishPool) {
                    if (!currentDishes.contains(candidate)) {
                        added = candidate;
                        break;
                    }
                }
                if (added == null) {
                    Toast.makeText(this, "Đã thêm hết món từ hệ thống, bạn có thể nhập thêm ở danh sách bên dưới", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentDishes.add(added);
                updateUI();
                Toast.makeText(this, "Hiện có " + currentDishes.size() + " món trên vòng quay", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnClearDishList != null) {
            btnClearDishList.setOnClickListener(v -> {
                currentDishes.clear();
                updateUI();
                if (edtDishList != null) {
                    edtDishList.requestFocus();
                }
                Toast.makeText(this, "Đã xóa tất cả danh sách món ăn!", Toast.LENGTH_SHORT).show();
            });
        }

        View.OnClickListener updateListener = v -> applyTypedDishes();
        if (btnUpdateWheel != null) btnUpdateWheel.setOnClickListener(updateListener);
        if (tvBtnUpdateWheel != null) tvBtnUpdateWheel.setOnClickListener(updateListener);

        if (btnSpinCenter != null) {
            btnSpinCenter.setOnClickListener(v -> performSpinWheel());
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }
        BottomNavHelper.bind(this, BottomNavHelper.TAB_DECIDE);
    }

    private void showAiSuggestDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_ai_suggest, null);
        dialog.setContentView(dialogView);

        ImageView btnCloseAiDialog = dialogView.findViewById(R.id.btnCloseAiDialog);
        EditText edtAiCraving = dialogView.findViewById(R.id.edtAiCraving);
        RelativeLayout btnCallGemini = dialogView.findViewById(R.id.btnCallGemini);
        TextView tvBtnCallGemini = dialogView.findViewById(R.id.tvBtnCallGemini);
        ProgressBar pbAiLoading = dialogView.findViewById(R.id.pbAiLoading);
        LinearLayout containerAiResults = dialogView.findViewById(R.id.containerAiResults);
        TextView tvAiDish1 = dialogView.findViewById(R.id.tvAiDish1);
        TextView tvAiReason1 = dialogView.findViewById(R.id.tvAiReason1);
        TextView tvAiDish2 = dialogView.findViewById(R.id.tvAiDish2);
        TextView tvAiReason2 = dialogView.findViewById(R.id.tvAiReason2);
        TextView tvAiDish3 = dialogView.findViewById(R.id.tvAiDish3);
        TextView tvAiReason3 = dialogView.findViewById(R.id.tvAiReason3);
        RelativeLayout btnApplyAiToWheel = dialogView.findViewById(R.id.btnApplyAiToWheel);

        if (btnCloseAiDialog != null) btnCloseAiDialog.setOnClickListener(v -> dialog.dismiss());

        final List<String> aiSuggestedDishes = new ArrayList<>();
        TextView[] dishViews = {tvAiDish1, tvAiDish2, tvAiDish3};
        TextView[] reasonViews = {tvAiReason1, tvAiReason2, tvAiReason3};

        if (btnCallGemini != null) {
            btnCallGemini.setOnClickListener(v -> {
                String promptText = edtAiCraving != null ? edtAiCraving.getText().toString().trim() : "";
                if (promptText.isEmpty()) {
                    promptText = "Gợi ý món ăn ngon hôm nay";
                }

                if (pbAiLoading != null) pbAiLoading.setVisibility(View.VISIBLE);
                if (tvBtnCallGemini != null) tvBtnCallGemini.setText("Đang suy nghĩ...");
                btnCallGemini.setEnabled(false);

                FoodDecideRequest request = new FoodDecideRequest(promptText, "Bình dân", 1, promptText);
                ApiClient.getApiService().foodDecide(request).enqueue(new Callback<FoodDecideResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FoodDecideResponse> call, @NonNull Response<FoodDecideResponse> response) {
                        btnCallGemini.setEnabled(true);
                        if (pbAiLoading != null) pbAiLoading.setVisibility(View.GONE);
                        if (tvBtnCallGemini != null) tvBtnCallGemini.setText("Nhận món ăn");

                        if (isFinishing() || isDestroyed()) return;

                        aiSuggestedDishes.clear();
                        for (int i = 0; i < 3; i++) {
                            if (dishViews[i] != null) dishViews[i].setVisibility(View.GONE);
                            if (reasonViews[i] != null) reasonViews[i].setVisibility(View.GONE);
                        }

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<FoodDecideResponse.Suggestion> list = response.body().getData().suggestions;
                            if (list != null) {
                                int shown = 0;
                                for (FoodDecideResponse.Suggestion s : list) {
                                    if (s != null && s.dishName != null && !s.dishName.trim().isEmpty()) {
                                        String cleanName = s.dishName.trim();
                                        aiSuggestedDishes.add(cleanName);
                                        if (shown < 3) {
                                            if (dishViews[shown] != null) {
                                                dishViews[shown].setText((shown + 1) + ". " + cleanName);
                                                dishViews[shown].setVisibility(View.VISIBLE);
                                            }
                                            if (reasonViews[shown] != null) {
                                                String reason = s.reason != null ? s.reason.trim() : "";
                                                if (!reason.isEmpty()) {
                                                    reasonViews[shown].setText(reason);
                                                    reasonViews[shown].setVisibility(View.VISIBLE);
                                                }
                                            }
                                            shown++;
                                        }
                                    }
                                }
                            }
                        }

                        if (aiSuggestedDishes.isEmpty()) {
                            if (containerAiResults != null) containerAiResults.setVisibility(View.GONE);
                            Toast.makeText(DecideActivity.this, "AI chưa trả về gợi ý phù hợp. Vui lòng thử mô tả khác!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (containerAiResults != null) containerAiResults.setVisibility(View.VISIBLE);
                            Toast.makeText(DecideActivity.this, "Đã gợi ý " + aiSuggestedDishes.size() + " món ngon!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FoodDecideResponse> call, @NonNull Throwable t) {
                        btnCallGemini.setEnabled(true);
                        if (pbAiLoading != null) pbAiLoading.setVisibility(View.GONE);
                        if (tvBtnCallGemini != null) tvBtnCallGemini.setText("Nhận món ăn");
                        if (!isFinishing() && !isDestroyed()) {
                            if (containerAiResults != null) containerAiResults.setVisibility(View.GONE);
                            Toast.makeText(DecideActivity.this, "Không thể kết nối dịch vụ gợi ý AI.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        }

        if (btnApplyAiToWheel != null) {
            btnApplyAiToWheel.setOnClickListener(v -> {
                if (aiSuggestedDishes.size() >= 2) {
                    currentDishes.clear();
                    currentDishes.addAll(aiSuggestedDishes);
                    updateUI();
                    dialog.dismiss();
                    Toast.makeText(DecideActivity.this, "Đã đưa " + aiSuggestedDishes.size() + " món vào vòng quay!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DecideActivity.this, "Cần ít nhất 2 món được gợi ý để quay!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    private List<String> parseDishesFromText(String rawText) {
        List<String> list = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) return list;

        if (rawText.contains("\n")) {
            String[] lines = rawText.split("\\r?\\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) list.add(trimmed);
            }
        } else if (rawText.contains(",")) {
            String[] parts = rawText.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) list.add(trimmed);
            }
        } else {
            String trimmed = rawText.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    private void applyTypedDishes() {
        if (edtDishList == null) return;
        String rawText = edtDishList.getText().toString();
        List<String> validDishes = parseDishesFromText(rawText);

        if (validDishes.size() < 2) {
            Toast.makeText(this, "Vui lòng nhập ít nhất 2 món ăn trên vòng quay!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validDishes.size() > 15) {
            Toast.makeText(this, "Bạn đã đạt giới hạn vòng quay", Toast.LENGTH_SHORT).show();
            return;
        }

        currentDishes.clear();
        currentDishes.addAll(validDishes);

        int count = currentDishes.size();
        if (tvItemCountDisplay != null) tvItemCountDisplay.setText(count + " món");
        if (tvDishCountBadge != null) tvDishCountBadge.setText(count + " món đã nhập");

        if (wheelView != null) {
            wheelView.setItems(currentDishes);
            wheelView.setRotation(0f);
        }

        updateChipStyles(count);

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ignored) {}

        Toast.makeText(this, "Đã cập nhật vòng quay với " + count + " món ăn!", Toast.LENGTH_SHORT).show();
    }

    private void performSpinWheel() {
        if (edtDishList != null) {
            List<String> typedList = parseDishesFromText(edtDishList.getText().toString());
            if (typedList.size() >= 2 && !typedList.equals(currentDishes)) {
                if (typedList.size() > 15) {
                    Toast.makeText(this, "Bạn đã đạt giới hạn vòng quay", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentDishes.clear();
                currentDishes.addAll(typedList);
                if (wheelView != null) wheelView.setItems(currentDishes);
                int count = currentDishes.size();
                if (tvItemCountDisplay != null) tvItemCountDisplay.setText(count + " món");
                if (tvDishCountBadge != null) tvDishCountBadge.setText(count + " món đã nhập");
                updateChipStyles(count);
            }
        }

        if (currentDishes.size() < 2) {
            Toast.makeText(this, "Vui lòng có ít nhất 2 món trên vòng quay!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSpinCenter.setEnabled(false);

        int count = currentDishes.size();
        int winningIndex = random.nextInt(count);
        final String winningDish = currentDishes.get(winningIndex);

        float sweepAngle = 360f / count;
        float midAngle = winningIndex * sweepAngle + (sweepAngle / 2f);
        float targetOffset = (270f - midAngle + 360f) % 360f;
        float jitter = (random.nextFloat() * 2f - 1f) * (sweepAngle * 0.25f);
        float finalTargetAngleMod = (targetOffset + jitter + 360f) % 360f;

        float currentRot = wheelView != null ? wheelView.getRotation() : 0f;
        float currentMod = (currentRot % 360f + 360f) % 360f;
        float delta = finalTargetAngleMod - currentMod;
        if (delta < 0) delta += 360f;

        int fullSpins = 5 + random.nextInt(2);
        float targetRotation = currentRot + (fullSpins * 360f) + delta;

        if (wheelView != null) {
            wheelView.animate()
                    .rotation(targetRotation)
                    .setDuration(2600)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .withEndAction(() -> {
                        btnSpinCenter.setEnabled(true);
                        wheelView.postDelayed(() -> navigateToResult(winningDish), 450);
                    })
                    .start();
        } else {
            btnSpinCenter.setEnabled(true);
            navigateToResult(winningDish);
        }
    }

    private void navigateToResult(String dishName) {
        Intent intent = new Intent(DecideActivity.this, TodayRestaurantResultActivity.class);
        intent.putExtra(TodayRestaurantResultActivity.EXTRA_DISH_NAME, dishName);
        startActivity(intent);
    }
}
