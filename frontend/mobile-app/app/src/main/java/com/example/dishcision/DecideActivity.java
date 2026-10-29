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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.FoodDecideRequest;
import com.example.dishcision.network.FoodDecideResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DecideActivity extends AppCompatActivity {

    private static final String[] MASTER_DISH_POOL = {
            "Phở Bò Tái Lăn", "Cơm Tấm Sườn Bì", "Bún Bò Huế", "Bánh Mì Pate", "Pizza Hải Sản",
            "Bún Chả Hà Nội", "Lẩu Nấm Hải Sản", "Nem Rán Giòn Rụm", "Gà Nướng Mật Ong", "Bún Riêu Cua",
            "Mì Quảng Tôm Thịt", "Bánh Xèo Giòn Tan", "Hủ Tiếu Nam Vang", "Cháo Sườn Quẩy", "Bò Bít Tết", "Sushi Thập Cẩm"
    };

    // Header & Bottom nav
    private ImageView btnNotification;
    private LinearLayout navHome;
    private LinearLayout navCreate;
    private LinearLayout navMessages;
    private LinearLayout navProfile;

    // Wheel Views
    private WheelView wheelView;
    private RelativeLayout btnSpinCenter;

    // Controls
    private TextView btnDecreaseItem;
    private TextView btnIncreaseItem;
    private TextView tvItemCountDisplay;
    private TextView tvDishCountBadge;

    // Preset Chips & AI Button
    private View btnAiSuggest;
    private TextView chip3Items;
    private TextView chip5Items;
    private TextView chip8Items;
    private TextView chip10Items;
    private TextView chip12Items;

    // Edit Dish List Box
    private EditText edtDishList;
    private TextView btnClearDishList;
    private RelativeLayout btnUpdateWheel;
    private TextView tvBtnUpdateWheel;

    // Active dishes
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

        initViews();
        setupDefaultDishes();
        setupListeners();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        navHome = findViewById(R.id.navHome);
        navCreate = findViewById(R.id.navCreate);
        navMessages = findViewById(R.id.navMessages);
        navProfile = findViewById(R.id.navProfile);

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

        edtDishList = findViewById(R.id.edtDishList);
        btnClearDishList = findViewById(R.id.btnClearDishList);
        btnUpdateWheel = findViewById(R.id.btnUpdateWheel);
        tvBtnUpdateWheel = findViewById(R.id.tvBtnUpdateWheel);
    }

    private void setupDefaultDishes() {
        currentDishes.clear();
        currentDishes.addAll(Arrays.asList(
                "Phở Bò Tái Lăn",
                "Cơm Tấm Sườn Bì",
                "Bún Bò Huế",
                "Bánh Mì Pate",
                "Pizza Hải Sản"
        ));
        updateUI();
    }

    private void updateUI() {
        int count = currentDishes.size();

        // Update multiline edit text
        if (edtDishList != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentDishes.size(); i++) {
                sb.append(currentDishes.get(i));
                if (i < currentDishes.size() - 1) sb.append("\n");
            }
            edtDishList.setText(sb.toString());
            edtDishList.setSelection(edtDishList.getText().length());
        }

        // Update count displays
        if (tvItemCountDisplay != null) {
            tvItemCountDisplay.setText(count + " món");
        }
        if (tvDishCountBadge != null) {
            tvDishCountBadge.setText(count + " món đã nhập");
        }

        // Immediately update WheelView with all dishes
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
        currentDishes.clear();
        for (int i = 0; i < count && i < MASTER_DISH_POOL.length; i++) {
            currentDishes.add(MASTER_DISH_POOL[i]);
        }
        updateUI();
        Toast.makeText(this, "Đã cập nhật vòng quay với " + count + " món ăn!", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        // AI Suggest Button Click
        if (btnAiSuggest != null) {
            btnAiSuggest.setOnClickListener(v -> showAiSuggestDialog());
        }

        // Preset Chips Click
        if (chip3Items != null) chip3Items.setOnClickListener(v -> setDishPresetCount(3));
        if (chip5Items != null) chip5Items.setOnClickListener(v -> setDishPresetCount(5));
        if (chip8Items != null) chip8Items.setOnClickListener(v -> setDishPresetCount(8));
        if (chip10Items != null) chip10Items.setOnClickListener(v -> setDishPresetCount(10));
        if (chip12Items != null) chip12Items.setOnClickListener(v -> setDishPresetCount(12));

        // Minus (-) Button
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

        // Plus (+) Button
        if (btnIncreaseItem != null) {
            btnIncreaseItem.setOnClickListener(v -> {
                if (currentDishes.size() < MASTER_DISH_POOL.length) {
                    for (String candidate : MASTER_DISH_POOL) {
                        if (!currentDishes.contains(candidate)) {
                            currentDishes.add(candidate);
                            break;
                        }
                    }
                    updateUI();
                    Toast.makeText(this, "Hiện có " + currentDishes.size() + " món trên vòng quay", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đã đạt số món tối đa (" + MASTER_DISH_POOL.length + " món)!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Clear Dish List Button
        if (btnClearDishList != null) {
            btnClearDishList.setOnClickListener(v -> {
                if (edtDishList != null) {
                    edtDishList.setText("");
                    edtDishList.requestFocus();
                }
                Toast.makeText(this, "Đã xóa danh sách món ăn, hãy nhập các món mới!", Toast.LENGTH_SHORT).show();
            });
        }

        // Update Wheel Button (Applies custom typed dishes to the wheel)
        View.OnClickListener updateListener = v -> applyTypedDishes();
        if (btnUpdateWheel != null) {
            btnUpdateWheel.setOnClickListener(updateListener);
        }
        if (tvBtnUpdateWheel != null) {
            tvBtnUpdateWheel.setOnClickListener(updateListener);
        }

        // Center Spin Button (Trực tiếp quay ngẫu nhiên từ danh sách hiện tại)
        if (btnSpinCenter != null) {
            btnSpinCenter.setOnClickListener(v -> performSpinWheel());
        }

        // Navigation listeners
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        }
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
        if (navCreate != null) {
            navCreate.setOnClickListener(v -> startActivity(new Intent(this, CreatePostActivity.class)));
        }
        if (navMessages != null) {
            navMessages.setOnClickListener(v -> startActivity(new Intent(this, MessagesInboxActivity.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }
    }

    private void showAiSuggestDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_dialog_ai_suggest, null);
        dialog.setContentView(dialogView);

        ImageView btnCloseAiDialog = dialogView.findViewById(R.id.btnCloseAiDialog);
        TextView chipMood1 = dialogView.findViewById(R.id.chipMood1);
        TextView chipMood2 = dialogView.findViewById(R.id.chipMood2);
        TextView chipMood3 = dialogView.findViewById(R.id.chipMood3);
        TextView chipMood4 = dialogView.findViewById(R.id.chipMood4);
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

        if (chipMood1 != null) chipMood1.setOnClickListener(v -> { if (edtAiCraving != null) edtAiCraving.setText("Thèm món nước nóng hổi đậm đà"); });
        if (chipMood2 != null) chipMood2.setOnClickListener(v -> { if (edtAiCraving != null) edtAiCraving.setText("Ăn thanh đạm, nhiều rau ít dầu mỡ"); });
        if (chipMood3 != null) chipMood3.setOnClickListener(v -> { if (edtAiCraving != null) edtAiCraving.setText("Món cay nồng kích thích giải tỏa mệt mỏi"); });
        if (chipMood4 != null) chipMood4.setOnClickListener(v -> { if (edtAiCraving != null) edtAiCraving.setText("Món ăn nhanh gọn, tiện lợi và tiết kiệm"); });

        final List<String> aiSuggestedDishes = new ArrayList<>();

        if (btnCallGemini != null) {
            btnCallGemini.setOnClickListener(v -> {
                String promptText = edtAiCraving != null ? edtAiCraving.getText().toString().trim() : "";
                if (promptText.isEmpty()) {
                    promptText = "Gợi ý món ăn ngon hôm nay";
                }

                if (pbAiLoading != null) pbAiLoading.setVisibility(View.VISIBLE);
                if (tvBtnCallGemini != null) tvBtnCallGemini.setText("Gemini AI đang suy nghĩ...");
                btnCallGemini.setEnabled(false);

                FoodDecideRequest request = new FoodDecideRequest(promptText, "Bình dân", 1, promptText);
                ApiClient.getApiService().foodDecide(request).enqueue(new Callback<FoodDecideResponse>() {
                    @Override
                    public void onResponse(Call<FoodDecideResponse> call, Response<FoodDecideResponse> response) {
                        btnCallGemini.setEnabled(true);
                        if (pbAiLoading != null) pbAiLoading.setVisibility(View.GONE);
                        if (tvBtnCallGemini != null) tvBtnCallGemini.setText("🤖 Nhờ Gemini 2.5 Flash Gợi Ý Lại");

                        aiSuggestedDishes.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<FoodDecideResponse.Suggestion> list = response.body().getData().suggestions;
                            if (list != null && !list.isEmpty()) {
                                for (FoodDecideResponse.Suggestion s : list) {
                                    if (s.dishName != null && !s.dishName.isEmpty()) {
                                        aiSuggestedDishes.add(s.dishName);
                                    }
                                }
                                if (list.size() > 0 && tvAiDish1 != null) {
                                    tvAiDish1.setText("1. " + list.get(0).dishName);
                                    if (tvAiReason1 != null) tvAiReason1.setText(list.get(0).reason);
                                }
                                if (list.size() > 1 && tvAiDish2 != null) {
                                    tvAiDish2.setText("2. " + list.get(1).dishName);
                                    if (tvAiReason2 != null) tvAiReason2.setText(list.get(1).reason);
                                }
                                if (list.size() > 2 && tvAiDish3 != null) {
                                    tvAiDish3.setText("3. " + list.get(2).dishName);
                                    if (tvAiReason3 != null) tvAiReason3.setText(list.get(2).reason);
                                }
                            }
                        }

                        if (aiSuggestedDishes.isEmpty()) {
                            // Fallback default suggestions
                            aiSuggestedDishes.add("Phở Bò Tái Lăn");
                            aiSuggestedDishes.add("Bún Bò Huế");
                            aiSuggestedDishes.add("Bánh Mì Pate");
                            if (tvAiDish1 != null) tvAiDish1.setText("1. Phở Bò Tái Lăn");
                            if (tvAiDish2 != null) tvAiDish2.setText("2. Bún Bò Huế");
                            if (tvAiDish3 != null) tvAiDish3.setText("3. Bánh Mì Pate");
                        }

                        if (containerAiResults != null) containerAiResults.setVisibility(View.VISIBLE);
                        Toast.makeText(DecideActivity.this, "Google Gemini đã gợi ý xong 3 món!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<FoodDecideResponse> call, Throwable t) {
                        btnCallGemini.setEnabled(true);
                        if (pbAiLoading != null) pbAiLoading.setVisibility(View.GONE);
                        if (tvBtnCallGemini != null) tvBtnCallGemini.setText("🤖 Nhờ Gemini 2.5 Flash Gợi Ý");

                        aiSuggestedDishes.clear();
                        aiSuggestedDishes.add("Phở Bò Tái Lăn");
                        aiSuggestedDishes.add("Bún Bò Huế");
                        aiSuggestedDishes.add("Cơm Tấm Sườn Bì");
                        if (containerAiResults != null) containerAiResults.setVisibility(View.VISIBLE);
                        Toast.makeText(DecideActivity.this, "Đã gợi ý món ngon cho bạn!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DecideActivity.this, "Đã đưa " + aiSuggestedDishes.size() + " món từ AI Gemini vào vòng quay!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DecideActivity.this, "Chưa có món nào được gợi ý!", Toast.LENGTH_SHORT).show();
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
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
        } else if (rawText.contains(",")) {
            String[] parts = rawText.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    list.add(trimmed);
                }
            }
        } else {
            String trimmed = rawText.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
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

        currentDishes.clear();
        currentDishes.addAll(validDishes);

        int count = currentDishes.size();
        if (tvItemCountDisplay != null) tvItemCountDisplay.setText(count + " món");
        if (tvDishCountBadge != null) tvDishCountBadge.setText(count + " món đã nhập");

        // Immediately update WheelView with all custom dishes!
        if (wheelView != null) {
            wheelView.setItems(currentDishes);
            wheelView.setRotation(0f);
        }

        updateChipStyles(count);

        // Hide keyboard
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ignored) {}

        Toast.makeText(this, "Đã cập nhật vòng quay với " + count + " món ăn!", Toast.LENGTH_SHORT).show();
    }

    private void performSpinWheel() {
        // 1. Tự động đồng bộ các món mới nếu người dùng vừa gõ vào ô nhập mà quên bấm "Cập nhật"
        if (edtDishList != null) {
            List<String> typedList = parseDishesFromText(edtDishList.getText().toString());
            if (typedList.size() >= 2 && !typedList.equals(currentDishes)) {
                currentDishes.clear();
                currentDishes.addAll(typedList);
                if (wheelView != null) {
                    wheelView.setItems(currentDishes);
                }
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

        // 2. CHỌN MÓN CHIẾN THẮNG HOÀN TOÀN NGẪU NHIÊN 100%
        int count = currentDishes.size();
        int winningIndex = random.nextInt(count);
        final String winningDish = currentDishes.get(winningIndex);

        // 3. Tính toán góc quay chính xác để ô trúng thưởng dừng đúng ngay dưới kim chỉ ở đỉnh (12h - 270 độ)
        float sweepAngle = 360f / count;
        float midAngle = winningIndex * sweepAngle + (sweepAngle / 2f);
        float targetOffset = (270f - midAngle + 360f) % 360f;

        // Thêm độ lệch tự nhiên ngẫu nhiên trong khoảng +/- 25% độ rộng nan quạt
        float jitter = (random.nextFloat() * 2f - 1f) * (sweepAngle * 0.25f);
        float finalTargetAngleMod = (targetOffset + jitter + 360f) % 360f;

        float currentRot = wheelView != null ? wheelView.getRotation() : 0f;
        float currentMod = (currentRot % 360f + 360f) % 360f;
        float delta = finalTargetAngleMod - currentMod;
        if (delta < 0) {
            delta += 360f;
        }

        // Quay từ 5 đến 6 vòng trước khi dừng
        int fullSpins = 5 + random.nextInt(2);
        float targetRotation = currentRot + (fullSpins * 360f) + delta;

        if (wheelView != null) {
            wheelView.animate()
                    .rotation(targetRotation)
                    .setDuration(2600)
                    .setInterpolator(new DecelerateInterpolator(1.8f))
                    .withEndAction(() -> {
                        btnSpinCenter.setEnabled(true);
                        // Giữ 450ms để người dùng thấy rõ kim đang chỉ đúng món trúng rồi mới chuyển màn hình
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
