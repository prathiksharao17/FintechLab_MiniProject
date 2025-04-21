import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const PrivateRoute = ({ element, role }) => {
  const userRole = localStorage.getItem('userRole');
  const location = useLocation(); // To preserve the state of the current location

  if (!userRole || userRole !== role) {
    // Redirect them to the login page if they don't have the correct role
    return <Navigate to="/" state={{ from: location }} />;
  }

  return element; // If the user has the correct role, render the passed component (element)
};

export default PrivateRoute;
