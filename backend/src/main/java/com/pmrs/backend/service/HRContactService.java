package com.pmrs.backend.service;

import com.pmrs.backend.entity.HRContact;

import java.util.List;

public interface HRContactService {

    List<HRContact> getAllHRContacts();

    HRContact getHRContactById(Integer id);

    HRContact saveHRContact(HRContact hrContact);

    HRContact updateHRContact(Integer id, HRContact hrContact);

    void deleteHRContact(Integer id);

    List<HRContact> getHRContactsByCompanyId(Integer companyId);
}
