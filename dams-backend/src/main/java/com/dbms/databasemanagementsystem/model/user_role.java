package com.dbms.databasemanagementsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "user_role")
@Getter
@Setter
@NoArgsConstructor
public class user_role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role__id")
    private Long role_id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "role_name")
    private acces userRole;

    public user_role(acces userRole) {
        this.userRole = userRole;
    }

    public Long getId() {
        return role_id;
    }
}

