package com.pmrs.backend.service;

import com.pmrs.backend.entity.Company;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public Company getCompanyById(Integer id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    @Override
    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Company updateCompany(Integer id, Company updated) {
        Company existing = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        existing.setCompanyName(updated.getCompanyName());
        existing.setSector(updated.getSector());
        existing.setTier(updated.getTier());
        existing.setWebsite(updated.getWebsite());
        return companyRepository.save(existing);
    }

    @Override
    public void deleteCompany(Integer id) {
        companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        companyRepository.deleteById(id);
    }

    @Override
    public List<Company> getCompaniesByTier(String tier) {
        return companyRepository.findByTier(tier);
    }

    @Override
    public List<Company> getCompaniesBySector(String sector) {
        return companyRepository.findBySector(sector);
    }
}
