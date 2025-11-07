package com.namatovu.alumniportal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.namatovu.alumniportal.R;
import com.namatovu.alumniportal.models.ChatMessage;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;
    
    private Context context;
    private List<ChatMessage> messages;
    private String currentUserId;
    private OnMessageClickListener listener;
    
    public interface OnMessageClickListener {
        void onMessageClick(ChatMessage message);
        void onMessageLongClick(ChatMessage message);
    }
    
    public ChatMessageAdapter(Context context, List<ChatMessage> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }
    
    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        
        if ("system".equals(message.getMessageType())) {
            return VIEW_TYPE_SYSTEM;
        } else if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        
        switch (viewType) {
            case VIEW_TYPE_SENT:
                View sentView = inflater.inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(sentView);
            case VIEW_TYPE_RECEIVED:
                View receivedView = inflater.inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(receivedView);
            case VIEW_TYPE_SYSTEM:
                View systemView = inflater.inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(systemView);
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    // Sent message view holder
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout messageContainer;
        private TextView textViewMessage;
        private ImageView imageViewMessage;
        private TextView textViewFileName;
        private TextView textViewFileSize;
        private TextView textViewTime;
        private TextView textViewStatus;
        private ImageView imageViewStatus;
        private View layoutFile;
        
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            imageViewMessage = itemView.findViewById(R.id.imageViewMessage);
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            textViewFileSize = itemView.findViewById(R.id.textViewFileSize);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            imageViewStatus = itemView.findViewById(R.id.imageViewStatus);
            layoutFile = itemView.findViewById(R.id.layoutFile);
            
            setupClickListeners();
        }
        
        private void setupClickListeners() {
            messageContainer.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageClick(messages.get(position));
                    }
                }
            });
            
            messageContainer.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageLongClick(messages.get(position));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(ChatMessage message) {
            // Hide all content types first
            textViewMessage.setVisibility(View.GONE);
            imageViewMessage.setVisibility(View.GONE);
            layoutFile.setVisibility(View.GONE);
            
            String messageType = message.getMessageType();
            
            switch (messageType) {
                case "text":
                    textViewMessage.setVisibility(View.VISIBLE);
                    textViewMessage.setText(message.getMessageText());
                    break;
                    
                case "image":
                    imageViewMessage.setVisibility(View.VISIBLE);
                    if (message.getFileUrl() != null) {
                        Glide.with(context)
                                .load(message.getFileUrl())
                                .placeholder(R.drawable.ic_image)
                                .into(imageViewMessage);
                    }
                    break;
                    
                case "file":
                    layoutFile.setVisibility(View.VISIBLE);
                    textViewFileName.setText(message.getFileName());
                    textViewFileSize.setText(message.getFileSizeFormatted());
                    break;
                    
                case "location":
                    textViewMessage.setVisibility(View.VISIBLE);
                    textViewMessage.setText("📍 " + message.getMessageText());
                    break;
            }
            
            // Set timestamp
            textViewTime.setText(message.getFormattedTime());
            
            // Set message status
            setMessageStatus(message);
        }
        
        private void setMessageStatus(ChatMessage message) {
            if (message.isRead()) {
                imageViewStatus.setImageResource(R.drawable.ic_done_all);
                imageViewStatus.setColorFilter(context.getColor(R.color.colorAccent));
                textViewStatus.setText("Read");
            } else if (message.isDelivered()) {
                imageViewStatus.setImageResource(R.drawable.ic_done_all);
                textViewStatus.setText("Delivered");
            } else {
                imageViewStatus.setImageResource(R.drawable.ic_check);
                textViewStatus.setText("Sent");
            }
        }
    }
    
    // Received message view holder
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout messageContainer;
        private TextView textViewMessage;
        private ImageView imageViewMessage;
        private TextView textViewFileName;
        private TextView textViewFileSize;
        private TextView textViewTime;
        private TextView textViewSenderName;
        private View layoutFile;
        
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            imageViewMessage = itemView.findViewById(R.id.imageViewMessage);
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            textViewFileSize = itemView.findViewById(R.id.textViewFileSize);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            layoutFile = itemView.findViewById(R.id.layoutFile);
            
            setupClickListeners();
        }
        
        private void setupClickListeners() {
            messageContainer.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageClick(messages.get(position));
                    }
                }
            });
            
            messageContainer.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMessageLongClick(messages.get(position));
                        return true;
                    }
                }
                return false;
            });
        }
        
        public void bind(ChatMessage message) {
            // Hide all content types first
            textViewMessage.setVisibility(View.GONE);
            imageViewMessage.setVisibility(View.GONE);
            layoutFile.setVisibility(View.GONE);
            
            String messageType = message.getMessageType();
            
            switch (messageType) {
                case "text":
                    textViewMessage.setVisibility(View.VISIBLE);
                    textViewMessage.setText(message.getMessageText());
                    break;
                    
                case "image":
                    imageViewMessage.setVisibility(View.VISIBLE);
                    if (message.getFileUrl() != null) {
                        Glide.with(context)
                                .load(message.getFileUrl())
                                .placeholder(R.drawable.ic_image)
                                .into(imageViewMessage);
                    }
                    break;
                    
                case "file":
                    layoutFile.setVisibility(View.VISIBLE);
                    textViewFileName.setText(message.getFileName());
                    textViewFileSize.setText(message.getFileSizeFormatted());
                    break;
                    
                case "location":
                    textViewMessage.setVisibility(View.VISIBLE);
                    textViewMessage.setText("📍 " + message.getMessageText());
                    break;
            }
            
            // Set timestamp
            textViewTime.setText(message.getFormattedTime());
            
            // Set sender name (for group chats)
            if (message.getSenderName() != null) {
                textViewSenderName.setVisibility(View.VISIBLE);
                textViewSenderName.setText(message.getSenderName());
            } else {
                textViewSenderName.setVisibility(View.GONE);
            }
        }
    }
    
    // System message view holder
    class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSystemMessage;
        private TextView textViewTime;
        
        public SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSystemMessage = itemView.findViewById(R.id.textViewSystemMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
        
        public void bind(ChatMessage message) {
            textViewSystemMessage.setText(message.getMessageText());
            textViewTime.setText(message.getFormattedTime());
        }
    }
}