package com.namatovu.alumniportal.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Service for sending emails using Gmail API with OAuth2 authentication
 */
public class GmailService {
    private static final String TAG = "GmailService";
    private static final String PREF_NAME = "gmail_auth";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String APPLICATION_NAME = "Alumni Portal";
    
    private Context context;
    private Gmail gmailService;
    private Executor executor;
    private SharedPreferences prefs;
    
    public GmailService(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Send an email using Gmail API
     */
    public Task<Boolean> sendEmail(String toEmail, String subject, String body) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        
        executor.execute(() -> {
            try {
                if (gmailService == null) {
                    initializeGmailService();
                }
                
                if (gmailService != null) {
                    MimeMessage mimeMessage = createEmail(toEmail, subject, body);
                    Message message = createMessageWithEmail(mimeMessage);
                    
                    gmailService.users().messages().send("me", message).execute();
                    Log.d(TAG, "Email sent successfully to: " + toEmail);
                    taskCompletionSource.setResult(true);
                } else {
                    Log.e(TAG, "Gmail service not initialized");
                    taskCompletionSource.setException(new Exception("Gmail service not initialized"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
                taskCompletionSource.setException(e);
            }
        });
        
        return taskCompletionSource.getTask();
    }
    
    /**
     * Initialize Gmail service with OAuth2 authentication
     */
    private void initializeGmailService() throws Exception {
        String refreshToken = prefs.getString(PREF_REFRESH_TOKEN, null);
        
        if (refreshToken == null) {
            // First time setup - need to get refresh token
            Log.d(TAG, "No refresh token found. Need to authenticate first.");
            throw new Exception("Authentication required. Please run initial setup.");
        }
        
        // Load credentials from assets
        JSONObject credentials = loadCredentials();
        String clientId = credentials.getJSONObject("web").getString("client_id");
        String clientSecret = credentials.getJSONObject("web").getString("client_secret");
        
        // Create GoogleCredential with refresh token
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(new NetHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(refreshToken);
        
        // Refresh the access token
        credential.refreshToken();
        
        // Build Gmail service
        gmailService = new Gmail.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        Log.d(TAG, "Gmail service initialized successfully");
    }
    
    /**
     * Initial authentication setup (call this once to get refresh token)
     */
    public Task<String> performInitialAuthentication() {
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        
        executor.execute(() -> {
            try {
                JSONObject credentials = loadCredentials();
                String clientId = credentials.getJSONObject("web").getString("client_id");
                String clientSecret = credentials.getJSONObject("web").getString("client_secret");
                
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        clientId,
                        clientSecret,
                        Arrays.asList(GmailScopes.GMAIL_SEND))
                        .setAccessType("offline")
                        .build();
                
                // Generate authorization URL
                String authUrl = flow.newAuthorizationUrl()
                        .setRedirectUri("urn:ietf:wg:oauth:2.0:oob")
                        .build();
                
                Log.d(TAG, "Authorization URL: " + authUrl);
                taskCompletionSource.setResult(authUrl);
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to generate auth URL", e);
                taskCompletionSource.setException(e);
            }
        });
        
        return taskCompletionSource.getTask();
    }
    
    /**
     * Complete authentication with authorization code
     */
    public Task<Boolean> completeAuthentication(String authorizationCode) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        
        executor.execute(() -> {
            try {
                JSONObject credentials = loadCredentials();
                String clientId = credentials.getJSONObject("web").getString("client_id");
                String clientSecret = credentials.getJSONObject("web").getString("client_secret");
                
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        clientId,
                        clientSecret,
                        Arrays.asList(GmailScopes.GMAIL_SEND))
                        .setAccessType("offline")
                        .build();
                
                GoogleTokenResponse response = flow.newTokenRequest(authorizationCode)
                        .setRedirectUri("urn:ietf:wg:oauth:2.0:oob")
                        .execute();
                
                // Save refresh token
                String refreshToken = response.getRefreshToken();
                if (refreshToken != null) {
                    prefs.edit().putString(PREF_REFRESH_TOKEN, refreshToken).apply();
                    Log.d(TAG, "Refresh token saved successfully");
                    taskCompletionSource.setResult(true);
                } else {
                    Log.e(TAG, "No refresh token received");
                    taskCompletionSource.setException(new Exception("No refresh token received"));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to complete authentication", e);
                taskCompletionSource.setException(e);
            }
        });
        
        return taskCompletionSource.getTask();
    }
    
    /**
     * Check if the service is authenticated
     */
    public boolean isAuthenticated() {
        return prefs.getString(PREF_REFRESH_TOKEN, null) != null;
    }
    
    /**
     * Clear authentication
     */
    public void clearAuthentication() {
        prefs.edit().clear().apply();
        gmailService = null;
        Log.d(TAG, "Authentication cleared");
    }
    
    private JSONObject loadCredentials() throws Exception {
        InputStream inputStream = context.getAssets().open("gmail_credentials.json");
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();
        
        String credentialsString = new String(buffer, "UTF-8");
        return new JSONObject(credentialsString);
    }
    
    private MimeMessage createEmail(String to, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("noreply@alumniportal.com"));
        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        
        return email;
    }
    
    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE);
        
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}