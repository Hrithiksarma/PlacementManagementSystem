import axios from "axios";

const API_URL = "http://localhost:8080/companies";

export const getAllCompanies = () => axios.get(API_URL);

export const getCompanyById = (id) => axios.get(`${API_URL}/${id}`);

export const addCompany = (company) => axios.post(API_URL, company);

export const updateCompany = (id, company) => axios.put(`${API_URL}/${id}`, company);

export const deleteCompany = (id) => axios.delete(`${API_URL}/${id}`);
