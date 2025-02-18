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

public class PostsOfUserActivity extends AppCompatActivity {

    private static final String TAG = "PostsOfUserActivity";
    private RecyclerView postRecyclerView;
    private MyPostsAdapter postAdapter;
    private FirebaseFirestore db;
    private String userId; // קבלת ה-ID של המשתמש שנבחר
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_of_user); // ניצור layout מתאים

        db = FirebaseFirestore.getInstance();
        postRecyclerView = findViewById(R.id.postRecyclerView);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new MyPostsAdapter(postList, null, null, false); // מצב רגיל, ללא עריכה
        postRecyclerView.setAdapter(postAdapter);

        // מקבלים את userId שהועבר מהאקטיביטי הקודם
        userId = getIntent().getStringExtra("userId");

        Log.d(TAG, "📥 Received userId: " + userId);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "שגיאה: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserPosts();
    }

    private void loadUserPosts() {
        Log.d(TAG, "🔍 Fetching posts for userId: " + userId);

        db.collection("posts")
                .whereEqualTo("userId", userId) // חיפוש לפי userId
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
}
