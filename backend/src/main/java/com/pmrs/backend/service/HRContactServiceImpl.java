package com.pmrs.backend.service;

import com.pmrs.backend.entity.HRContact;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.HRContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HRContactServiceImpl implements HRContactService {

    private final HRContactRepository hrContactRepository;

    public HRContactServiceImpl(HRContactRepository hrContactRepository) {
        this.hrContactRepository = hrContactRepository;
    }

    @Override
    public List<HRContact> getAllHRContacts() {
        return hrContactRepository.findAll();
    }

    @Override
    public HRContact getHRContactById(Integer id) {
        return hrContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR Contact not found with id: " + id));
    }

    @Override
    public HRContact saveHRContact(HRContact hrContact) {
        return hrContactRepository.save(hrContact);
    }

    @Override
    public HRContact updateHRContact(Integer id, HRContact updated) {
        HRContact existing = hrContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR Contact not found with id: " + id));
        existing.setCompany(updated.getCompany());
        existing.setHrName(updated.getHrName());
        existing.setHrEmail(updated.getHrEmail());
        existing.setHrPhone(updated.getHrPhone());
        existing.setDesignation(updated.getDesignation());
        return hrContactRepository.save(existing);
    }

    @Override
    public void deleteHRContact(Integer id) {
        hrContactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR Contact not found with id: " + id));
        hrContactRepository.deleteById(id);
    }

    @Override
    public List<HRContact> getHRContactsByCompanyId(Integer companyId) {
        return hrContactRepository.findByCompany_CompanyId(companyId);
    }
}
