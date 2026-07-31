package com.pmrs.backend.service;

import com.pmrs.backend.dto.HrContactSearchResult;
import com.pmrs.backend.entity.Company;
import com.pmrs.backend.entity.ExternalHrContact;
import com.pmrs.backend.entity.HRContact;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.CompanyRepository;
import com.pmrs.backend.repository.ExternalHrContactRepository;
import com.pmrs.backend.repository.HRContactRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HRContactServiceImpl implements HRContactService {

    private final HRContactRepository hrContactRepository;
    private final CompanyRepository companyRepository;
    private final ExternalHrContactRepository externalHrContactRepository;

    public HRContactServiceImpl(HRContactRepository hrContactRepository,
                                CompanyRepository companyRepository,
                                ExternalHrContactRepository externalHrContactRepository) {
        this.hrContactRepository = hrContactRepository;
        this.companyRepository = companyRepository;
        this.externalHrContactRepository = externalHrContactRepository;
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

    @Override
    public List<HrContactSearchResult> searchByCompanyName(String companyName) {
        List<HrContactSearchResult> results = new ArrayList<>();

        Company company = companyRepository.findByCompanyNameIgnoreCase(companyName).orElse(null);

        if (company != null) {
            for (HRContact hr : hrContactRepository.findByCompany_CompanyId(company.getCompanyId())) {
                HrContactSearchResult r = new HrContactSearchResult();
                r.setSource(hr.getSource() == null ? "MANUAL" : hr.getSource().name());
                r.setHrId(hr.getHrId());
                r.setName(hr.getHrName());
                r.setEmail(hr.getHrEmail());
                r.setPhone(hr.getHrPhone());
                r.setDesignation(hr.getDesignation());
                r.setCompanyName(company.getCompanyName());
                results.add(r);
            }
        }

        for (ExternalHrContact ext : externalHrContactRepository.findByCompanyNameContainingIgnoreCase(companyName)) {
            HrContactSearchResult r = new HrContactSearchResult();
            r.setSource("EXTERNAL");
            r.setExternalId(ext.getId());
            r.setName(ext.getHrName());
            r.setEmail(ext.getHrEmail());
            r.setPhone(ext.getHrPhone());
            r.setDesignation(ext.getDesignation());
            r.setCompanyName(ext.getCompanyName());
            results.add(r);
        }

        return results;
    }

    @Override
    public List<HrContactSearchResult> getAllMerged() {
        List<HrContactSearchResult> results = new ArrayList<>();

        for (HRContact hr : hrContactRepository.findAll()) {
            HrContactSearchResult r = new HrContactSearchResult();
            r.setSource(hr.getSource() == null ? "MANUAL" : hr.getSource().name());
            r.setHrId(hr.getHrId());
            r.setName(hr.getHrName());
            r.setEmail(hr.getHrEmail());
            r.setPhone(hr.getHrPhone());
            r.setDesignation(hr.getDesignation());
            r.setCompanyName(hr.getCompany() != null ? hr.getCompany().getCompanyName() : null);
            results.add(r);
        }

        for (ExternalHrContact ext : externalHrContactRepository.findAll()) {
            HrContactSearchResult r = new HrContactSearchResult();
            r.setSource("EXTERNAL");
            r.setExternalId(ext.getId());
            r.setName(ext.getHrName());
            r.setEmail(ext.getHrEmail());
            r.setPhone(ext.getHrPhone());
            r.setDesignation(ext.getDesignation());
            r.setCompanyName(ext.getCompanyName());
            results.add(r);
        }

        return results;
    }
}
