package com.pmrs.backend.service;

import com.pmrs.backend.entity.ExternalHrContact;

public interface ExternalHrContactService {

    ExternalHrContact getById(Integer id);

    ExternalHrContact save(ExternalHrContact contact);

    ExternalHrContact update(Integer id, ExternalHrContact contact);

    void delete(Integer id);
}
