import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/api/dashboard`;

export const getDashboardData = (params = {}) => axios.get(API_URL, { params });
