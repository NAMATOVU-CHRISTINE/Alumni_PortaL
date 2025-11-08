package com.namatovu.alumniportal.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    
    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_RECEIVED = 2;
    
    private List<ChatMessage> messages;
    private String currentUserId;
    private String currentUserName;
    
    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }
    
    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getSenderId().equals(currentUserId) ? MESSAGE_SENT : MESSAGE_RECEIVED;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        
        if (viewType == MESSAGE_SENT) {
            view = inflater.inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = inflater.inflate(R.layout.item_message_received, parent, false);
        }
        
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage;
        private TextView textTime;
        private TextView textSenderName;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textViewMessage);
            textTime = itemView.findViewById(R.id.textViewTime);
            textSenderName = itemView.findViewById(R.id.textViewSenderName);
        }
        
        public void bind(ChatMessage message) {
            if (textMessage != null && message.getMessageText() != null) {
                textMessage.setText(message.getMessageText());
                textMessage.setVisibility(View.VISIBLE);  // Make message visible!
            }
            
            if (textTime != null) {
                textTime.setText(message.getFormattedTime());
            }
            
            if (textSenderName != null) {
                String senderName = message.getSenderName();
                if (senderName != null && !senderName.isEmpty()) {
                    textSenderName.setText(senderName);
                    textSenderName.setVisibility(View.VISIBLE);
                } else {
                    textSenderName.setVisibility(View.GONE);
                }
            }
        }
    }
}