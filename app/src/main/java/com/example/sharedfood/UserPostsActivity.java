package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserPostsActivity extends AppCompatActivity implements MyPostsAdapter.PostDeleteListener, MyPostsAdapter.PostEditListener {

    private static final String TAG = "UserPostsActivity";
    private RecyclerView postRecyclerView;
    private MyPostsAdapter postAdapter;
    private FirebaseFirestore db;
    private String userId; // ✅ הוספנו את המשתנה שחסר לך
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_posts);

        db = FirebaseFirestore.getInstance();
        postRecyclerView = findViewById(R.id.postRecyclerView);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();

        boolean isAdminView = getIntent().getBooleanExtra("isAdminView", false);
        postAdapter = new MyPostsAdapter(postList, this, this, isAdminView);
        postRecyclerView.setAdapter(postAdapter);

        this.userId = getIntent().getStringExtra("userId");
        Log.d(TAG, "📥 Received userId: " + userId);
        Log.d("UserPostsActivity", "📥 Received userId: " + userId); // ✅ הדפסת מה שהתקבל

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "שגיאה: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "📩 Received userId: " + userId);

        loadUserPosts();
    }


    private void loadUserPosts() {
        Log.d(TAG, "📥 Received userId: " + userId); // ✅ בדיקה אם ה-ID התקבל נכון

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "❌ שגיאה: userId חסר!");
            Toast.makeText(this, "שגיאה: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔍 Fetching posts for userId: " + userId);
        db.collection("posts")
                .whereEqualTo("userId", userId.trim()) // ✅ מסיר רווחים מיותרים
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();

                        if (task.getResult().isEmpty()) {
                            Log.d(TAG, "⚠️ No posts found for userId: " + userId);
                            Toast.makeText(this, "למשתמש זה אין פוסטים", Toast.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Post post = document.toObject(Post.class);
                                post.setId(document.getId());
                                postList.add(post);
                                Log.d(TAG, "✅ Loaded post ID: " + post.getId() + ", Description: " + post.getDescription());
                            }
                        }

                        postAdapter.notifyDataSetChanged();
                        Log.d(TAG, "🔄 Adapter updated with " + postList.size() + " posts.");
                    } else {
                        Log.e(TAG, "❌ Failed to load posts", task.getException());
                        Toast.makeText(this, "שגיאה בטעינת הפוסטים", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onDeleteClick(Post post) {
        db.collection("posts").document(post.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "הפוסט נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "📩 Received userId: " + userId);
                    loadUserPosts(); // רענון הרשימה
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting post", e);
                    Toast.makeText(this, "שגיאה במחיקת הפוסט", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(Post post) {
        Intent intent = new Intent(this, EditPostActivity.class);
        intent.putExtra("postId", post.getId());
        startActivity(intent);
    }
}
