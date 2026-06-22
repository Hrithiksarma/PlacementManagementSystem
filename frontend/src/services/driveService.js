import axios from "axios";

const API_URL = "http://localhost:8080/drives";

export const getAllDrives = () => axios.get(API_URL);

export const addDrive = (drive) => axios.post(API_URL, drive);

export const updateDrive = (id, drive) => axios.put(`${API_URL}/${id}`, drive);

export const deleteDrive = (id) => axios.delete(`${API_URL}/${id}`);
