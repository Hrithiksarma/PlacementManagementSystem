import axios from "axios";

const API_URL = "http://localhost:8080/api/dashboard";

export const getDashboardData = (params = {}) => axios.get(API_URL, { params });
