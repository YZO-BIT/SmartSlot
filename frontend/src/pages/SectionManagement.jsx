import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../TeacherManagement.css';

import Header from '../components.jsx/header';
import Sectiontable from '../components.jsx/Sectiontable';
import Sectionmodal from '../components.jsx/Sectionmodal';
import { getAllSections, createSection, updateSection, deleteSection, getAllTeachers, getAllWorkloads, getTeacherWorkload } from '../api/api';

function SectionManagement() {
  const [sections, setSections] = useState([]);
  const [teachers, setTeachers] = useState([]); 
  const [assignments, setAssignments] = useState([]); // ➕ Added
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSection, setSelectedSection] = useState(null);
  const navigate = useNavigate();

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isTeacher = user.role === 'TEACHER';

  // 📦 FETCH DATA
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      navigate('/login');
      return;
    }
    fetchSections();
    fetchTeachers(); 
    fetchAssignments(); // ➕ Added
  }, []);

  const fetchSections = async () => {
    try {
      const response = await getAllSections();
      let allSecs = Array.isArray(response.data) ? response.data : [];
      if (isTeacher && user.id) {
        const wlRes = await getTeacherWorkload(user.id);
        const wl = wlRes.data || [];
        const allowedSecIds = new Set();
        wl.forEach(asgn => {
          const secs = asgn.sections && asgn.sections.length ? asgn.sections : (asgn.section ? [asgn.section] : []);
          secs.forEach(s => allowedSecIds.add(s.id));
        });
        allSecs = allSecs.filter(s => allowedSecIds.has(s.id));
      }
      setSections(allSecs);
    } catch (err) {
      console.error("Error fetching sections:", err);
      setSections([]);
    }
  };

  const fetchTeachers = async () => {
    try {
      const response = await getAllTeachers();
      setTeachers(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Error fetching teachers for section audit:", err);
    }
  };

  const fetchAssignments = async () => {
    try {
      const response = await getAllWorkloads();
      setAssignments(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Error fetching global assignments:", err);
    }
  };

  // 🔍 FILTER
  const filteredSections = (sections || []).filter((s) =>
    (s?.name?.toLowerCase()?.includes(searchTerm.toLowerCase()))
  );

  // 💾 SAVE (ADD / EDIT)
  const handleSave = async (data) => {
    try {
      if (selectedSection) {
        // ✏️ UPDATE
        await updateSection(selectedSection.id, data);
      } else {
        // ➕ ADD
        await createSection(data);
      }
      fetchSections();
      setIsModalOpen(false);
      setSelectedSection(null);
    } catch (err) {
      console.error("Error saving section:", err);
      alert("Failed to save section.");
    }
  };

  // ❌ DELETE
  const handleDelete = async (id) => {
    const confirmDelete = window.confirm("Are you sure you want to delete this section?");
    
    if (confirmDelete) {
      try {
        await deleteSection(id);
        fetchSections();
      } catch (err) {
        console.error("Error deleting section:", err);
        alert("Failed to delete section.");
      }
    }
  };

  // ✏️ EDIT OPEN
  const handleEdit = (section) => {
    setSelectedSection(section);
    setIsModalOpen(true);
  };

  return (
    <div className="teacher-management">

      {/* 🔷 HEADER */}
      <Header 
        title="Section Management"
        subTitle="Manage academic segments and students"
        searchPlaceholder="Search section..."
        addLabel="Add Section"
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        onAdd={() => {
          setSelectedSection(null);
          setIsModalOpen(true);
        }}
      />

      {/* 🔷 TABLE */}
      {sections.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
          {sections.length === 0 ? "Loading sections or no sections found..." : "No sections found."}
        </div>
      ) : (
        <Sectiontable 
          sections={filteredSections}
          teachers={teachers}
          assignments={assignments} // ➕ Added
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      )}

      {/* 🔷 MODAL */}
      {isModalOpen && (
        <Sectionmodal 
          section={selectedSection}
          allTeachers={teachers}
          allAssignments={assignments} // ➕ Added
          onClose={() => {
            setIsModalOpen(false);
            setSelectedSection(null);
          }}
          onSave={handleSave}
        />
      )}

    </div>
  );
}

export default SectionManagement;
