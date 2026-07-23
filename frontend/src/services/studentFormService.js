import axios from "axios";

const API_URL = "http://localhost:8080/student-form-submissions";

export const getStudentSubmissions = (status) =>
  axios.get(API_URL, { params: { status } });

export const syncStudentSubmissions = () => axios.post(`${API_URL}/sync`);

export const importAllStudents = () => axios.post(`${API_URL}/import-all`);

export const includeStudentSubmission = (id) => axios.post(`${API_URL}/${id}/include`);

export const rejectStudentSubmission = (id) => axios.post(`${API_URL}/${id}/reject`);
