package com.pmrs.backend.dto;

import java.util.List;

/** Result of the admin-triggered bulk backfill of student login accounts. */
public class BackfillResultDTO {
    private final int created;
    private final int skipped;
    private final List<String> createdRollNumbers;

    public BackfillResultDTO(int created, int skipped, List<String> createdRollNumbers) {
        this.created = created;
        this.skipped = skipped;
        this.createdRollNumbers = createdRollNumbers;
    }

    public int getCreated() { return created; }
    public int getSkipped() { return skipped; }
    public List<String> getCreatedRollNumbers() { return createdRollNumbers; }
}
