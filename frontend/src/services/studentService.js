import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/students`;

export const getStudents = () => axios.get(API_URL);

export const addStudent = (student) => axios.post(API_URL, student);

export const updateStudent = (id, student) => axios.put(`${API_URL}/${id}`, student);

export const deleteStudent = (id) => axios.delete(`${API_URL}/${id}`);

export const getFilteredStudents = (department, program, batchYear) =>
  axios.get(`${API_URL}/filter`, { params: { department, program, batchYear } });

export const backfillStudentAccounts = () =>
  axios.post(`${API_URL}/backfill-accounts`);

export const resetStudentPassword = (id) =>
  axios.post(`${API_URL}/${id}/reset-password`);
