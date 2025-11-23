package com.namatovu.alumniportal.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.namatovu.alumniportal.utils.NotificationHelper;

/**
 * BroadcastReceiver for handling incoming messages
 */
public class MessageBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MessageBroadcastReceiver";
    
    public static final String ACTION_MESSAGE_RECEIVED = "com.namatovu.alumniportal.MESSAGE_RECEIVED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            return;
        }
        
        Log.d(TAG, "Message broadcast received");
        
        String senderName = intent.getStringExtra("senderName");
        String messageText = intent.getStringExtra("messageText");
        String chatId = intent.getStringExtra("chatId");
        String senderId = intent.getStringExtra("senderId");
        
        if (senderName == null || messageText == null || chatId == null) {
            Log.w(TAG, "Missing required message data");
            return;
        }
        
        // Show notification
        NotificationHelper.showNotification(
            context,
            "New Message",
            senderName + ": " + messageText,
            chatId,
            "message"
        );
        
        Log.d(TAG, "Notification shown for message from " + senderName);
    }
}
