import axios from "axios";

const API_URL = "http://localhost:8080/external-hrcontacts";

export const addExternalHrContact = (contact) => axios.post(API_URL, contact);

export const updateExternalHrContact = (id, contact) => axios.put(`${API_URL}/${id}`, contact);

export const deleteExternalHrContact = (id) => axios.delete(`${API_URL}/${id}`);
