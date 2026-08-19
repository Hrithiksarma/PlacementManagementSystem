import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/admin/staff`;

export const getStaffAccount = () => axios.get(API_URL);

export const createStaff = (username, email) =>
  axios.post(API_URL, { username, email });

export const resetStaffPassword = () => axios.post(`${API_URL}/reset-password`);

export const disableStaff = (id) => axios.patch(`${API_URL}/${id}/disable`);

export const enableStaff = (id) => axios.patch(`${API_URL}/${id}/enable`);

export const sendStaffWelcomeEmail = (userId, temporaryPassword, comment) =>
  axios.post(`${API_URL}/${userId}/send-welcome-email`, { temporaryPassword, comment });
