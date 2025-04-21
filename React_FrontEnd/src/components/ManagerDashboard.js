import React, { useEffect, useState } from 'react'; 
import axios from 'axios';
import './ManagerDashboard.css';

const ManagerDashboard = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [pendingLeaveRequests, setPendingLeaveRequests] = useState([]);
  const [escalatedLeaveRequests, setEscalatedLeaveRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [rejectionReasons, setRejectionReasons] = useState({});
  const [message, setMessage] = useState('');
  const token = localStorage.getItem('authToken');

  const [requests, setRequests] = useState([]);

  const [leaveRequestData, setLeaveRequestData] = useState({
    employeeName: '',
    leaveType: '',
    reason: '',
    startDate: '',
    endDate: ''
  });

  const [managerProfile, setManagerProfile] = useState(null);
  const [availabilityLoading, setAvailabilityLoading] = useState(false);

  useEffect(() => {
    const fetchLeaveRequests = async () => {
      try {
        const token = localStorage.getItem('authToken');
        if (!token) {
          setMessage('No token found. Please log in.');
          return;
        }

        // Fetch Pending Leave Requests for the current manager
        const pendingResponse = await axios.get('http://localhost:8080/api/leave-requests/pending-approval', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        // Fetch Escalated Leave Requests for the current manager
        const escalatedResponse = await axios.get('http://localhost:8080/api/leave-requests/escalated-requests', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setPendingLeaveRequests(pendingResponse.data);
        setEscalatedLeaveRequests(escalatedResponse.data);
      } catch (error) {
        setMessage('Error fetching leave requests.');
      } finally {
        setLoading(false);
      }
    };

    fetchLeaveRequests();
  }, []);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem('authToken');
        const response = await axios.get('http://localhost:8080/api/users/profile', {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setManagerProfile(response.data);
      } catch (error) {
        setMessage('Error fetching profile.');
      }
    };

    if (activeTab === 'profile') {
      fetchProfile();
    }
  }, [activeTab]);

  const handleToggleAvailability = async () => {
    if (!managerProfile || !managerProfile.id) return;

    setAvailabilityLoading(true);
    try {
      const token = localStorage.getItem('authToken');
      await axios.put(
        `http://localhost:8080/api/users/${managerProfile.id}/availability`,  // Updated to use managerProfile.id
        { isAvailable: !managerProfile.isAvailable },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setManagerProfile((prev) => ({
        ...prev,
        isAvailable: !prev.isAvailable,
      }));
      setMessage('Availability status updated.');
    } catch (error) {
      setMessage('Error updating availability.');
    } finally {
      setAvailabilityLoading(false);
    }
  };

  const handleReasonChange = (id, value) => {
    setRejectionReasons((prev) => ({ ...prev, [id]: value }));
  };

  const handleApprove = async (leaveRequestId, isEscalated = false) => {
    try {
      const token = localStorage.getItem('authToken');
      if (!token) {
        setMessage('Please log in to approve leave requests.');
        return;
      }
  
      const requestUrl = isEscalated
        ? `http://localhost:8080/api/leave-requests/escalated-requests/${leaveRequestId}/status`
        : `http://localhost:8080/api/leave-requests/${leaveRequestId}/status`;
  
      await axios.put(
        requestUrl,
        { status: 'APPROVED', comments: '' }, // Approving the request with no comments (optional)
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
  
      if (isEscalated) {
        setEscalatedLeaveRequests(escalatedLeaveRequests.filter((req) => req.id !== leaveRequestId));
      } else {
        setPendingLeaveRequests(pendingLeaveRequests.filter((req) => req.id !== leaveRequestId));
      }
      setMessage('Leave request approved.');
    } catch (error) {
      setMessage('Error approving leave request.');
    }
  };
  
  const handleReject = async (leaveRequestId, isEscalated = false) => {
    const reason = rejectionReasons[leaveRequestId];
    if (!reason) {
      alert('Please provide a reason for rejection.');
      return;
    }
  
    try {
      const token = localStorage.getItem('authToken');
      if (!token) {
        setMessage('Please log in to reject leave requests.');
        return;
      }
  
      const requestUrl = isEscalated
        ? `http://localhost:8080/api/leave-requests/escalated-requests/${leaveRequestId}/status`
        : `http://localhost:8080/api/leave-requests/${leaveRequestId}/status`;
  
      await axios.put(
        requestUrl,
        { status: 'REJECTED', comments: reason }, // Rejecting with the reason
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
  
      if (isEscalated) {
        setEscalatedLeaveRequests(escalatedLeaveRequests.filter((req) => req.id !== leaveRequestId));
      } else {
        setPendingLeaveRequests(pendingLeaveRequests.filter((req) => req.id !== leaveRequestId));
      }
  
      setRejectionReasons((prev) => {
        const updated = { ...prev };
        delete updated[leaveRequestId];
        return updated;
      });
      setMessage('Leave request rejected.');
    } catch (error) {
      setMessage('Error rejecting leave request.');
    }
  };

  

  const formatDate = (dateStr) => {
    return new Date(dateStr).toLocaleDateString();
  };

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  const handleLeaveRequestChange = (e) => {
    const { name, value } = e.target;
    setLeaveRequestData((prev) => ({ ...prev, [name]: value }));
  };

  const fetchLeaveRequests = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/leave-requests/profile', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setRequests(response.data);
    } catch (error) {
      console.error('Error fetching leave requests:', error);
    }
  };
  useEffect(() => {
    fetchLeaveRequests();
  }, []);
  

  const handleCreateLeaveRequest = async () => {
    try {
      const token = localStorage.getItem('authToken');
      if (!token) {
        setMessage('Please log in to submit leave requests.');
        return;
      }

      await axios.post(
        'http://localhost:8080/api/leave-requests',
        leaveRequestData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setMessage('Leave request created successfully.');
      setLeaveRequestData({
        employeeName: '',
        leaveType: '',
        reason: '',
        startDate: '',
        endDate: ''
      });
    } catch (error) {
      setMessage('Error creating leave request.');
    }
  };

  return (
    <div className="dashboard-container">
      <h2>Manager Dashboard</h2>
      {message && <div className="message">{message}</div>}
      <nav>
        <button onClick={() => handleTabChange('dashboard')}>Dashboard</button>
        <button onClick={() => handleTabChange('createLeave')}>Create Leave Request</button>
        <button onClick={() => handleTabChange('profile')}>Profile</button>
      </nav>

      <div className="tab-content">
        {activeTab === 'dashboard' && (
          <div>
            <h3>Pending Leave Requests</h3>
            {loading ? (
              <p>Loading pending leave requests...</p>
            ) : (
              <div className="leave-requests-list">
                {pendingLeaveRequests.length === 0 ? (
                  <p>No pending leave requests.</p>
                ) : (
                  pendingLeaveRequests.map((leaveRequest) => (
                    <div key={leaveRequest.id} className="leave-request-card">
                      <h3>{leaveRequest.employeeName}</h3>
                      <p><strong>Leave Type:</strong> {leaveRequest.leaveType}</p>
                      <p><strong>Reason:</strong> {leaveRequest.reason}</p>
                      <p><strong>Status:</strong> {leaveRequest.status}</p>
                      <p><strong>Start Date:</strong> {formatDate(leaveRequest.startDate)}</p>
                      <p><strong>End Date:</strong> {formatDate(leaveRequest.endDate)}</p>

                      {leaveRequest.status === 'PENDING' && (
                        <div className="actions">
                          <button onClick={() => handleApprove(leaveRequest.id)} className="approve-button">Approve</button>
                          <button onClick={() => handleReject(leaveRequest.id)} className="reject-button">Reject</button>
                          <textarea
                            placeholder="Enter rejection reason"
                            value={rejectionReasons[leaveRequest.id] || ''}
                            onChange={(e) => handleReasonChange(leaveRequest.id, e.target.value)}
                          ></textarea>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            )}

            <h3>Escalated Leave Requests</h3>
            {loading ? (
              <p>Loading escalated leave requests...</p>
            ) : (
              <div className="leave-requests-list">
                {escalatedLeaveRequests.length === 0 ? (
                  <p>No escalated leave requests.</p>
                ) : (
                  escalatedLeaveRequests.map((leaveRequest) => (
                    <div key={leaveRequest.id} className="leave-request-card">
                      <h3>{leaveRequest.employeeName}</h3>
                      <p><strong>Leave Type:</strong> {leaveRequest.leaveType}</p>
                      <p><strong>Reason:</strong> {leaveRequest.reason}</p>
                      <p><strong>Status:</strong> {leaveRequest.status}</p>
                      <p><strong>Start Date:</strong> {formatDate(leaveRequest.startDate)}</p>
                      <p><strong>End Date:</strong> {formatDate(leaveRequest.endDate)}</p>

                      {leaveRequest.status === 'PENDING' && (
                        <div className="actions">
                          <button onClick={() => handleApprove(leaveRequest.id, true)} className="approve-button">Approve</button>
                          <button onClick={() => handleReject(leaveRequest.id, true)} className="reject-button">Reject</button>
                          <textarea
                            placeholder="Enter rejection reason"
                            value={rejectionReasons[leaveRequest.id] || ''}
                            onChange={(e) => handleReasonChange(leaveRequest.id, e.target.value)}
                          ></textarea>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        )}

        {activeTab === 'createLeave' && (
          <div className="create-leave-request">
            <h3>Create Leave Request</h3>
            <form>
              <input
                type="text"
                name="employeeName"
                value={leaveRequestData.employeeName}
                placeholder="Employee Name"
                onChange={handleLeaveRequestChange}
              />
              <input
                type="text"
                name="leaveType"
                value={leaveRequestData.leaveType}
                placeholder="Leave Type"
                onChange={handleLeaveRequestChange}
              />
              <textarea
                name="reason"
                value={leaveRequestData.reason}
                placeholder="Reason"
                onChange={handleLeaveRequestChange}
              />
              <input
                type="date"
                name="startDate"
                value={leaveRequestData.startDate}
                onChange={handleLeaveRequestChange}
              />
              <input
                type="date"
                name="endDate"
                value={leaveRequestData.endDate}
                onChange={handleLeaveRequestChange}
              />
              <button type="button" onClick={handleCreateLeaveRequest}>Submit</button>
            </form>
          </div>
        )}

        {activeTab === 'profile' && managerProfile && (
          <div className="profile">
            <h3>Profile</h3>
            <p><strong>Name:</strong> {managerProfile.name}</p>
            <p><strong>Email:</strong> {managerProfile.email}</p>
            <button onClick={handleToggleAvailability}>
              {availabilityLoading ? 'Updating availability...' : managerProfile.isAvailable ? 'Mark Unavailable' : 'Mark Available'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ManagerDashboard;
