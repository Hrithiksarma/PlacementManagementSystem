package com.pmrs.backend.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * One-time migration to the job-category tier codes (A/B/C) introduced by
 * the CTC-based tier policy. The CTC boundaries are unchanged
 * (< 6 / 6–11.99 / ≥ 12 LPA), so existing rows only need relabeling, never
 * recomputing — but {@code 01_schema.sql} declared both tier columns as
 * MySQL {@code ENUM('Normal','Dream','Super Dream')} (Students also allows
 * {@code 'Unplaced'}), so inserting/updating to 'A'/'B'/'C' is rejected by
 * the schema itself until the column is widened. Hibernate's
 * {@code ddl-auto=update} never alters an existing column's type, so that
 * widening has to happen here, before any row is relabeled.
 *
 *   1. Companies.tier / Students.placement_tier: ENUM → VARCHAR(20)
 *   2. "Normal" → "A", "Dream" → "B", "Super Dream" → "C"
 *
 * Runs on every startup but is a no-op once migrated (the ALTER only fires
 * while the column is still an ENUM; the renames only match legacy values),
 * so it is safe to leave in place indefinitely.
 *
 * Table names are resolved case-insensitively via information_schema before
 * use (see resolveTableName) rather than hardcoded — MySQL's table-name case
 * sensitivity depends on the host OS (case-insensitive on Windows, which is
 * where this was first written and tested; case-sensitive by default on
 * Linux, e.g. Railway's deployed MySQL), so a literal "Companies" here can
 * silently stop matching Hibernate's actual physical table name depending on
 * where it runs.
 */
@Component
@Order(1) // before DataInitializer, so seed data always sees the new codes
public class TierNameMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TierNameMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        migrateTable("Companies", "tier", "VARCHAR(20) NOT NULL");
        migrateTable("Students", "placement_tier", "VARCHAR(20) NOT NULL DEFAULT 'Unplaced'");
    }

    private void migrateTable(String logicalTable, String column, String newDefinition) {
        String table = resolveTableName(logicalTable);
        if (table == null) {
            // Table doesn't exist yet (e.g. very first deploy against a fresh
            // database) — nothing to widen or rename.
            return;
        }
        widenIfEnum(table, column, newDefinition);
        int updated = renameTier(table, column);
        if (updated > 0) {
            log.info("Tier naming migration: renamed {} row(s) in {}.{} to the A/B/C scheme.",
                    updated, table, column);
        }
    }

    /** Finds the actual physical table name (correct case) via information_schema, or null if it doesn't exist. */
    @SuppressWarnings("unchecked")
    private String resolveTableName(String logicalTable) {
        List<Object> rows = entityManager.createNativeQuery(
                "SELECT TABLE_NAME FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(:table)")
                .setParameter("table", logicalTable)
                .getResultList();
        return rows.isEmpty() ? null : String.valueOf(rows.get(0));
    }

    /**
     * Companies.tier / Students.placement_tier were declared as a MySQL
     * ENUM restricted to the old labels — widen to a plain VARCHAR so any
     * tier code (current or future) can be stored, without touching the
     * data already in the column.
     */
    @SuppressWarnings("unchecked")
    private void widenIfEnum(String table, String column, String newDefinition) {
        List<Object> rows = entityManager.createNativeQuery(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :table AND COLUMN_NAME = :column")
                .setParameter("table", table)
                .setParameter("column", column)
                .getResultList();
        if (rows.isEmpty()) {
            return;
        }
        Object raw = rows.get(0);
        String dataType = (raw instanceof Object[] arr) ? String.valueOf(arr[0]) : String.valueOf(raw);
        if ("enum".equalsIgnoreCase(dataType)) {
            entityManager.createNativeQuery(
                    "ALTER TABLE " + table + " MODIFY COLUMN " + column + " " + newDefinition)
                    .executeUpdate();
            log.info("Widened {}.{} from ENUM to VARCHAR so it can hold the A/B/C tier codes.",
                    table, column);
        }
    }

    private int renameTier(String table, String column) {
        int updated = 0;
        updated += entityManager.createNativeQuery(
                "UPDATE " + table + " SET " + column + " = 'A' WHERE " + column + " = 'Normal'")
                .executeUpdate();
        updated += entityManager.createNativeQuery(
                "UPDATE " + table + " SET " + column + " = 'B' WHERE " + column + " = 'Dream'")
                .executeUpdate();
        updated += entityManager.createNativeQuery(
                "UPDATE " + table + " SET " + column + " = 'C' WHERE " + column + " = 'Super Dream'")
                .executeUpdate();
        return updated;
    }
}
