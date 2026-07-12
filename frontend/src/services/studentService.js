import axios from "axios";

const API_URL = "http://localhost:8080/students";

export const getStudents = () => axios.get(API_URL);

export const addStudent = (student) => axios.post(API_URL, student);

export const updateStudent = (id, student) => axios.put(`${API_URL}/${id}`, student);

export const deleteStudent = (id) => axios.delete(`${API_URL}/${id}`);

export const getFilteredStudents = (department, program, batchYear) =>
  axios.get(`${API_URL}/filter`, { params: { department, program, batchYear } });

export const previewStudent = (rollNo) =>
  axios.get(`${API_URL}/preview/${rollNo}`);

export const importStudent = (rollNo) =>
  axios.post(`${API_URL}/import/${rollNo}`);

export const backfillStudentAccounts = () =>
  axios.post(`${API_URL}/backfill-accounts`);
