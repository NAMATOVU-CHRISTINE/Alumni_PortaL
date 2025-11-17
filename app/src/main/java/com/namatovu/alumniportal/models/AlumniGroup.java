package com.namatovu.alumniportal.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for Alumni Groups (e.g., Class of 2025, Department groups)
 */
public class AlumniGroup {
    private String groupId;
    private String groupName;
    private String description;
    private String groupType; // "class", "department", "interest", "location"
    private String imageUrl;
    private String creatorId;
    private String creatorName;
    private List<String> memberIds;
    private List<String> adminIds;
    private int memberCount;
    private long createdAt;
    private long lastActivityAt;
    private boolean isPrivate;
    
    // For class groups
    private String graduationYear;
    
    // For department groups
    private String department;
    
    public AlumniGroup() {
        this.memberIds = new ArrayList<>();
        this.adminIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastActivityAt = System.currentTimeMillis();
        this.isPrivate = false;
    }
    
    public AlumniGroup(String groupName, String groupType, String creatorId, String creatorName) {
        this();
        this.groupName = groupName;
        this.groupType = groupType;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.adminIds.add(creatorId);
        this.memberIds.add(creatorId);
        this.memberCount = 1;
    }
    
    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getGroupType() { return groupType; }
    public void setGroupType(String groupType) { this.groupType = groupType; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    
    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }
    
    public List<String> getAdminIds() { return adminIds; }
    public void setAdminIds(List<String> adminIds) { this.adminIds = adminIds; }
    
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(long lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
    
    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    // Helper methods
    public boolean isMember(String userId) {
        return memberIds != null && memberIds.contains(userId);
    }
    
    public boolean isAdmin(String userId) {
        return adminIds != null && adminIds.contains(userId);
    }
    
    public void addMember(String userId) {
        if (memberIds == null) {
            memberIds = new ArrayList<>();
        }
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
            memberCount = memberIds.size();
        }
    }
    
    public void removeMember(String userId) {
        if (memberIds != null) {
            memberIds.remove(userId);
            memberCount = memberIds.size();
        }
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("groupName", groupName);
        map.put("description", description);
        map.put("groupType", groupType);
        map.put("imageUrl", imageUrl);
        map.put("creatorId", creatorId);
        map.put("creatorName", creatorName);
        map.put("memberIds", memberIds);
        map.put("adminIds", adminIds);
        map.put("memberCount", memberCount);
        map.put("createdAt", createdAt);
        map.put("lastActivityAt", lastActivityAt);
        map.put("isPrivate", isPrivate);
        map.put("graduationYear", graduationYear);
        map.put("department", department);
        return map;
    }
}
