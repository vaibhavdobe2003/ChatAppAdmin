package com.example.chatappadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.chatappadmin.R;
import com.example.chatappadmin.RequestModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.ViewHolder> {

    private List<RequestModel> requestList;

    public RequestListAdapter(List<RequestModel> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestModel msg = requestList.get(position);
        holder.chatName.setText( msg.getUserName());

        String formattedDate = android.text.format.DateFormat.format("dd-MM-yy hh:mm a", msg.getTimestamp()).toString();
        holder.timestamp.setText(formattedDate);

        holder.arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imageUrl = msg.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Intent intent = new Intent(v.getContext(), ViewDataActivity.class);
                    intent.putExtra("imageUrl", imageUrl); // 🔁 pass the URL
                    v.getContext().startActivity(intent);
                }
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition(); // ⚠️ don't cache early

                if (adapterPosition != RecyclerView.NO_POSITION) {
                    RequestModel request = requestList.get(adapterPosition);

                    FirebaseFirestore.getInstance()
                            .collection("investigateMessage")
                            .document(request.getDocumentId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                int latestPosition = holder.getAdapterPosition(); // 🔁 get fresh position
                                if (latestPosition != RecyclerView.NO_POSITION && latestPosition < requestList.size()) {
                                    requestList.remove(latestPosition);
                                    notifyItemRemoved(latestPosition);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(v.getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatName,timestamp;
        ImageView arrow;
        ImageView delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName);
            timestamp = itemView.findViewById(R.id.time);
            arrow=itemView.findViewById(R.id.arrow);
            delete=itemView.findViewById(R.id.delete);
        }
    }
}
