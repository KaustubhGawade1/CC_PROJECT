package com.dbms.databasemanagementsystem.repository;

import com.dbms.databasemanagementsystem.model.assetPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetpermRepo extends JpaRepository <assetPermission,Long>{
}
