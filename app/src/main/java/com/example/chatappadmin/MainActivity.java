package com.example.chatappadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RequestListAdapter requestListAdapter;
    private List<RequestModel> requestList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.requests);
        progressBar = findViewById(R.id.progressBar);

        requestList = new ArrayList<>();
        requestListAdapter = new RequestListAdapter(requestList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(requestListAdapter);

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        // Load data with snapshot listener (faster)
        loadAllData();
    }

    private void loadAllData() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("investigateMessage")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error loading data", error);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    requestList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        RequestModel msg = doc.toObject(RequestModel.class);
                        requestList.add(msg);
                    }
                    requestListAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }
}

