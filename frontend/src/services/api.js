import axios from 'axios';

// Support VITE_API_BASE_URL for production deployments (e.g. Netlify -> Render)
// Falls back to '/api/v1' for local dev with Vite proxy
const envApiUrl = import.meta.env.VITE_API_BASE_URL;
const baseURL = envApiUrl ? `${envApiUrl.replace(/\/+$/, '')}/api/v1` : '/api/v1';

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('erp_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    const businessId = localStorage.getItem('erp_active_business_id');
    if (businessId) {
      config.headers['X-Business-ID'] = businessId;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('erp_token');
      localStorage.removeItem('erp_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
