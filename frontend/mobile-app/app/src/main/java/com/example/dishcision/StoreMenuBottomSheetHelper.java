package com.example.dishcision;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.dishcision.network.ApiClient;
import com.example.dishcision.network.ImageLoader;
import com.example.dishcision.network.StoreDetailResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoreMenuBottomSheetHelper {

    public static void showByStoreId(Activity activity, int storeId, String fallbackName) {
        if (activity == null || activity.isFinishing() || storeId <= 0) return;

        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.layout_dialog_menu_bottomsheet, null);
        dialog.setContentView(view);

        TextView tvStoreName = view.findViewById(R.id.tvMenuStoreName);
        TextView tvStoreMeta = view.findViewById(R.id.tvMenuStoreMeta);
        View btnClose = view.findViewById(R.id.btnCloseMenu);
        if (tvStoreName != null && fallbackName != null && !fallbackName.isEmpty()) {
            tvStoreName.setText("Menu · " + fallbackName);
        }
        if (tvStoreMeta != null) {
            tvStoreMeta.setText("Đang tải thực đơn...");
        }
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();

        String authHeader = ApiClient.getAuthHeader(activity);
        ApiClient.getApiService().getStoreDetail(authHeader, storeId).enqueue(new Callback<StoreDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<StoreDetailResponse> call, @NonNull Response<StoreDetailResponse> response) {
                if (activity.isFinishing() || !dialog.isShowing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    bindData(activity, dialog, view, response.body().data);
                } else {
                    TextView tvEmpty = view.findViewById(R.id.tvEmptyMenu);
                    if (tvEmpty != null) {
                        tvEmpty.setText("Không thể tải thực đơn lúc này");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                    if (tvStoreMeta != null) tvStoreMeta.setText("");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StoreDetailResponse> call, @NonNull Throwable t) {
                if (activity.isFinishing() || !dialog.isShowing()) return;
                TextView tvEmpty = view.findViewById(R.id.tvEmptyMenu);
                if (tvEmpty != null) {
                    tvEmpty.setText("Lỗi kết nối máy chủ");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                if (tvStoreMeta != null) tvStoreMeta.setText("");
            }
        });
    }

    public static void showWithData(Activity activity, StoreDetailResponse.StoreDetailData data) {
        if (activity == null || activity.isFinishing() || data == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.layout_dialog_menu_bottomsheet, null);
        dialog.setContentView(view);

        View btnClose = view.findViewById(R.id.btnCloseMenu);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        bindData(activity, dialog, view, data);
        dialog.show();
    }

    private static void bindData(Activity activity, BottomSheetDialog dialog, View view, StoreDetailResponse.StoreDetailData data) {
        TextView tvStoreName = view.findViewById(R.id.tvMenuStoreName);
        TextView tvStoreMeta = view.findViewById(R.id.tvMenuStoreMeta);
        View layoutFeatured = view.findViewById(R.id.layoutFeaturedDish);
        ImageView imgFeatured = view.findViewById(R.id.imgFeaturedDish);
        TextView tvFeaturedName = view.findViewById(R.id.tvFeaturedDishName);
        TextView tvFeaturedDesc = view.findViewById(R.id.tvFeaturedDishDesc);
        TextView tvFeaturedPrice = view.findViewById(R.id.tvFeaturedDishPrice);
        LinearLayout layoutCategories = view.findViewById(R.id.layoutMenuCategories);
        LinearLayout layoutItems = view.findViewById(R.id.layoutMenuItemsContainer);
        TextView tvEmpty = view.findViewById(R.id.tvEmptyMenu);

        if (tvStoreName != null) {
            tvStoreName.setText("Menu · " + (data.name != null ? data.name : ""));
        }

        if (tvStoreMeta != null) {
            StringBuilder meta = new StringBuilder(data.isOpen ? "Đang mở cửa" : "Đã đóng cửa");
            if (data.openHours != null && !data.openHours.isEmpty()) {
                meta.append("  ").append(data.openHours);
            }
            if (data.address != null && !data.address.isEmpty()) {
                meta.append(" • ").append(data.address);
            }
            tvStoreMeta.setText(meta.toString());
        }

        List<StoreDetailResponse.MenuDishItem> menu = data.menu != null ? data.menu : new ArrayList<>();

        if (layoutFeatured != null) {
            String highlight = data.highlightDish != null ? data.highlightDish.trim() : "";
            if (!highlight.isEmpty()) {
                layoutFeatured.setVisibility(View.VISIBLE);
                if (tvFeaturedName != null) tvFeaturedName.setText(highlight);
                if (tvFeaturedDesc != null) tvFeaturedDesc.setText(data.dishDescription != null ? data.dishDescription : "");
                if (tvFeaturedPrice != null) tvFeaturedPrice.setText(data.dishPrice != null ? data.dishPrice : "");
                String featImg = null;
                int featDishId = 0;
                for (StoreDetailResponse.MenuDishItem d : menu) {
                    if (d.name != null && d.name.equalsIgnoreCase(highlight)) {
                        featImg = d.imageUrl;
                        featDishId = d.id;
                        break;
                    }
                }
                if (imgFeatured != null && featImg != null && !featImg.isEmpty()) {
                    ImageLoader.loadImage(imgFeatured, featImg);
                }
                if (featDishId > 0) {
                    int targetId = featDishId;
                    layoutFeatured.setOnClickListener(v -> {
                        dialog.dismiss();
                        Intent intent = new Intent(activity, DishDetailActivity.class);
                        intent.putExtra("DISH_ID", targetId);
                        activity.startActivity(intent);
                    });
                }
            } else {
                layoutFeatured.setVisibility(View.GONE);
            }
        }

        if (menu.isEmpty()) {
            if (layoutCategories != null) layoutCategories.removeAllViews();
            if (layoutItems != null) layoutItems.removeAllViews();
            if (tvEmpty != null) {
                tvEmpty.setText("Quán chưa cập nhật danh sách món ăn.");
                tvEmpty.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        Map<String, List<StoreDetailResponse.MenuDishItem>> grouped = new LinkedHashMap<>();
        for (StoreDetailResponse.MenuDishItem item : menu) {
            String cat = (item.category != null && !item.category.trim().isEmpty()) ? item.category.trim() : "Món chính";
            List<StoreDetailResponse.MenuDishItem> bucket = grouped.get(cat);
            if (bucket == null) {
                bucket = new ArrayList<>();
                grouped.put(cat, bucket);
            }
            bucket.add(item);
        }

        final String[] selectedCategory = {"Tất cả"};
        if (layoutCategories != null) {
            List<String> catNames = new ArrayList<>();
            catNames.add("Tất cả");
            catNames.addAll(grouped.keySet());
            renderCategoryChips(activity, dialog, layoutCategories, catNames, selectedCategory, grouped, layoutItems);
        }

        renderMenuSections(activity, dialog, layoutItems, grouped, "Tất cả");
    }

    private static void renderCategoryChips(
            Activity activity,
            BottomSheetDialog dialog,
            LinearLayout container,
            List<String> categories,
            String[] selectedCategory,
            Map<String, List<StoreDetailResponse.MenuDishItem>> grouped,
            LinearLayout itemsContainer
    ) {
        container.removeAllViews();
        float density = activity.getResources().getDisplayMetrics().density;
        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            boolean active = cat.equals(selectedCategory[0]);
            TextView chip = new TextView(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) params.setMarginStart(Math.round(10 * density));
            chip.setLayoutParams(params);
            chip.setPadding(Math.round(16 * density), Math.round(8 * density), Math.round(16 * density), Math.round(8 * density));
            chip.setText(cat);
            chip.setTextSize(13f);
            if (active) {
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                chip.setTextColor(ContextCompat.getColor(activity, R.color.white));
                chip.setTypeface(null, Typeface.BOLD);
            } else {
                chip.setBackgroundResource(R.drawable.bg_favorite_search);
                chip.setTextColor(ContextCompat.getColor(activity, R.color.text_dark));
            }
            chip.setOnClickListener(v -> {
                selectedCategory[0] = cat;
                renderCategoryChips(activity, dialog, container, categories, selectedCategory, grouped, itemsContainer);
                renderMenuSections(activity, dialog, itemsContainer, grouped, cat);
            });
            container.addView(chip);
        }
    }

    private static void renderMenuSections(
            Activity activity,
            BottomSheetDialog dialog,
            LinearLayout container,
            Map<String, List<StoreDetailResponse.MenuDishItem>> grouped,
            String filterCat
    ) {
        if (container == null) return;
        container.removeAllViews();
        float density = activity.getResources().getDisplayMetrics().density;

        boolean firstSection = true;
        for (Map.Entry<String, List<StoreDetailResponse.MenuDishItem>> entry : grouped.entrySet()) {
            if (!"Tất cả".equals(filterCat) && !entry.getKey().equals(filterCat)) continue;
            List<StoreDetailResponse.MenuDishItem> dishes = entry.getValue();

            LinearLayout sectionHeader = new LinearLayout(activity);
            LinearLayout.LayoutParams hdrParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hdrParams.topMargin = Math.round((firstSection ? 4 : 20) * density);
            sectionHeader.setLayoutParams(hdrParams);
            sectionHeader.setOrientation(LinearLayout.HORIZONTAL);
            firstSection = false;

            TextView tvSecTitle = new TextView(activity);
            tvSecTitle.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvSecTitle.setText(entry.getKey());
            tvSecTitle.setTextColor(ContextCompat.getColor(activity, R.color.text_dark));
            tvSecTitle.setTextSize(17f);
            tvSecTitle.setTypeface(null, Typeface.BOLD);

            TextView tvSecCount = new TextView(activity);
            tvSecCount.setText(dishes.size() + " món");
            tvSecCount.setTextColor(ContextCompat.getColor(activity, R.color.text_gray));
            tvSecCount.setTextSize(12f);

            sectionHeader.addView(tvSecTitle);
            sectionHeader.addView(tvSecCount);
            container.addView(sectionHeader);

            for (StoreDetailResponse.MenuDishItem dish : dishes) {
                LinearLayout row = new LinearLayout(activity);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rowParams.topMargin = Math.round(12 * density);
                row.setLayoutParams(rowParams);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setClickable(true);
                row.setFocusable(true);
                row.setPadding(0, Math.round(4 * density), 0, Math.round(4 * density));

                LinearLayout topLine = new LinearLayout(activity);
                topLine.setOrientation(LinearLayout.HORIZONTAL);

                TextView tvName = new TextView(activity);
                tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                tvName.setText(dish.name != null ? dish.name : "");
                tvName.setTextColor(ContextCompat.getColor(activity, R.color.text_dark));
                tvName.setTextSize(15f);
                tvName.setTypeface(null, Typeface.BOLD);

                TextView tvPrice = new TextView(activity);
                tvPrice.setText(dish.price != null ? dish.price : "");
                tvPrice.setTextColor(ContextCompat.getColor(activity, R.color.text_dark));
                tvPrice.setTextSize(15f);
                tvPrice.setTypeface(null, Typeface.BOLD);

                topLine.addView(tvName);
                topLine.addView(tvPrice);
                row.addView(topLine);

                String descText = dish.desc != null && !dish.desc.isEmpty() ? dish.desc : dish.description;
                if (descText != null && !descText.trim().isEmpty()) {
                    TextView tvDesc = new TextView(activity);
                    LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    descParams.topMargin = Math.round(2 * density);
                    tvDesc.setLayoutParams(descParams);
                    tvDesc.setText(descText.trim());
                    tvDesc.setTextColor(ContextCompat.getColor(activity, R.color.text_gray));
                    tvDesc.setTextSize(12f);
                    row.addView(tvDesc);
                }

                row.setOnClickListener(v -> {
                    if (dish.id <= 0) {
                        Toast.makeText(activity, "Không có chi tiết cho món này", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dialog.dismiss();
                    Intent intent = new Intent(activity, DishDetailActivity.class);
                    intent.putExtra("DISH_ID", dish.id);
                    activity.startActivity(intent);
                });

                container.addView(row);
            }
        }
    }
}

