import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/form-submissions`;

export const getSubmissions = (status) =>
  axios.get(API_URL, { params: { status } });

export const getCompaniesWithoutSubmission = () =>
  axios.get(`${API_URL}/not-submitted`);

export const syncSubmissions = () => axios.post(`${API_URL}/sync`);

export const includeSubmission = (id) => axios.post(`${API_URL}/${id}/include`);

export const rejectSubmission = (id) => axios.post(`${API_URL}/${id}/reject`);
