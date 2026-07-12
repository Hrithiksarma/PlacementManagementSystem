import axios from "axios";

const api = axios.create({ baseURL: "/api" });

// Attach JWT token to every request automatically
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("academic_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If token expires mid-session, redirect to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("academic_token");
      localStorage.removeItem("academic_user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export const login = (credentials) =>
  api.post("/auth/login", credentials);

export const getStudents = () =>
  api.get("/students");

export const createStudent = (data) =>
  api.post("/students", data);

export const getStudent = (rollNo) =>
  api.get(`/students/${rollNo}`);

export const updateStudent = (rollNo, data) =>
  api.put(`/students/${rollNo}`, data);

export const deleteStudent = (rollNo) =>
  api.delete(`/students/${rollNo}`);
