package com.example.dishcision.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.example.dishcision.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class AuthHelper {

    public static void logout(Activity activity) {
        if (activity == null) return;

        // 1. Clear TokenManager tokens
        new TokenManager(activity).clearAuth();

        // 2. Clear Dishcision preferences
        activity.getSharedPreferences("DishcisionPrefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // 3. Clear Google Sign-In session
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignIn.getClient(activity, gso).signOut();
        } catch (Exception ignored) {}

        // 4. Redirect to LoginActivity and clear backstack
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
