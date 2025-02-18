package com.example.sharedfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class UserPostListActivity extends AppCompatActivity {

    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post_list);

        db = FirebaseFirestore.getInstance();
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(new ArrayList<>(), null, this::viewUserPosts);
        userRecyclerView.setAdapter(userAdapter);

        loadUsers();
    }

    private void loadUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<User> userList = new ArrayList<>();
                task.getResult().forEach(document -> {
                    String userId = document.getId();  // 🔴 קבלת userId מה-Firestore
                    String email = document.getString("email");
                    boolean isBanned = document.getBoolean("isBanned") != null && document.getBoolean("isBanned");
                    Long tempBanTime = document.getLong("tempBanTime");

                    userList.add(new User(userId, email, isBanned, tempBanTime));
                });
                userAdapter.updateUsers(userList);
            }
        });
    }


    private void viewUserPosts(User user) {
        Log.d("UserPostListActivity", "📌 Sending userId: " + user.getId());

        Intent intent = new Intent(this, PostsOfUserActivity.class);
        intent.putExtra("userId", user.getId()); // שולחים את ה-ID הנכון
        startActivity(intent);
    }



}
