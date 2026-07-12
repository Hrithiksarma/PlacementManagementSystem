package com.academic.service.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "roll_number_counters",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_counter_dept_year",
        columnNames = {"dept_id", "admission_year"}
    )
)
public class RollNumberCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dept_id", nullable = false)
    private Integer deptId;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "last_serial", nullable = false)
    private Integer lastSerial;

    public Long    getId()                      { return id; }
    public void    setId(Long v)                { this.id = v; }

    public Integer getDeptId()                  { return deptId; }
    public void    setDeptId(Integer v)         { this.deptId = v; }

    public Integer getAdmissionYear()           { return admissionYear; }
    public void    setAdmissionYear(Integer v)  { this.admissionYear = v; }

    public Integer getLastSerial()              { return lastSerial; }
    public void    setLastSerial(Integer v)     { this.lastSerial = v; }
}
