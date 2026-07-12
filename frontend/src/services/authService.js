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

// ── Redirect to /login on 401, or to the forced password-change page on 403 ───
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const path = window.location.pathname;

    if (error.response?.status === 401) {
      if (path !== "/login") {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("username");
        window.location.href = "/login";
      }
    }

    if (error.response?.status === 403 && error.response?.data?.error === "PASSWORD_CHANGE_REQUIRED") {
      const role = localStorage.getItem("role");
      const changePasswordPath = role === "STUDENT" ? "/student/change-password" : "/admin/change-password";
      if (path !== changePasswordPath) {
        window.location.href = changePasswordPath;
      }
    }

    if (error.response?.status === 403 && error.response?.data?.error === "ACCOUNT_DISABLED") {
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

export const changePassword = (currentPassword, newPassword) =>
  axios.post(`${AUTH_URL}/change-password`, { currentPassword, newPassword });

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
