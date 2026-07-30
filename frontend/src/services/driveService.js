import axios from "axios";

const API_URL = "http://localhost:8080/drives";

export const getAllDrives = () => axios.get(API_URL);

export const addDrive = (drive) => axios.post(API_URL, drive);

export const updateDrive = (id, drive) => axios.put(`${API_URL}/${id}`, drive);

export const updateDriveStatus = (id, status) =>
  axios.patch(`${API_URL}/${id}/status`, { status });

export const deleteDrive = (id) => axios.delete(`${API_URL}/${id}`);

export const getEligibleStudentsForDrive = (driveId) =>
  axios.get(`${API_URL}/${driveId}/eligible-students`);

export const notifyStudents = (driveId, eventType, studentIds) =>
  axios.post(`${API_URL}/${driveId}/notify`, { eventType, studentIds });
