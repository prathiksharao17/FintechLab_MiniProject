import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminDashboard.css'; // Add appropriate CSS for layout

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch data from the backend
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const token = localStorage.getItem('authToken');
        const headers = { Authorization: `Bearer ${token}` };

        // Fetch all users
        const usersResponse = await axios.get('http://localhost:8080/api/users', { headers });
        setUsers(usersResponse.data);

        // Fetch all leave requests
        const leaveRequestsResponse = await axios.get('http://localhost:8080/api/leave-requests', { headers });
        setLeaveRequests(leaveRequestsResponse.data);

        setLoading(false);
      } catch (error) {
        console.error("Error fetching admin dashboard data", error);
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <div>Loading dashboard...</div>;
  }

  return (
    <div className="admin-dashboard">
      <h1>Admin Dashboard</h1>

      <section>
        <h2>User Management</h2>
        <ul>
          {users.map((user) => (
            <li key={user.id}>
              {user.username} - {user.role}
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h2>Leave Requests</h2>
        <h3>All Leave Requests</h3>
        <ul>
          {leaveRequests.map((request) => (
            <li key={request.id}>
              {request.employeeName} - {request.leaveType} - {request.status}
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
};

export default AdminDashboard;
