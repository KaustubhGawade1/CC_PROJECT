package com.dbms.databasemanagementsystem.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class assetPermission {


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public asset getAsset() {
        return Asset;
    }

    public void setAsset(asset asset) {
        Asset = asset;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }



    @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private Users user;

        @ManyToOne
        private asset Asset;

        @Enumerated(EnumType.STRING)
        private PermissionType permissionType;

        private LocalDateTime createdAt = LocalDateTime.now();


    }


