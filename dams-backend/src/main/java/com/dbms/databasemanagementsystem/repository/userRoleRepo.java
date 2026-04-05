package com.dbms.databasemanagementsystem.repository;

import com.dbms.databasemanagementsystem.model.acces;
import com.dbms.databasemanagementsystem.model.user_role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userRoleRepo extends JpaRepository<user_role,Long> {



    Optional<user_role> findByUserRole(acces userRole);
}
