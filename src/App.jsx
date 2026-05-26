import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/login';
import TeacherManagement from './pages/TeacherManagement';
import RoomManagement from './pages/RoomManagement';
import SectionManagement from './pages/SectionManagement';
import BookingManagement from './pages/BookingManagement';
import Dashboard from './pages/Dashboard';
import ApprovalCenter from './pages/ApprovalCenter';
import SubjectManagement from './pages/SubjectManagement';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/teachers" element={<TeacherManagement />} />
        <Route path="/subjects" element={<SubjectManagement />} />
        <Route path="/rooms" element={<RoomManagement />} />
        <Route path="/sections" element={<SectionManagement />} />
        <Route path="/bookings" element={<BookingManagement />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/admin" element={<Dashboard />} />
        <Route path="/approvals" element={<ApprovalCenter />} />
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;