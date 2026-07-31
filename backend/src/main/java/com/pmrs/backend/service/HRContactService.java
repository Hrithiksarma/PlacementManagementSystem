package com.pmrs.backend.service;

import com.pmrs.backend.dto.HrContactSearchResult;
import com.pmrs.backend.entity.HRContact;

import java.util.List;

public interface HRContactService {

    List<HRContact> getAllHRContacts();

    HRContact getHRContactById(Integer id);

    HRContact saveHRContact(HRContact hrContact);

    HRContact updateHRContact(Integer id, HRContact hrContact);

    void deleteHRContact(Integer id);

    List<HRContact> getHRContactsByCompanyId(Integer companyId);

    /**
     * Merges real HRContact rows (if companyName matches a real Company),
     * ExternalHrContact rows (matched by free-text company name), and
     * not-yet-included Google Form drive submissions for that company —
     * each tagged with its source so the caller can tell them apart.
     */
    List<HrContactSearchResult> searchByCompanyName(String companyName);

    /** All real HRContact rows plus all ExternalHrContact rows, tagged with source, no company filter. */
    List<HrContactSearchResult> getAllMerged();
}
