package com.namatovu.alumniportal.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.namatovu.alumniportal.models.User;
import com.namatovu.alumniportal.models.JobPosting;
import com.namatovu.alumniportal.models.AlumniEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced search and filtering system for alumni, jobs, events, and content
 */
public class SearchAndFilterManager {
    private static final String TAG = "SearchFilterManager";
    private static final String PREFS_NAME = "search_preferences";
    private static final String RECENT_SEARCHES_KEY = "recent_searches";
    private static final String SAVED_SEARCHES_KEY = "saved_searches";
    private static final int MAX_RECENT_SEARCHES = 10;
    
    private static SearchAndFilterManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final FirebaseFirestore db;
    
    // Search types
    public enum SearchType {
        ALUMNI,
        JOBS,
        EVENTS,
        NEWS,
        ALL
    }
    
    // Sort options
    public enum SortOption {
        RELEVANCE,
        ALPHABETICAL,
        DATE_NEWEST,
        DATE_OLDEST,
        GRADUATION_YEAR,
        LOCATION,
        COMPANY
    }
    
    private SearchAndFilterManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.db = FirebaseFirestore.getInstance();
    }
    
    public static synchronized SearchAndFilterManager getInstance(Context context) {
        if (instance == null) {
            instance = new SearchAndFilterManager(context);
        }
        return instance;
    }
    
    /**
     * Search filter configuration
     */
    public static class SearchFilter {
        public String query;
        public SearchType type = SearchType.ALL;
        public SortOption sortBy = SortOption.RELEVANCE;
        public Map<String, Object> filters = new HashMap<>();
        public int limit = 50;
        public int offset = 0;
        
        // Alumni-specific filters
        public String graduationYearFrom;
        public String graduationYearTo;
        public String major;
        public String location;
        public String company;
        public String jobTitle;
        public List<String> skills = new ArrayList<>();
        public Boolean availableForMentoring;
        public Boolean isVerified;
        
        // Job-specific filters
        public String jobType; // "full-time", "part-time", "contract", "internship"
        public String experience; // "entry", "mid", "senior", "executive"
        public String salaryMin;
        public String salaryMax;
        public String industry;
        public Boolean remoteWork;
        
        // Event-specific filters
        public String eventType; // "networking", "professional", "social", "educational"
        public String dateFrom;
        public String dateTo;
        public Boolean isVirtual;
        public String category;
        
        public SearchFilter() {}
        
        public SearchFilter(String query) {
            this.query = query;
        }
        
        public SearchFilter(String query, SearchType type) {
            this.query = query;
            this.type = type;
        }
    }
    
    /**
     * Search result wrapper
     */
    public static class SearchResult<T> {
        public List<T> results;
        public int totalCount;
        public boolean hasMore;
        public String query;
        public SearchType type;
        public long searchTime;
        
        public SearchResult() {
            this.results = new ArrayList<>();
        }
        
        public SearchResult(List<T> results, String query, SearchType type, long searchTime) {
            this.results = results != null ? results : new ArrayList<>();
            this.query = query;
            this.type = type;
            this.searchTime = searchTime;
        }
    }
    
    /**
     * Search interface for callbacks
     */
    public interface SearchCallback<T> {
        void onSearchComplete(SearchResult<T> result);
        void onSearchError(String error);
    }
    
    /**
     * Main search method - searches across all content types
     */
    public void search(SearchFilter filter, SearchCallback<Object> callback) {
        long startTime = System.currentTimeMillis();
        
        PerformanceHelper.getInstance().startTiming("search_operation");
        
        // Validate search query
        if (TextUtils.isEmpty(filter.query) && filter.filters.isEmpty()) {
            callback.onSearchError("Search query cannot be empty");
            return;
        }
        
        // Save to recent searches
        saveRecentSearch(filter.query);
        
        // Perform search based on type
        switch (filter.type) {
            case ALUMNI:
                searchAlumni(filter, new SearchCallback<User>() {
                    @Override
                    public void onSearchComplete(SearchResult<User> result) {
                        PerformanceHelper.getInstance().endTiming("search_operation");
                        callback.onSearchComplete(convertResult(result));
                    }
                    
                    @Override
                    public void onSearchError(String error) {
                        PerformanceHelper.getInstance().endTiming("search_operation");
                        callback.onSearchError(error);
                    }
                });
                break;
                
            case JOBS:
                searchJobs(filter, new SearchCallback<JobPosting>() {
                    @Override
                    public void onSearchComplete(SearchResult<JobPosting> result) {
                        PerformanceHelper.getInstance().endTiming("search_operation");
                        callback.onSearchComplete(convertResult(result));
                    }
                    
                    @Override
                    public void onSearchError(String error) {
                        PerformanceHelper.getInstance().endTiming("search_operation");
                        callback.onSearchError(error);
                    }
                });
                break;
                
            case EVENTS:
                searchEvents(filter, new SearchCallback<AlumniEvent>() {
                    @Override
                    public void onSearchComplete(SearchResult<AlumniEvent> result) {
                        PerformanceHelper.getInstance().endTiming("search_operation");
                        callback.onSearchComplete(convertResult(result));
                    }
                    
                    @Override
                    public void onSearchError(String error) {
                        PerformanceHelper.getInstance().endTiming("search_operation");
                        callback.onSearchError(error);
                    }
                });
                break;
                
            case ALL:
                searchAll(filter, callback);
                break;
                
            default:
                callback.onSearchError("Unsupported search type");
                break;
        }
        
        // Log search analytics
        AnalyticsHelper.logSearch(filter.query, filter.type.toString());
    }
    
    /**
     * Search alumni with advanced filters
     */
    public void searchAlumni(SearchFilter filter, SearchCallback<User> callback) {
        Query query = db.collection("users");
        
        // Apply privacy filter - only show users who allow alumni search
        query = query.whereEqualTo("privacySettings.allow_alumni_search", true);
        
        // Apply text search (simplified - in production, use Algolia or similar)
        if (!TextUtils.isEmpty(filter.query)) {
            String lowercaseQuery = filter.query.toLowerCase();
            query = query.whereGreaterThanOrEqualTo("searchKeywords", lowercaseQuery)
                        .whereLessThanOrEqualTo("searchKeywords", lowercaseQuery + "\uf8ff");
        }
        
        // Apply graduation year filter
        if (!TextUtils.isEmpty(filter.graduationYearFrom)) {
            query = query.whereGreaterThanOrEqualTo("graduationYear", Integer.parseInt(filter.graduationYearFrom));
        }
        if (!TextUtils.isEmpty(filter.graduationYearTo)) {
            query = query.whereLessThanOrEqualTo("graduationYear", Integer.parseInt(filter.graduationYearTo));
        }
        
        // Apply major filter
        if (!TextUtils.isEmpty(filter.major)) {
            query = query.whereEqualTo("major", filter.major);
        }
        
        // Apply location filter
        if (!TextUtils.isEmpty(filter.location)) {
            query = query.whereEqualTo("location", filter.location);
        }
        
        // Apply company filter
        if (!TextUtils.isEmpty(filter.company)) {
            query = query.whereEqualTo("company", filter.company);
        }
        
        // Apply mentoring availability filter
        if (filter.availableForMentoring != null) {
            query = query.whereEqualTo("privacySettings.allow_mentor_requests", filter.availableForMentoring);
        }
        
        // Apply verification filter
        if (filter.isVerified != null) {
            query = query.whereEqualTo("isVerified", filter.isVerified);
        }
        
        // Apply sorting
        query = applySorting(query, filter.sortBy);
        
        // Apply limit
        query = query.limit(filter.limit);
        
        // Execute query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                List<User> users = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : snapshot) {
                    try {
                        User user = document.toObject(User.class);
                        
                        // Apply additional filtering that can't be done in Firestore
                        if (matchesAdvancedFilters(user, filter)) {
                            users.add(user);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing user document: " + document.getId(), e);
                    }
                }
                
                // Apply client-side sorting if needed
                if (filter.sortBy != SortOption.RELEVANCE) {
                    users = sortAlumniResults(users, filter.sortBy);
                }
                
                long searchTime = System.currentTimeMillis();
                SearchResult<User> result = new SearchResult<>(users, filter.query, SearchType.ALUMNI, searchTime);
                result.totalCount = users.size();
                result.hasMore = snapshot.size() == filter.limit;
                
                callback.onSearchComplete(result);
                
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Search failed";
                Log.e(TAG, "Alumni search failed", task.getException());
                ErrorHandler.getInstance(context).handleError(task.getException(), "search_alumni");
                callback.onSearchError(error);
            }
        });
    }
    
    /**
     * Search job postings with filters
     */
    public void searchJobs(SearchFilter filter, SearchCallback<JobPosting> callback) {
        Query query = db.collection("jobPostings");
        
        // Apply active status filter
        query = query.whereEqualTo("isActive", true);
        
        // Apply text search
        if (!TextUtils.isEmpty(filter.query)) {
            String lowercaseQuery = filter.query.toLowerCase();
            query = query.whereArrayContains("searchKeywords", lowercaseQuery);
        }
        
        // Apply job type filter
        if (!TextUtils.isEmpty(filter.jobType)) {
            query = query.whereEqualTo("jobType", filter.jobType);
        }
        
        // Apply experience level filter
        if (!TextUtils.isEmpty(filter.experience)) {
            query = query.whereEqualTo("experienceLevel", filter.experience);
        }
        
        // Apply location filter
        if (!TextUtils.isEmpty(filter.location)) {
            query = query.whereEqualTo("location", filter.location);
        }
        
        // Apply company filter
        if (!TextUtils.isEmpty(filter.company)) {
            query = query.whereEqualTo("company", filter.company);
        }
        
        // Apply remote work filter
        if (filter.remoteWork != null) {
            query = query.whereEqualTo("remoteWork", filter.remoteWork);
        }
        
        // Apply industry filter
        if (!TextUtils.isEmpty(filter.industry)) {
            query = query.whereEqualTo("industry", filter.industry);
        }
        
        // Apply sorting
        query = applySorting(query, filter.sortBy);
        
        // Apply limit
        query = query.limit(filter.limit);
        
        // Execute query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                List<JobPosting> jobs = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : snapshot) {
                    try {
                        JobPosting job = document.toObject(JobPosting.class);
                        
                        // Apply salary filter (client-side due to Firestore limitations)
                        if (matchesSalaryRange(job, filter.salaryMin, filter.salaryMax)) {
                            jobs.add(job);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing job document: " + document.getId(), e);
                    }
                }
                
                long searchTime = System.currentTimeMillis();
                SearchResult<JobPosting> result = new SearchResult<>(jobs, filter.query, SearchType.JOBS, searchTime);
                result.totalCount = jobs.size();
                result.hasMore = snapshot.size() == filter.limit;
                
                callback.onSearchComplete(result);
                
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Job search failed";
                Log.e(TAG, "Job search failed", task.getException());
                ErrorHandler.getInstance(context).handleError(task.getException(), "search_jobs");
                callback.onSearchError(error);
            }
        });
    }
    
    /**
     * Search events with filters
     */
    public void searchEvents(SearchFilter filter, SearchCallback<AlumniEvent> callback) {
        Query query = db.collection("events");
        
        // Apply text search
        if (!TextUtils.isEmpty(filter.query)) {
            String lowercaseQuery = filter.query.toLowerCase();
            query = query.whereArrayContains("searchKeywords", lowercaseQuery);
        }
        
        // Apply event type filter
        if (!TextUtils.isEmpty(filter.eventType)) {
            query = query.whereEqualTo("eventType", filter.eventType);
        }
        
        // Apply virtual event filter
        if (filter.isVirtual != null) {
            query = query.whereEqualTo("isVirtual", filter.isVirtual);
        }
        
        // Apply category filter
        if (!TextUtils.isEmpty(filter.category)) {
            query = query.whereEqualTo("category", filter.category);
        }
        
        // Apply date range filter
        if (!TextUtils.isEmpty(filter.dateFrom)) {
            // Convert date string to timestamp
            query = query.whereGreaterThanOrEqualTo("eventDate", filter.dateFrom);
        }
        if (!TextUtils.isEmpty(filter.dateTo)) {
            query = query.whereLessThanOrEqualTo("eventDate", filter.dateTo);
        }
        
        // Apply location filter
        if (!TextUtils.isEmpty(filter.location)) {
            query = query.whereEqualTo("location", filter.location);
        }
        
        // Apply sorting
        query = applySorting(query, filter.sortBy);
        
        // Apply limit
        query = query.limit(filter.limit);
        
        // Execute query
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                List<AlumniEvent> events = new ArrayList<>();
                
                for (QueryDocumentSnapshot document : snapshot) {
                    try {
                        AlumniEvent event = document.toObject(AlumniEvent.class);
                        events.add(event);
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing event document: " + document.getId(), e);
                    }
                }
                
                long searchTime = System.currentTimeMillis();
                SearchResult<AlumniEvent> result = new SearchResult<>(events, filter.query, SearchType.EVENTS, searchTime);
                result.totalCount = events.size();
                result.hasMore = snapshot.size() == filter.limit;
                
                callback.onSearchComplete(result);
                
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Event search failed";
                Log.e(TAG, "Event search failed", task.getException());
                ErrorHandler.getInstance(context).handleError(task.getException(), "search_events");
                callback.onSearchError(error);
            }
        });
    }
    /**
     * Search across all content types
     */
    private void searchAll(SearchFilter filter, SearchCallback<Object> callback) {
        List<Object> allResults = new ArrayList<>();
        AtomicInteger completedSearches = new AtomicInteger(0);
        final int totalSearches = 3;

        // Search alumni
        SearchFilter alumniFilter = new SearchFilter(filter.query);
        alumniFilter.type = SearchType.ALUMNI;
        alumniFilter.limit = 20;

        searchAlumni(alumniFilter, new AggregatingCallback<User>(allResults, completedSearches, totalSearches, callback, filter.query));

        // Search jobs
        SearchFilter jobFilter = new SearchFilter(filter.query);
        jobFilter.type = SearchType.JOBS;
        jobFilter.limit = 15;

        searchJobs(jobFilter, new AggregatingCallback<JobPosting>(allResults, completedSearches, totalSearches, callback, filter.query));

        // Search events
        SearchFilter eventFilter = new SearchFilter(filter.query);
        eventFilter.type = SearchType.EVENTS;
        eventFilter.limit = 15;

        searchEvents(eventFilter, new AggregatingCallback<AlumniEvent>(allResults, completedSearches, totalSearches, callback, filter.query));
    }

    /**
     * Aggregating callback used to collect results from multiple searches and invoke the final callback once complete.
     */
    private static class AggregatingCallback<T> implements SearchCallback<T> {
        private final List<Object> allResults;
        private final AtomicInteger completed;
        private final int total;
        private final SearchCallback<Object> finalCallback;
        private final String query;

        AggregatingCallback(List<Object> allResults, AtomicInteger completed, int total, SearchCallback<Object> finalCallback, String query) {
            this.allResults = allResults;
            this.completed = completed;
            this.total = total;
            this.finalCallback = finalCallback;
            this.query = query;
        }

        @Override
        public void onSearchComplete(SearchResult<T> result) {
            synchronized (allResults) {
                allResults.addAll(result.results);
                if (completed.incrementAndGet() == total) {
                    long searchTime = System.currentTimeMillis();
                    SearchResult<Object> res = new SearchResult<>(allResults, query, SearchType.ALL, searchTime);
                    res.totalCount = allResults.size();
                    finalCallback.onSearchComplete(res);
                }
            }
        }

        @Override
        public void onSearchError(String error) {
            synchronized (allResults) {
                if (completed.incrementAndGet() == total) {
                    long searchTime = System.currentTimeMillis();
                    SearchResult<Object> res = new SearchResult<>(allResults, query, SearchType.ALL, searchTime);
                    res.totalCount = allResults.size();
                    finalCallback.onSearchComplete(res);
                }
            }
        }
    }
    }
    
    // Helper methods for filtering and sorting
    private Query applySorting(Query query, SortOption sortBy) {
        switch (sortBy) {
            case ALPHABETICAL:
                return query.orderBy("fullName");
            case DATE_NEWEST:
                return query.orderBy("createdAt", Query.Direction.DESCENDING);
            case DATE_OLDEST:
                return query.orderBy("createdAt");
            case GRADUATION_YEAR:
                return query.orderBy("graduationYear");
            case LOCATION:
                return query.orderBy("location");
            case COMPANY:
                return query.orderBy("company");
            default:
                return query; // No sorting for relevance (handled by search algorithm)
        }
    }
    
    private boolean matchesAdvancedFilters(User user, SearchFilter filter) {
        // Skills filter
        if (!filter.skills.isEmpty()) {
            if (user.getSkills() == null || user.getSkills().isEmpty()) {
                return false;
            }
            
            boolean hasMatchingSkill = false;
            for (String skill : filter.skills) {
                if (user.getSkills().contains(skill)) {
                    hasMatchingSkill = true;
                    break;
                }
            }
            if (!hasMatchingSkill) {
                return false;
            }
        }
        
        // Job title filter
        if (!TextUtils.isEmpty(filter.jobTitle)) {
            if (user.getCurrentJob() == null || 
                !user.getCurrentJob().toLowerCase().contains(filter.jobTitle.toLowerCase())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean matchesSalaryRange(JobPosting job, String salaryMin, String salaryMax) {
        if (TextUtils.isEmpty(salaryMin) && TextUtils.isEmpty(salaryMax)) {
            return true;
        }
        
        if (job.getSalaryMin() == null || job.getSalaryMax() == null) {
            return true; // Include jobs without salary info
        }
        
        try {
            if (!TextUtils.isEmpty(salaryMin)) {
                int minSalary = Integer.parseInt(salaryMin);
                if (job.getSalaryMax() < minSalary) {
                    return false;
                }
            }
            
            if (!TextUtils.isEmpty(salaryMax)) {
                int maxSalary = Integer.parseInt(salaryMax);
                if (job.getSalaryMin() > maxSalary) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid salary range in filter", e);
        }
        
        return true;
    }
    
    private List<User> sortAlumniResults(List<User> users, SortOption sortBy) {
        switch (sortBy) {
            case ALPHABETICAL:
                users.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
                break;
            case GRADUATION_YEAR:
                users.sort((a, b) -> Integer.compare(
                    a.getGraduationYear() != null ? a.getGraduationYear() : 0,
                    b.getGraduationYear() != null ? b.getGraduationYear() : 0
                ));
                break;
            case LOCATION:
                users.sort((a, b) -> {
                    String locationA = a.getLocation() != null ? a.getLocation() : "";
                    String locationB = b.getLocation() != null ? b.getLocation() : "";
                    return locationA.compareToIgnoreCase(locationB);
                });
                break;
            case COMPANY:
                users.sort((a, b) -> {
                    String companyA = a.getCompany() != null ? a.getCompany() : "";
                    String companyB = b.getCompany() != null ? b.getCompany() : "";
                    return companyA.compareToIgnoreCase(companyB);
                });
                break;
        }
        return users;
    }
    
    @SuppressWarnings("unchecked")
    private SearchResult<Object> convertResult(SearchResult<?> result) {
        return (SearchResult<Object>) result;
    }
    
    // Recent searches management
    public void saveRecentSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        
        Set<String> recentSearches = prefs.getStringSet(RECENT_SEARCHES_KEY, new HashSet<>());
        recentSearches = new HashSet<>(recentSearches); // Make it mutable
        
        // Remove if already exists (to move to top)
        recentSearches.remove(query);
        
        // Add to beginning
        List<String> recentList = new ArrayList<>(recentSearches);
        recentList.add(0, query);
        
        // Keep only the most recent ones
        if (recentList.size() > MAX_RECENT_SEARCHES) {
            recentList = recentList.subList(0, MAX_RECENT_SEARCHES);
        }
        
        prefs.edit().putStringSet(RECENT_SEARCHES_KEY, new HashSet<>(recentList)).apply();
    }
    
    public List<String> getRecentSearches() {
        Set<String> recentSearches = prefs.getStringSet(RECENT_SEARCHES_KEY, new HashSet<>());
        return new ArrayList<>(recentSearches);
    }
    
    public void clearRecentSearches() {
        prefs.edit().remove(RECENT_SEARCHES_KEY).apply();
    }
    
    // Saved searches management
    public void saveSearch(String name, SearchFilter filter) {
        Map<String, String> savedSearches = getSavedSearchesMap();
        
        // Convert filter to JSON string (simplified)
        String filterJson = filterToJson(filter);
        savedSearches.put(name, filterJson);
        
        // Save back to preferences
        saveSavedSearchesMap(savedSearches);
    }
    
    public Map<String, SearchFilter> getSavedSearches() {
        Map<String, String> savedSearches = getSavedSearchesMap();
        Map<String, SearchFilter> result = new HashMap<>();
        
        for (Map.Entry<String, String> entry : savedSearches.entrySet()) {
            SearchFilter filter = jsonToFilter(entry.getValue());
            if (filter != null) {
                result.put(entry.getKey(), filter);
            }
        }
        
        return result;
    }
    
    public void deleteSavedSearch(String name) {
        Map<String, String> savedSearches = getSavedSearchesMap();
        savedSearches.remove(name);
        saveSavedSearchesMap(savedSearches);
    }
    
    private Map<String, String> getSavedSearchesMap() {
        // This would use JSON serialization in a real implementation
        return new HashMap<>();
    }
    
    private void saveSavedSearchesMap(Map<String, String> searches) {
        // This would use JSON serialization in a real implementation
    }
    
    private String filterToJson(SearchFilter filter) {
        // This would use proper JSON serialization
        return filter.query + "|" + filter.type.toString();
    }
    
    private SearchFilter jsonToFilter(String json) {
        // This would use proper JSON deserialization
        String[] parts = json.split("\\|");
        if (parts.length >= 2) {
            SearchFilter filter = new SearchFilter(parts[0]);
            filter.type = SearchType.valueOf(parts[1]);
            return filter;
        }
        return null;
    }
    
    /**
     * Get search suggestions based on query
     */
    public List<String> getSearchSuggestions(String query, SearchType type) {
        List<String> suggestions = new ArrayList<>();
        
        // Add recent searches that match
        for (String recent : getRecentSearches()) {
            if (recent.toLowerCase().contains(query.toLowerCase())) {
                suggestions.add(recent);
            }
        }
        
        // Add popular searches (this would come from analytics)
        suggestions.addAll(getPopularSearchTerms(type));
        
        // Remove duplicates and limit results
        Set<String> uniqueSuggestions = new HashSet<>(suggestions);
        List<String> result = new ArrayList<>(uniqueSuggestions);
        
        return result.size() > 10 ? result.subList(0, 10) : result;
    }
    
    private List<String> getPopularSearchTerms(SearchType type) {
        // This would come from analytics in a real implementation
        List<String> popular = new ArrayList<>();
        
        switch (type) {
            case ALUMNI:
                popular.addAll(Arrays.asList("Software Engineer", "Product Manager", "Data Scientist", "Consultant"));
                break;
            case JOBS:
                popular.addAll(Arrays.asList("Remote", "Full-time", "Entry Level", "Senior"));
                break;
            case EVENTS:
                popular.addAll(Arrays.asList("Networking", "Career Fair", "Alumni Mixer", "Tech Talk"));
                break;
        }
        
        return popular;
    }
}