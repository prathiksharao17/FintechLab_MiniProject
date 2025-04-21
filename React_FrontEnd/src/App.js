import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthForm from './components/AuthForm';
import EmployeeDashboard from './components/EmployeeDashboard';
import ManagerDashboard from './components/ManagerDashboard';
import AdminDashboard from './components/AdminDashboard'; // Import AdminDashboard
import PrivateRoute from './components/PrivateRoute';
import LandingPage from './components/LandingPage';
import './App.css';

// ✅ Initialize auth once at app load
import { initializeAuth } from './utility/auth';
initializeAuth();

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<AuthForm />} />
          <Route path="/signup" element={<AuthForm />} />
          <Route
            path="/dashboard"
            element={<PrivateRoute role="EMPLOYEE" element={<EmployeeDashboard />} />}
          />
          <Route
            path="/manager-dashboard"
            element={<PrivateRoute role="MANAGER" element={<ManagerDashboard />} />}
          />
          <Route
            path="/admin-dashboard"  // Admin dashboard route
            element={<PrivateRoute role="ADMIN" element={<AdminDashboard />} />} 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
