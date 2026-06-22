import axios from "axios";

const AUTH_URL = "http://localhost:8080/auth";

// ── Attach JWT to every request ───────────────────────────────────────────────
axios.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Redirect to /login on 401 ─────────────────────────────────────────────────
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const path = window.location.pathname;
      if (path !== "/login") {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("username");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export const login = (username, password) =>
  axios.post(`${AUTH_URL}/login`, { username, password });

export const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("username");
  window.location.href = "/login";
};

export const getRole     = () => localStorage.getItem("role") ?? "";
export const getUsername = () => localStorage.getItem("username") ?? "";
export const isLoggedIn  = () => Boolean(localStorage.getItem("token"));

// ── Permission helpers ────────────────────────────────────────────────────────
export const hasRole    = (role)  => getRole() === role;
export const hasAnyRole = (roles) => roles.includes(getRole());
export const isAdmin    = ()      => hasRole("ADMIN");
export const isOfficer  = ()      => hasRole("PLACEMENT_OFFICER");
