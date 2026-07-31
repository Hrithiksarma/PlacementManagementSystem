package com.pmrs.backend.service;

import com.pmrs.backend.entity.ExternalHrContact;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.ExternalHrContactRepository;
import org.springframework.stereotype.Service;

@Service
public class ExternalHrContactServiceImpl implements ExternalHrContactService {

    private final ExternalHrContactRepository repository;

    public ExternalHrContactServiceImpl(ExternalHrContactRepository repository) {
        this.repository = repository;
    }

    @Override
    public ExternalHrContact getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("External HR contact not found with id: " + id));
    }

    @Override
    public ExternalHrContact save(ExternalHrContact contact) {
        return repository.save(contact);
    }

    @Override
    public ExternalHrContact update(Integer id, ExternalHrContact updated) {
        ExternalHrContact existing = getById(id);
        existing.setCompanyName(updated.getCompanyName());
        existing.setHrName(updated.getHrName());
        existing.setHrEmail(updated.getHrEmail());
        existing.setHrPhone(updated.getHrPhone());
        existing.setDesignation(updated.getDesignation());
        return repository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        getById(id);
        repository.deleteById(id);
    }
}
