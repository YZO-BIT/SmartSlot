import React, { useEffect, useState } from "react";
import { getDashboardStats, getTeacherStats } from "../api/api";

const QuickStats = ({ userRole, userId, refreshKey }) => {
  const isTeacher = userRole === 'TEACHER';
  const [stats, setStats] = useState({
    teachers: 0,
    rooms: 0,
    sections: 0,
    bookings: 0,
    // Teacher specific
    todayLectures: 0,
    totalLectures: 0,
    pendingRequests: 0,
    department: 'N/A'
  });

  useEffect(() => {
    if (isTeacher && userId) {
      getTeacherStats(userId)
        .then((res) => {
          setStats(prev => ({...prev, ...res.data}));
        })
        .catch((err) => console.error("Error fetching teacher stats:", err));
    } else {
      getDashboardStats()
        .then((res) => {
          setStats(prev => ({...prev, ...res.data}));
        })
        .catch((err) => console.error("Error fetching admin stats:", err));
    }
  }, [isTeacher, userId, refreshKey]);

  if (isTeacher) {
    return (
      <div className="quick-stats">
        <div className="stat-card">
          <span className="stat-label">Today's Lectures</span>
          <span className="stat-value">{stats.todayLectures}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Total Bookings</span>
          <span className="stat-value">{stats.totalLectures}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Pending Requests</span>
          <span className="stat-value">{stats.pendingRequests}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Department</span>
          <span className="stat-value" style={{fontSize: '1.2rem'}}>{stats.department}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="quick-stats">
      <div className="stat-card">
        <span className="stat-label">Teachers</span>
        <span className="stat-value">{stats.teachers}</span>
      </div>
      <div className="stat-card">
        <span className="stat-label">Rooms</span>
        <span className="stat-value">{stats.rooms}</span>
      </div>
      <div className="stat-card">
        <span className="stat-label">Sections</span>
        <span className="stat-value">{stats.sections}</span>
      </div>
      <div className="stat-card">
        <span className="stat-label">Bookings</span>
        <span className="stat-value">{stats.bookings}</span>
      </div>
    </div>
  );
};

export default QuickStats;
