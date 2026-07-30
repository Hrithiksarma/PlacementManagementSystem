import axios from "axios";

const API_URL = "http://localhost:8080/notifications";

export const getNotifications = () => axios.get(API_URL);

export const getUnreadNotificationCount = () => axios.get(`${API_URL}/unread-count`);

export const markNotificationRead = (id) => axios.post(`${API_URL}/${id}/read`);

export const markAllNotificationsRead = () => axios.post(`${API_URL}/read-all`);
