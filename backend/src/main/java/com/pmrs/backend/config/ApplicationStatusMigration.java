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
 * One-time rename of legacy Applications.status labels to match the
 * institute's withdrawal-penalty policy wording:
 *
 *   "Shortlisted"     → "First Round"     (cleared the initial screening)
 *   "Offer Released"  → "Selected"        (Selected now IS the job offer —
 *                                          there is no separate offer-released
 *                                          stage; the student decides directly
 *                                          from "Selected")
 *
 * Applications.status is a plain VARCHAR(50) (unlike the tier columns, it was
 * never an ENUM), so this is a straightforward value rename with no schema
 * change needed. Runs on every startup but is a no-op once migrated, since
 * the WHERE clause only matches the legacy values.
 *
 * The table name is resolved case-insensitively via information_schema (see
 * TierNameMigration.resolveTableName for why: MySQL's table-name case
 * sensitivity depends on the host OS, so a literal "Applications" can stop
 * matching Hibernate's actual physical table name on Linux deployments).
 */
@Component
@Order(2) // after TierNameMigration, before DataInitializer
public class ApplicationStatusMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStatusMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String table = resolveTableName("Applications");
        if (table == null) {
            // Table doesn't exist yet (e.g. very first deploy) — nothing to rename.
            return;
        }
        int shortlisted = entityManager.createNativeQuery(
                "UPDATE " + table + " SET status = 'First Round' WHERE status = 'Shortlisted'")
                .executeUpdate();
        int offerReleased = entityManager.createNativeQuery(
                "UPDATE " + table + " SET status = 'Selected' WHERE status = 'Offer Released'")
                .executeUpdate();
        if (shortlisted > 0 || offerReleased > 0) {
            log.info("Application status migration: {} 'Shortlisted' → 'First Round', "
                    + "{} 'Offer Released' → 'Selected'.", shortlisted, offerReleased);
        }
    }

    @SuppressWarnings("unchecked")
    private String resolveTableName(String logicalTable) {
        List<Object> rows = entityManager.createNativeQuery(
                "SELECT TABLE_NAME FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(:table)")
                .setParameter("table", logicalTable)
                .getResultList();
        return rows.isEmpty() ? null : String.valueOf(rows.get(0));
    }
}
