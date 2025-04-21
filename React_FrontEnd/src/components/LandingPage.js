import React from 'react';
import { Link } from 'react-router-dom';
import './LandingPage.css';

const LandingPage = () => {
  return (
    <div className="landing-page">
      <div className="overlay">
        <div className="content">
          <h1 className="title">Welcome to the Leave Request System</h1>
          <p className="description">Manage your leave requests with ease and efficiency.</p>
          <div className="buttons">
            <Link to="/login" className="btn login-btn">Login</Link>
            <Link to="/signup" className="btn signup-btn">Sign Up</Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;