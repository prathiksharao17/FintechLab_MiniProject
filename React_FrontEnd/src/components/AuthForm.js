import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './AuthForm.css';

const AuthForm = () => {
  const [isSignup, setIsSignup] = useState(true);
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    role: 'EMPLOYEE',
    firstName: '',
    lastName: '',
    department: '',
    position: '',
    phoneNumber: ''
  });
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const url = isSignup
      ? 'http://localhost:8080/api/auth/signup'
      : 'http://localhost:8080/api/auth/login';

    try {
      const payload = isSignup
        ? formData
        : {
            email: formData.email,
            password: formData.password
          };

      const res = await axios.post(url, payload);

      if (!isSignup) {
        const { token, role, userId, username } = res.data;

        if (token) {
          // Store token and user data in localStorage
          localStorage.setItem('authToken', token);
          localStorage.setItem('userRole', role);
          localStorage.setItem('userId', userId);
          localStorage.setItem('username', username);

          // Set authToken for axios globally
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          setMessage('Login successful!');

          // Navigate based on the role
          if (role === 'MANAGER') {
            navigate('/manager-dashboard');
          } else if (role === 'ADMIN') {
            navigate('/admin-dashboard');
          } else {
            navigate('/dashboard');
          }
        } else {
          setMessage('Login failed: No token received.');
        }
      } else {
        setMessage(res.data.message || 'Signup successful!');
      }
    } catch (err) {
      setMessage(err.response?.data?.message || 'Something went wrong');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>{isSignup ? 'Sign Up' : 'Login'}</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          name="email"
          placeholder="Email"
          onChange={handleChange}
          required
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          onChange={handleChange}
          required
        />

        {isSignup && (
          <>
            <input
              type="text"
              name="firstName"
              placeholder="First Name"
              onChange={handleChange}
              required
            />
            <input
              type="text"
              name="lastName"
              placeholder="Last Name"
              onChange={handleChange}
              required
            />
            <input
              type="text"
              name="phoneNumber"
              placeholder="Phone Number"
              onChange={handleChange}
            />
            <input
              type="text"
              name="department"
              placeholder="Department"
              onChange={handleChange}
            />
            <input
              type="text"
              name="position"
              placeholder="Position"
              onChange={handleChange}
            />
            <select name="role" onChange={handleChange} defaultValue="EMPLOYEE">
              <option value="MANAGER">Manager</option>
              <option value="EMPLOYEE">Employee</option>
              <option value="ADMIN">Admin</option>
            </select>
          </>
        )}

        <button type="submit" disabled={loading}>
          {loading ? 'Loading...' : isSignup ? 'Sign Up' : 'Login'}
        </button>
      </form>

      <p>{message}</p>
      <button onClick={() => setIsSignup(!isSignup)}>
        {isSignup ? 'Already a user? Login' : 'New here? Sign Up'}
      </button>
    </div>
  );
};

export default AuthForm;
