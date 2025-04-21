# 🚀 Approval Workflow System

A full-stack approval workflow system designed for organizational hierarchies, where approval requests are automatically escalated through the management chain. Built with **React** for the frontend, **RESTful APIs** for the backend, and **MySQL** as the database.



## 📌 Problem Statement

In an organization, when an approval request is raised, it should follow the reporting hierarchy. If the immediate manager is not available, the request should escalate to the next-level manager automatically.



## 🛠️ Tech Stack

- **Frontend:** React.js
- **Backend:** RESTFUL API
- **Database:** MySQL
- **Authentication:** JWT TOken Authentication (Bearer Token)
- **API Integration:** Axios / Fetch API



## ✨ Features

### 👥 User Roles
- **Employee**: Can submit approval requests.
- **Manager**: Can approve/reject requests.
- **Admins/Superiors**: Can act on behalf of unavailable managers.

### 🔑 Authentication
- **Sign Up**: New users can register and define their role (employee/manager).
- **Login**: Role-based redirection to respective dashboards.

### 🖥️ Dashboards
- **Employee Dashboard**:
  - Submit a request
  - View request history and status

- **Manager Dashboard**:
  - View pending requests
  - Approve or reject requests
  - See escalated requests if a lower-level manager is unavailable
  
- **Admin Dashboard**:
  - View All users
  - View all Leave requests




### 🔔 Notifications
- Instant feedback to managers on new incoming requests
- Escalated request alerts

