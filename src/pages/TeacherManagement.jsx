import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../TeacherManagement.css';

import Header from '../components.jsx/header';
import Teachertable from '../components.jsx/Teachertable';
import Teachermodal from '../components.jsx/Teachermodal';
import { getAllTeachers, createTeacher, updateTeacher, deleteTeacher } from '../api/api';

function TeacherManagement() {
  const [teachers, setTeachers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTeacher, setSelectedTeacher] = useState(null);
  const navigate = useNavigate();

  // 📦 FETCH DATA
  useEffect(() => {
    const user = localStorage.getItem('user');
    if (!user) {
      navigate('/login');
      return;
    }
    fetchTeachers();
  }, []);

  const fetchTeachers = async () => {
    try {
      const response = await getAllTeachers();
      setTeachers(response.data);
    } catch (err) {
      console.error("Error fetching teachers:", err);
    }
  };

  // 🔍 FILTER
  const filteredTeachers = teachers.filter((t) =>
    (t.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.email?.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  // 💾 SAVE (ADD / EDIT)
  const handleSave = async (data) => {
    try {
      if (selectedTeacher) {
        // ✏️ UPDATE
        await updateTeacher(selectedTeacher.id, data);
      } else {
        // ➕ ADD
        await createTeacher(data);
      }
      fetchTeachers();
      setIsModalOpen(false);
      setSelectedTeacher(null);
    } catch (err) {
      console.error("Error saving teacher:", err);
      const errMsg = err.response?.data || "Failed to save teacher. Check backend.";
      alert(typeof errMsg === 'string' ? errMsg : "Failed to save teacher. Check backend.");
    }
  };

  // ❌ DELETE
  const handleDelete = async (id) => {
    const confirmDelete = window.confirm("Are you sure you want to delete this teacher?");
    
    if (confirmDelete) {
      try {
        await deleteTeacher(id);
        fetchTeachers();
      } catch (err) {
        console.error("Error deleting teacher:", err);
        alert("Failed to delete teacher.");
      }
    }
  };

  // ✏️ EDIT OPEN
  const handleEdit = (teacher) => {
    setSelectedTeacher(teacher);
    setIsModalOpen(true);
  };

  return (
    <div className="teacher-management">

      {/* 🔷 HEADER */}
      <Header 
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        onAdd={() => {
          setSelectedTeacher(null);
          setIsModalOpen(true);
        }}
      />

      {/* 🔷 TABLE */}
      <Teachertable 
        teachers={filteredTeachers}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />

      {/* 🔷 MODAL */}
      {isModalOpen && (
        <Teachermodal 
          teacher={selectedTeacher}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedTeacher(null);
          }}
          onSave={handleSave}
        />
      )}

    </div>
  );
}

export default TeacherManagement;
