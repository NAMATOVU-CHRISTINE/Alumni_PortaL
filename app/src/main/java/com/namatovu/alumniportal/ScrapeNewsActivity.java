package com.namatovu.alumniportal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.namatovu.alumniportal.services.MUSTNewsScraper;

/**
 * Activity to scrape news from MUST website and save to Firestore
 */
public class ScrapeNewsActivity extends AppCompatActivity {
    
    private Button scrapeButton;
    private TextView statusText;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrape_news);
        
        scrapeButton = findViewById(R.id.scrapeButton);
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);
        
        scrapeButton.setOnClickListener(v -> startScraping());
    }
    
    private void startScraping() {
        scrapeButton.setEnabled(false);
        progressBar.setVisibility(android.view.View.VISIBLE);
        statusText.setText("Scraping MUST website for news...");
        
        MUSTNewsScraper.scrapeAndSaveNews(new MUSTNewsScraper.ScraperCallback() {
            @Override
            public void onSuccess(int newsCount) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    statusText.setText("✅ Successfully scraped " + newsCount + " news items!\n\nNews has been saved to Firestore.");
                    scrapeButton.setEnabled(true);
                    Toast.makeText(ScrapeNewsActivity.this, "News scraped successfully!", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    statusText.setText("❌ Error: " + error);
                    scrapeButton.setEnabled(true);
                    Toast.makeText(ScrapeNewsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
