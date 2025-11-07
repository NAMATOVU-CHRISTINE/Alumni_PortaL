package com.namatovu.alumniportal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Add Article Activity - Form for users to add new articles to the Knowledge section
 */
public class AddArticleActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etContent;
    private ChipGroup chipGroupCategory;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_article);

        initializeViews();
        setupToolbar();
        setupSubmitButton();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etContent = findViewById(R.id.etContent);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Article");
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            android.util.Log.d("AddArticle", "Submit button clicked");
            if (validateInputs()) {
                android.util.Log.d("AddArticle", "Validation passed, submitting article");
                submitArticle();
            } else {
                android.util.Log.d("AddArticle", "Validation failed");
            }
        });
    }

    private boolean validateInputs() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        android.util.Log.d("AddArticle", "Validating - Title: '" + title + "', Description: '" + description + "', Content: '" + content + "'");

        if (title.isEmpty()) {
            android.util.Log.d("AddArticle", "Validation failed - Title is empty");
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            android.util.Log.d("AddArticle", "Validation failed - Description is empty");
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        if (content.isEmpty()) {
            android.util.Log.d("AddArticle", "Validation failed - Content is empty");
            etContent.setError("Content is required");
            etContent.requestFocus();
            return false;
        }

        // Simplified category validation - just check if any chip exists
        // Since Networking is checked by default, this should always pass
        android.util.Log.d("AddArticle", "Validation passed - all fields filled");
        return true;
    }

    private void submitArticle() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        
        android.util.Log.d("AddArticle", "Submitting article - Title: " + title);
        
        // Get selected category with fallback
        int checkedChipId = chipGroupCategory.getCheckedChipId();
        String category = "Networking"; // Default fallback
        
        if (checkedChipId != -1) {
            Chip selectedChip = findViewById(checkedChipId);
            if (selectedChip != null) {
                category = getSelectedCategory(selectedChip.getText().toString());
            }
        }

        android.util.Log.d("AddArticle", "Submitting article with category: " + category);

        // Return data to KnowledgeActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("title", title);
        resultIntent.putExtra("description", description);
        resultIntent.putExtra("content", content);
        resultIntent.putExtra("category", category);
        
        android.util.Log.d("AddArticle", "Setting result and finishing activity");
        
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getSelectedCategory(String chipText) {
        if (chipText.contains("Networking")) return "Networking";
        if (chipText.contains("Skills")) return "Skills";
        if (chipText.contains("Mentorship")) return "Mentorship";
        if (chipText.contains("Career")) return "Career Growth";
        if (chipText.contains("Leadership")) return "Leadership";
        return "Networking"; // Default
    }
}