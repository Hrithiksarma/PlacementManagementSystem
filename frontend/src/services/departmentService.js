import axios from "axios";

const API_URL = "http://localhost:8080/departments";

export const getDepartments = () => axios.get(API_URL);
