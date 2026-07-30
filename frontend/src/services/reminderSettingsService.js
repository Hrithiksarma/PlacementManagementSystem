import axios from "axios";

const API_URL = "http://localhost:8080/reminder-settings";

export const getReminderSettings = () => axios.get(API_URL);

export const updateReminderSettings = (enabled) => axios.put(API_URL, { enabled });
