package com.example.dishcision;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishcision.network.AccountActionRequest;
import com.example.dishcision.network.AccountModel;
import com.example.dishcision.network.AccountsListResponse;
import com.example.dishcision.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockedAccountsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnAddBlocked;
    private EditText edtSearchBlocked;
    private TextView tvBlockedCount;

    private View layoutBlocked1, layoutBlocked2, layoutBlocked3;
    private View divider1, divider2;
    private Button btnUnblock1, btnUnblock2, btnUnblock3;
    private TextView tvNoBlockedAccounts;

    private boolean is1Blocked = true;
    private boolean is2Blocked = true;
    private boolean is3Blocked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blocked_accounts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_blocked_accounts), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchBlockedAccountsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddBlocked = findViewById(R.id.btnAddBlocked);
        edtSearchBlocked = findViewById(R.id.edtSearchBlocked);
        tvBlockedCount = findViewById(R.id.tvBlockedCount);

        layoutBlocked1 = findViewById(R.id.layoutBlocked1);
        layoutBlocked2 = findViewById(R.id.layoutBlocked2);
        layoutBlocked3 = findViewById(R.id.layoutBlocked3);

        divider1 = findViewById(R.id.divider1);
        divider2 = findViewById(R.id.divider2);

        btnUnblock1 = findViewById(R.id.btnUnblock1);
        btnUnblock2 = findViewById(R.id.btnUnblock2);
        btnUnblock3 = findViewById(R.id.btnUnblock3);

        tvNoBlockedAccounts = findViewById(R.id.tvNoBlockedAccounts);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddBlocked != null) {
            btnAddBlocked.setOnClickListener(v ->
                Toast.makeText(this, "Chọn tài khoản từ danh sách bạn bè để chặn", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnUnblock1 != null) {
            btnUnblock1.setOnClickListener(v -> unblockOnBackend(1));
        }

        if (btnUnblock2 != null) {
            btnUnblock2.setOnClickListener(v -> unblockOnBackend(2));
        }

        if (btnUnblock3 != null) {
            btnUnblock3.setOnClickListener(v -> unblockOnBackend(3));
        }

        if (edtSearchBlocked != null) {
            edtSearchBlocked.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterList(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void fetchBlockedAccountsFromBackend() {
        ApiClient.getApiService().getBlockedAccounts("Bearer dummy_token").enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AccountModel> list = response.body().getData();
                    boolean has1 = false, has2 = false, has3 = false;
                    for (AccountModel item : list) {
                        if (item.getId() == 1) has1 = true;
                        if (item.getId() == 2) has2 = true;
                        if (item.getId() == 3) has3 = true;
                    }
                    is1Blocked = has1;
                    is2Blocked = has2;
                    is3Blocked = has3;
                }
                updateUIState();
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                updateUIState();
            }
        });
    }

    private void unblockOnBackend(int id) {
        AccountActionRequest req = new AccountActionRequest(id);
        ApiClient.getApiService().unblockAccount("Bearer dummy_token", req).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (id == 1) is1Blocked = false;
                if (id == 2) is2Blocked = false;
                if (id == 3) is3Blocked = false;
                updateUIState();
                Toast.makeText(BlockedAccountsActivity.this, "Đã bỏ chặn tài khoản", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                if (id == 1) is1Blocked = false;
                if (id == 2) is2Blocked = false;
                if (id == 3) is3Blocked = false;
                updateUIState();
                Toast.makeText(BlockedAccountsActivity.this, "Đã bỏ chặn tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIState() {
        int count = 0;
        if (is1Blocked) count++;
        if (is2Blocked) count++;
        if (is3Blocked) count++;

        if (layoutBlocked1 != null) layoutBlocked1.setVisibility(is1Blocked ? View.VISIBLE : View.GONE);
        if (layoutBlocked2 != null) layoutBlocked2.setVisibility(is2Blocked ? View.VISIBLE : View.GONE);
        if (layoutBlocked3 != null) layoutBlocked3.setVisibility(is3Blocked ? View.VISIBLE : View.GONE);

        if (divider1 != null) divider1.setVisibility((is1Blocked && (is2Blocked || is3Blocked)) ? View.VISIBLE : View.GONE);
        if (divider2 != null) divider2.setVisibility((is2Blocked && is3Blocked) ? View.VISIBLE : View.GONE);

        if (tvBlockedCount != null) tvBlockedCount.setText(count + " tài khoản");
        if (tvNoBlockedAccounts != null) tvNoBlockedAccounts.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void filterList(String query) {
        if (query == null || query.trim().isEmpty()) {
            updateUIState();
            return;
        }
        String q = query.toLowerCase().trim();

        boolean show1 = is1Blocked && "minh anh".contains(q);
        boolean show2 = is2Blocked && "hoàng nam".contains(q);
        boolean show3 = is3Blocked && "thanh trúc".contains(q);

        if (layoutBlocked1 != null) layoutBlocked1.setVisibility(show1 ? View.VISIBLE : View.GONE);
        if (layoutBlocked2 != null) layoutBlocked2.setVisibility(show2 ? View.VISIBLE : View.GONE);
        if (layoutBlocked3 != null) layoutBlocked3.setVisibility(show3 ? View.VISIBLE : View.GONE);

        if (divider1 != null) divider1.setVisibility((show1 && (show2 || show3)) ? View.VISIBLE : View.GONE);
        if (divider2 != null) divider2.setVisibility((show2 && show3) ? View.VISIBLE : View.GONE);
    }
}
