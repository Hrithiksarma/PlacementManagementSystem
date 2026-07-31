import axios from "axios";

const API_URL = "http://localhost:8080/applications";

export const getAllApplications = () => axios.get(API_URL);

export const getFilteredApplications = (branch, program, batchYear) =>
  axios.get(`${API_URL}/filter`, { params: { branch, program, batchYear } });

export const addApplication = (application) => axios.post(API_URL, application);

export const updateApplication = (id, application) => axios.put(`${API_URL}/${id}`, application);

export const updateApplicationStatus = (id, status) =>
  axios.put(`${API_URL}/${id}/status`, { status });

export const sendStatusEmail = (id, comment) =>
  axios.post(`${API_URL}/${id}/send-status-email`, { comment });

export const deleteApplication = (id) => axios.delete(`${API_URL}/${id}`);
