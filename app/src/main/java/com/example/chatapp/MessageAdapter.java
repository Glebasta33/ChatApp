package com.example.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;

    public MessageAdapter() {
        this.messages = new ArrayList<>();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.textViewAuthor.setText(message.getAuthor());
        if (message.getTextOfMessage() != null && !message.getTextOfMessage().isEmpty()) {
            holder.textViewTextOfMessage.setText(message.getTextOfMessage());
            holder.imageViewImage.setVisibility(View.GONE);
        }
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.imageViewImage.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getImageUrl()).into(holder.imageViewImage);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewAuthor;
        private TextView textViewTextOfMessage;
        private ImageView imageViewImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            textViewTextOfMessage = itemView.findViewById(R.id.textViewTextOfMessage);
            imageViewImage = itemView.findViewById(R.id.imageViewImage);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }
}
