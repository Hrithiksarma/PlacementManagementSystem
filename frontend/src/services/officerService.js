import axios from "axios";

const API_URL = "http://localhost:8080/admin/officers";

export const getOfficers = () => axios.get(API_URL);

export const createOfficer = (username, email) =>
  axios.post(API_URL, { username, email });

export const disableOfficer = (id) => axios.patch(`${API_URL}/${id}/disable`);

export const enableOfficer = (id) => axios.patch(`${API_URL}/${id}/enable`);
