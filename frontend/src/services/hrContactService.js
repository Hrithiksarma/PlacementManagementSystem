import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/hrcontacts`;

export const getAllHRContacts = () => axios.get(API_URL);

export const searchHRContactsByCompany = (companyName) =>
  axios.get(`${API_URL}/search`, { params: { companyName } });

export const getAllHRContactsMerged = () => axios.get(`${API_URL}/search`);

export const addHRContact = (contact) => axios.post(API_URL, contact);

export const updateHRContact = (id, contact) => axios.put(`${API_URL}/${id}`, contact);

export const deleteHRContact = (id) => axios.delete(`${API_URL}/${id}`);
