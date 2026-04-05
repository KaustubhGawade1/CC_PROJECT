package com.dbms.databasemanagementsystem.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

@Entity
    public class asset {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long asset_id;

    public asset() {

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    @Getter
    @Setter
        private String filename;


   // video, doc, etc.

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    private LocalDateTime uploadedAt;

    public asset( String filename, Users uploadedBy) {
       this.uploadedBy = uploadedBy;

        this.filename = filename;

      //  this.asset_id = asset_id;
    }

    @Getter
      @Setter
      @ManyToOne
      @JoinColumn(name = "uploaded_by_user")
       private Users uploadedBy;


    public Long getAsset_id() {
        return asset_id;
    }

    public void setAsset_id(Long asset_id) {
        this.asset_id = asset_id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Users getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Users uploadedBy) {
        this.uploadedBy = uploadedBy;
    }



    public asset(String filename, String contentType, Long size, String filePath) {
        this.filename = filename;

        this.contentType = contentType;
        this.size = size;
        this.filePath = filePath;
    }

    private String contentType;
    private Long size;
    private String filePath;

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    private boolean isPublic;


}


