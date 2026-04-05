package com.dbms.databasemanagementsystem.repository;

import com.dbms.databasemanagementsystem.model.Users;
import com.dbms.databasemanagementsystem.model.asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<asset,Long> {


        // Full-text search by title, contentType, or uploader username
        @Query("SELECT a FROM asset a WHERE " +
            //    "LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                "LOWER(a.filename) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                "LOWER(a.contentType) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                "LOWER(a.uploadedBy.username) LIKE LOWER(CONCAT('%', :query, '%'))")
        List<asset> searchAllFields(@Param("query") String query);


    // Fetch only assets uploaded by a specific user
        //List<asset> findByUploadedBy(Users user);

        // Fetch only public assets
        //List<asset> findByIsPublicTrue();

        // Fetch by ID and uploader (for access control)
      //  Optional<asset> findByIdAndUploadedBy(Long id, Users user);
    }


