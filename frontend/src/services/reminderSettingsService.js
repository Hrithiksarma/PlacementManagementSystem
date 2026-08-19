import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/reminder-settings`;

export const getReminderSettings = () => axios.get(API_URL);

export const updateReminderSettings = (enabled) => axios.put(API_URL, { enabled });
