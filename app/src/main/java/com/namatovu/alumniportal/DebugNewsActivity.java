package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Debug activity to check what news is in Firestore
 */
public class DebugNewsActivity extends AppCompatActivity {
    
    private TextView debugText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_news);
        
        debugText = findViewById(R.id.debugText);
        checkFirestoreNews();
    }
    
    private void checkFirestoreNews() {
        StringBuilder result = new StringBuilder();
        result.append("Checking Firestore 'news' collection...\n\n");
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Check news collection
        db.collection("news")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int count = queryDocumentSnapshots.size();
                result.append("Total news items found: ").append(count).append("\n\n");
                
                if (count == 0) {
                    result.append("❌ No news items in database!\n");
                    result.append("You need to add news to Firestore first.\n\n");
                    result.append("To add news:\n");
                    result.append("1. Go to Firestore Console\n");
                    result.append("2. Create 'news' collection\n");
                    result.append("3. Add documents with:\n");
                    result.append("   - title (string)\n");
                    result.append("   - summary (string)\n");
                    result.append("   - publishedAt (timestamp)\n");
                    result.append("   - category (string)\n");
                    result.append("   - author (string)\n");
                } else {
                    result.append("✅ Found ").append(count).append(" news items:\n\n");
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        Object publishedAt = doc.get("publishedAt");
                        
                        result.append("📰 ").append(title != null ? title : "No title").append("\n");
                        result.append("   Author: ").append(author != null ? author : "Unknown").append("\n");
                        result.append("   Published: ").append(publishedAt != null ? publishedAt.toString() : "No date").append("\n");
                        result.append("   ID: ").append(doc.getId()).append("\n\n");
                    }
                }
                
                debugText.setText(result.toString());
            })
            .addOnFailureListener(e -> {
                result.append("❌ Error reading Firestore:\n");
                result.append(e.getMessage());
                debugText.setText(result.toString());
            });
    }
}
