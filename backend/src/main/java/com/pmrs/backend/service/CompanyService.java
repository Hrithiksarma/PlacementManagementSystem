package com.pmrs.backend.service;

import com.pmrs.backend.entity.Company;

import java.util.List;

public interface CompanyService {

    List<Company> getAllCompanies();

    Company getCompanyById(Integer id);

    Company saveCompany(Company company);

    Company updateCompany(Integer id, Company company);

    void deleteCompany(Integer id);

    List<Company> getCompaniesByTier(String tier);

    List<Company> getCompaniesBySector(String sector);
}
