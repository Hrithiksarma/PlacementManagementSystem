package com.pmrs.backend.entity;

import jakarta.persistence.*;

/**
 * Persistent counter used by {@link com.pmrs.backend.service.LocalRollNumberService}
 * to allocate sequential, collision-free roll number serials.
 *
 * <p>One row exists per (dept_id, batch_year) combination.
 * {@code lastSerial} holds the highest serial already issued for that group.
 * The next student in the group receives {@code lastSerial + 1}.
 *
 * <p>Concurrency is handled at the database layer: the service reads this row
 * with {@code SELECT ... FOR UPDATE} (pessimistic lock), increments
 * {@code lastSerial}, and saves — all within the same transaction.  No two
 * transactions can hold the lock for the same row simultaneously, so serials
 * are always unique.
 */
@Entity
@Table(
    name = "roll_number_counters",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_counter_dept_batch",
        columnNames = {"dept_id", "batch_year"}
    )
)
public class RollNumberCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Foreign key to Departments — identifies which department this counter belongs to. */
    @Column(name = "dept_id", nullable = false)
    private Integer deptId;

    /** The academic batch year (e.g. 2025). Serials restart at 1 for each new batch. */
    @Column(name = "batch_year", nullable = false)
    private Integer batchYear;

    /**
     * The highest serial number already issued for this (dept_id, batch_year).
     * Starts at 0 when the row is first created.  Incremented once per student.
     */
    @Column(name = "last_serial", nullable = false)
    private Integer lastSerial;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public Integer getBatchYear() { return batchYear; }
    public void setBatchYear(Integer batchYear) { this.batchYear = batchYear; }

    public Integer getLastSerial() { return lastSerial; }
    public void setLastSerial(Integer lastSerial) { this.lastSerial = lastSerial; }
}
