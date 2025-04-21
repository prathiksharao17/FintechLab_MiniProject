// utility/auth.js
import axios from 'axios';

// Token management
const TOKEN_KEY = 'authToken';
const USER_KEY = 'userData';

// Store token and user data
export const setAuthToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token);
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
};

export const setUserData = (userData) => {
  localStorage.setItem(USER_KEY, JSON.stringify(userData));
};

// Retrieve token and user data
export const getAuthToken = () => {
  return localStorage.getItem(TOKEN_KEY);
};

export const getUserData = () => {
  const userData = localStorage.getItem(USER_KEY);
  return userData ? JSON.parse(userData) : null;
};

// Check if user is authenticated
export const isAuthenticated = () => {
  return !!getAuthToken();
};

// Get user role
export const getUserRole = () => {
  const userData = getUserData();
  return userData ? userData.role : null;
};

// Clear authentication data
export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  delete axios.defaults.headers.common['Authorization'];
};

// Authentication API calls
export const loginUser = async (credentials) => {
  try {
    const response = await axios.post('/api/auth/login', credentials);
    if (response.data && response.data.token) {
      setAuthToken(response.data.token);
      setUserData({
        id: response.data.userId,
        username: response.data.username,
        role: response.data.role,
        // Add any other user data from response
      });
      return response.data;
    }
    return null;
  } catch (error) {
    throw error.response?.data?.message || 'Login failed';
  }
};

export const signupUser = async (userData) => {
  try {
    const response = await axios.post('/api/auth/signup', userData);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || 'Signup failed';
  }
};

export const logoutUser = () => {
  clearAuth();
};

// Set up axios interceptor to handle token expiration
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // Token expired or invalid
      clearAuth();
      window.location.href = '/ui_login';
    }
    return Promise.reject(error);
  }
);

// Get user profile data (leave requests)
export const getUserProfile = async () => {
  try {
    const response = await axios.get('/api/leave-requests/profile'); // Replace with your actual endpoint
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || 'Error fetching profile';
  }
};

// Initialize - call this when your app starts
export const initializeAuth = () => {
  const token = getAuthToken();
  if (token) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }
};
