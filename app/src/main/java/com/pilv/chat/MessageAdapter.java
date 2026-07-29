package com.pilv.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<String> messages;

    public MessageAdapter(List<String> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String message = messages.get(position);
        holder.messageText.setText(message);

        // Cloud fade effect - older messages are slightly faded
        float alpha = 1.0f - (0.03f * (messages.size() - position));
        holder.itemView.setAlpha(Math.max(alpha, 0.3f));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(android.R.id.text1);
            messageText.setTextColor(0xFFFFFFFF);
            messageText.setTextSize(16);
        }
    }
}