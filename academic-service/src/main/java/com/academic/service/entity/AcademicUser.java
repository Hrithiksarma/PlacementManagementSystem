package com.academic.service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_users")
public class AcademicUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    public Long   getId()                   { return id; }
    public void   setId(Long v)             { this.id = v; }

    public String getUsername()             { return username; }
    public void   setUsername(String v)     { this.username = v; }

    public String getPassword()             { return password; }
    public void   setPassword(String v)     { this.password = v; }

    public String getRole()                 { return role; }
    public void   setRole(String v)         { this.role = v; }
}
