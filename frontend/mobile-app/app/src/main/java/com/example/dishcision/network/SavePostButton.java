package com.example.dishcision.network;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dishcision.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Bookmark (🔖) button for one post card. Flips the icon immediately, calls
 * POST/DELETE posts/{postId}/save, and rolls back if the server refuses.
 * Saved posts appear in "Bài viết đã lưu" (GET users/me/saved-posts).
 */
public final class SavePostButton {

    private final Activity activity;
    private final ImageView button;
    private int postId;          // 0 until the card is bound to a real post
    private String authorName;
    private boolean saved;
    private boolean inFlight;

    public SavePostButton(Activity activity, ImageView button) {
        this.activity = activity;
        this.button = button;
        if (button != null) button.setOnClickListener(v -> toggle());
        render();
    }

    /** Attach the card to a post returned by the API. */
    public void bind(int postId, boolean isSaved, String authorName) {
        this.postId = postId;
        this.saved = isSaved;
        this.authorName = authorName;
        render();
    }

    private void toggle() {
        if (postId <= 0) {
            Toast.makeText(activity, "Bài viết chưa tải xong, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (inFlight) return;

        final boolean target = !saved;
        saved = target;
        render();
        inFlight = true;

        String auth = ApiClient.getAuthHeader(activity);
        Call<Object> call = target
                ? ApiClient.getApiService().savePost(auth, postId)
                : ApiClient.getApiService().unsavePost(auth, postId);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> c, Response<Object> response) {
                inFlight = false;
                if (activity.isFinishing()) return;
                if (response.isSuccessful()) {
                    String who = authorName != null && !authorName.isEmpty() ? " của " + authorName : "";
                    Toast.makeText(activity,
                            target ? "Đã lưu bài viết" + who + " vào mục Bài viết đã lưu" : "Đã bỏ lưu bài viết",
                            Toast.LENGTH_SHORT).show();
                } else {
                    rollback(response.code() == 401
                            ? "Vui lòng đăng nhập lại để lưu bài viết"
                            : "Không thể " + (target ? "lưu" : "bỏ lưu") + " bài viết (mã " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Object> c, Throwable t) {
                inFlight = false;
                if (activity.isFinishing()) return;
                rollback("Không thể kết nối máy chủ");
            }

            private void rollback(String message) {
                saved = !target;
                render();
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render() {
        if (button != null) {
            button.setImageResource(saved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
        }
    }
}
