package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.Chat;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private Context context;
    private List<Chat> chats;
    private String currentUserId;
    private OnChatClickListener listener;
    
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
        void onChatLongClick(Chat chat);
    }
    
    public ChatListAdapter(Context context, List<Chat> chats, String currentUserId) {
        this.context = context;
        this.chats = chats;
        this.currentUserId = currentUserId;
    }
    
    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }
    
    @Override
    public int getItemCount() {
        return chats.size();
    }
    
    class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProfile;
        private TextView textViewName;
        private TextView textViewLastMessage;
        private TextView textViewTime;
        private TextView textViewUnreadCount;
        private View viewUnreadIndicator;
        
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewLastMessage = itemView.findViewById(R.id.textViewLastMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewUnreadCount = itemView.findViewById(R.id.textViewUnreadCount);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onChatClick(chats.get(position));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onChatLongClick(chats.get(position));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(Chat chat) {
            // Set chat name/title
            String displayName = chat.getDisplayName(currentUserId);
            textViewName.setText(displayName);
            
            // Set profile image
            String displayImage = chat.getDisplayImage(currentUserId);
            if (displayImage != null && !displayImage.isEmpty()) {
                Glide.with(context)
                        .load(displayImage)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(imageViewProfile);
            } else {
                imageViewProfile.setImageResource(chat.isGroupChat() ? 
                        R.drawable.ic_group : R.drawable.ic_person);
            }
            
            // Set last message
            String lastMessageText = chat.getLastMessageDisplayText();
            textViewLastMessage.setText(lastMessageText);
            
            // Set timestamp
            String timeAgo = chat.getLastMessageTimeAgo();
            textViewTime.setText(timeAgo);
            
            // Set unread count
            int unreadCount = chat.getUnreadCount(currentUserId);
            if (unreadCount > 0) {
                textViewUnreadCount.setVisibility(View.VISIBLE);
                viewUnreadIndicator.setVisibility(View.VISIBLE);
                
                if (unreadCount > 99) {
                    textViewUnreadCount.setText("99+");
                } else {
                    textViewUnreadCount.setText(String.valueOf(unreadCount));
                }
                
                // Bold text for unread chats
                textViewName.setTypeface(null, android.graphics.Typeface.BOLD);
                textViewLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                textViewUnreadCount.setVisibility(View.GONE);
                viewUnreadIndicator.setVisibility(View.GONE);
                
                // Normal text for read chats
                textViewName.setTypeface(null, android.graphics.Typeface.NORMAL);
                textViewLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
            
            // Add online indicator for direct chats
            if (chat.isDirectChat()) {
                // TODO: Add online status indicator
            }
        }
    }
}