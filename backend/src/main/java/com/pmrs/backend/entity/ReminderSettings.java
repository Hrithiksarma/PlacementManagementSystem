package com.pmrs.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Single-row table holding the on/off state of the automatic drive-reminder
 * job — a DB-backed toggle instead of a config-file property, so an officer
 * can flip it from the Notify Students page without a backend restart.
 */
@Entity
@Table(name = "ReminderSettings")
public class ReminderSettings {

    /** Always 1 — this table only ever has a single row. */
    @Id
    private Integer id = 1;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
