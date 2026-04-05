package com.dbms.databasemanagementsystem.payload;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetDto {
    private String filename;
    private String url;
    private String type; // video, doc, etc.
    private LocalDateTime uploadedAt;
}
