import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './EmployeeDashboard.css';

const EmployeeDashboard = () => {
  const [leaveData, setLeaveData] = useState({
    name: '',
    startDate: '',
    endDate: '',
    leaveType: 'ANNUAL',
    reason: ''
  });

  const [requests, setRequests] = useState([]);
  const [userProfile, setUserProfile] = useState(null); // ✅ profile state
  const [message, setMessage] = useState('');
  const token = localStorage.getItem('authToken');

  const handleChange = (e) => {
    setLeaveData({ ...leaveData, [e.target.name]: e.target.value });
  };

  const submitLeaveRequest = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:8080/api/leave-requests', leaveData, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setMessage('Leave request submitted successfully!');
      setLeaveData({
        name: '',
        startDate: '',
        endDate: '',
        leaveType: 'ANNUAL',
        reason: ''
      });
      fetchLeaveRequests();
    } catch (error) {
      setMessage(error.response?.data?.message || 'Error submitting leave request');
    }
  };

  const fetchLeaveRequests = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/leave-requests/profile', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setRequests(response.data);
    } catch (error) {
      console.error('Error fetching leave requests:', error);
    }
  };

  const fetchUserProfile = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/users/profile', {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      setUserProfile(response.data);
    } catch (error) {
      console.error('Error fetching user profile:', error);
    }
  };

  const cancelLeaveRequest = async (id) => {
    try {
      await axios.delete(`http://localhost:8080/api/leave-requests/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      setMessage('Leave request cancelled.');
      fetchLeaveRequests();
    } catch (error) {
      setMessage(error.response?.data?.message || 'Failed to cancel leave request.');
    }
  };

  useEffect(() => {
    fetchLeaveRequests();
    fetchUserProfile(); // ✅ fetch profile on load
  }, []);

  return (
    <div className="dashboard-container">
      <h2 className="dashboard-heading">Employee Dashboard</h2>

      {userProfile && (
        <div className="profile-section">
          <h3 className="section-heading">Welcome, {userProfile.name}</h3>
          <p><strong>Email:</strong> {userProfile.email}</p>
          <p><strong>Department:</strong> {userProfile.department}</p>
        </div>
      )}

      <div className="leave-request-section">
        <h3 className="section-heading">Submit Leave Request</h3>
        <form onSubmit={submitLeaveRequest} className="leave-form">
          <input
            type="text"
            name="name"
            placeholder="Your Name"
            value={leaveData.name}
            onChange={handleChange}
            required
            className="input-field"
          />
          <input
            type="date"
            name="startDate"
            value={leaveData.startDate}
            onChange={handleChange}
            required
            className="input-field"
          />
          <input
            type="date"
            name="endDate"
            value={leaveData.endDate}
            onChange={handleChange}
            required
            className="input-field"
          />
          <select
            name="leaveType"
            value={leaveData.leaveType}
            onChange={handleChange}
            className="input-field"
          >
            <option value="ANNUAL">Annual Leave</option>
            <option value="SICK">Sick Leave</option>
            <option value="UNPAID">Unpaid Leave</option>
          </select>
          <textarea
            name="reason"
            placeholder="Reason for leave"
            value={leaveData.reason}
            onChange={handleChange}
            required
            className="input-field"
          />
          <button type="submit" className="submit-button">Submit</button>
        </form>

        {message && <p className="status-message">{message}</p>}
      </div>

      <div className="leave-requests-section">
        <h3 className="section-heading">Your Leave Requests</h3>
        <ul className="leave-requests-list">
          {requests.map((req) => (
            <li key={req.id} className="leave-request-item">
              <div className="leave-request-info">
                <strong>{req.startDate} to {req.endDate}</strong> - {req.leaveType} ({req.status})<br />
                
                <span><strong>Email:</strong> {req.email || userProfile?.email}</span>
              </div>
              {(req.status === 'PENDING' || req.status === 'APPROVED') && (
                <button
                  onClick={() => cancelLeaveRequest(req.id)}
                  className="cancel-button"
                >
                  Cancel Leave
                </button>
              )}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default EmployeeDashboard;
