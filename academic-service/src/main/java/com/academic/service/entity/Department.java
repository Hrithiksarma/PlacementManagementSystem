package com.academic.service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Integer deptId;

    @Column(name = "section", nullable = false, unique = true, length = 10)
    private String section;

    @Column(name = "program", nullable = false, length = 20)
    private String program;

    @Column(name = "branch", nullable = false, length = 60)
    private String branch;

    @Column(name = "dept_name", nullable = false, length = 100)
    private String deptName;

    public Integer getDeptId()              { return deptId; }
    public void    setDeptId(Integer v)     { this.deptId = v; }

    public String  getSection()             { return section; }
    public void    setSection(String v)     { this.section = v; }

    public String  getProgram()             { return program; }
    public void    setProgram(String v)     { this.program = v; }

    public String  getBranch()              { return branch; }
    public void    setBranch(String v)      { this.branch = v; }

    public String  getDeptName()            { return deptName; }
    public void    setDeptName(String v)    { this.deptName = v; }
}
