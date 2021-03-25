package com.gitee.kooder.gitea;

/**
 * @author zhanggx
 */
public class Organization {

    private Integer id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String description;
    private String website;
    private String location;
    private String visibility;
    private Boolean repoAdminChangeTeamAccess;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Boolean isRepoAdminChangeTeamAccess() {
        return repoAdminChangeTeamAccess;
    }

    public void setRepoAdminChangeTeamAccess(Boolean repoAdminChangeTeamAccess) {
        this.repoAdminChangeTeamAccess = repoAdminChangeTeamAccess;
    }
}
