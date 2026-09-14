import axios from "axios";

/**
 * Central Axios instance. Base URL defaults to a relative "/api" path so that in
 * production the frontend and backend can sit behind the same reverse proxy (see
 * nginx.conf), while in dev the Vite proxy (vite.config.ts) forwards to Spring Boot.
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("recall_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("recall_token");
      localStorage.removeItem("recall_user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  },
);
