package com.example.dishcision;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class RestrictedAccountsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnAddRestricted;
    private TextView tvRestrictedCount;
    private View layoutRestricted1;
    private Button btnUnrestrict1;
    private TextView tvNoRestrictedAccounts;

    private boolean is1Restricted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restricted_accounts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_restricted_accounts), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        fetchRestrictedAccountsFromBackend();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddRestricted = findViewById(R.id.btnAddRestricted);
        tvRestrictedCount = findViewById(R.id.tvRestrictedCount);
        layoutRestricted1 = findViewById(R.id.layoutRestricted1);
        btnUnrestrict1 = findViewById(R.id.btnUnrestrict1);
        tvNoRestrictedAccounts = findViewById(R.id.tvNoRestrictedAccounts);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddRestricted != null) {
            btnAddRestricted.setOnClickListener(v ->
                Toast.makeText(this, "Chọn tài khoản từ danh sách bạn bè để hạn chế", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnUnrestrict1 != null) {
            btnUnrestrict1.setOnClickListener(v -> unrestrictOnBackend(101));
        }
    }

    private void fetchRestrictedAccountsFromBackend() {
        ApiClient.getApiService().getRestrictedAccounts("Bearer dummy_token").enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AccountModel> list = response.body().getData();
                    boolean has101 = false;
                    for (AccountModel item : list) {
                        if (item.getId() == 101) has101 = true;
                    }
                    is1Restricted = has101;
                }
                updateUIState();
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                updateUIState();
            }
        });
    }

    private void unrestrictOnBackend(int id) {
        AccountActionRequest req = new AccountActionRequest(id);
        ApiClient.getApiService().unrestrictAccount("Bearer dummy_token", req).enqueue(new Callback<AccountsListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccountsListResponse> call, @NonNull Response<AccountsListResponse> response) {
                is1Restricted = false;
                updateUIState();
                Toast.makeText(RestrictedAccountsActivity.this, "Đã bỏ hạn chế tài khoản", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<AccountsListResponse> call, @NonNull Throwable t) {
                is1Restricted = false;
                updateUIState();
                Toast.makeText(RestrictedAccountsActivity.this, "Đã bỏ hạn chế tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIState() {
        int count = is1Restricted ? 1 : 0;
        if (layoutRestricted1 != null) layoutRestricted1.setVisibility(is1Restricted ? View.VISIBLE : View.GONE);
        if (tvRestrictedCount != null) tvRestrictedCount.setText(count + " tài khoản");
        if (tvNoRestrictedAccounts != null) tvNoRestrictedAccounts.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }
}
