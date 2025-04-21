import React, { useEffect, useState } from 'react';
import axios from 'axios';

const SuperiorDashboard = () => {
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [managerAvailable, setManagerAvailable] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const storedUser = JSON.parse(localStorage.getItem('user'));
    if (storedUser && storedUser.id) {
      fetchManagerAvailability();
    }
  }, []);

  const fetchManagerAvailability = async () => {
    try {
      const token = localStorage.getItem('authToken');

      // Fetch manager's availability
      const managerId = 'manager_id'; // Replace with actual manager ID logic
      const response = await axios.get(`http://localhost:8080/api/users/${managerId}/availability`, {
        headers: { Authorization: `Bearer ${token}` }
      });

      setManagerAvailable(response.data.isAvailable);

      if (!response.data.isAvailable) {
        fetchEscalatedRequests();
      }
    } catch (err) {
      console.error('Error fetching manager availability');
    }
  };

  const fetchEscalatedRequests = async () => {
    try {
      const token = localStorage.getItem('authToken');
      const response = await axios.get(`http://localhost:8080/api/leave-requests/escalated`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setLeaveRequests(response.data);
    } catch (err) {
      setMessage('Error fetching escalated requests');
    }
  };

  const handleAction = async (id, action) => {
    try {
      const token = localStorage.getItem('authToken');
      await axios.put(
        `http://localhost:8080/api/leave-requests/${id}/${action}`,
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setLeaveRequests(leaveRequests.filter(req => req.id !== id));
    } catch (err) {
      console.error('Error processing request');
    }
  };

  return (
    <div className="dashboard-container">
      <h2>Superior Manager Dashboard</h2>
      <p>Manager Availability: {managerAvailable ? 'Available ✅' : 'Unavailable ❌'}</p>
      {!managerAvailable ? (
        <>
          <h3>Escalated Leave Requests</h3>
          {leaveRequests.length === 0 ? (
            <p>No escalated requests</p>
          ) : (
            leaveRequests.map(req => (
              <div key={req.id} className="leave-card">
                <p><strong>Employee:</strong> {req.employeeName}</p>
                <p><strong>Reason:</strong> {req.reason}</p>
                <p><strong>Type:</strong> {req.leaveType}</p>
                <p><strong>Status:</strong> {req.status}</p>
                <button onClick={() => handleAction(req.id, 'approve')}>Approve</button>
                <button onClick={() => handleAction(req.id, 'reject')}>Reject</button>
              </div>
            ))
          )}
        </>
      ) : (
        <p>Manager is available. No escalated requests to process.</p>
      )}
    </div>
  );
};

export default SuperiorDashboard;
