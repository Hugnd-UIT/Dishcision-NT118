package com.example.dishcision.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/forgot-password")
    Call<AuthResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auth/reset-password")
    Call<AuthResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("auth/change-password")
    Call<AuthResponse> changePassword(
            @Header("Authorization") String authHeader,
            @Body ChangePasswordRequest request
    );
}
