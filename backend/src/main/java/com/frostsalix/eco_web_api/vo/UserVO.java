package com.frostsalix.eco_web_api.vo;

public class UserVO {

    private Long id;
    private String username;
    private String role;

    public UserVO() {
    }

    public UserVO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}