import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../components.jsx/header';
import Subjectform from '../components.jsx/Subjectform';
import { api } from '../api/api'; 
import '../TeacherManagement.css'; 

function SubjectManagement() {
  const [subjects, setSubjects] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [selectedSubject, setSelectedSubject] = useState(null);
  const navigate = useNavigate();

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isTeacher = user.role === 'TEACHER';

  useEffect(() => {
    fetchSubjects();
  }, []);

  const fetchSubjects = async () => {
    setLoading(true);
    try {
      const res = await api.get('/subjects');
      setSubjects(res.data || []);
    } catch (err) {
      console.error("Error fetching subjects:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (formData) => {
    try {
      if (selectedSubject) {
        await api.put(`/subjects/${selectedSubject.id}`, formData);
      } else {
        await api.post('/subjects', formData);
      }
      fetchSubjects();
      setIsModalOpen(false);
      setSelectedSubject(null);
    } catch (err) {
      alert("Error saving subject: " + (err.response?.data?.message || err.message));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this subject?")) return;
    try {
      await api.delete(`/subjects/${id}`);
      fetchSubjects();
    } catch (err) {
      alert("Error deleting subject");
    }
  };

  const openEdit = (sub) => {
    setSelectedSubject(sub);
    setIsModalOpen(true);
  };

  const filtered = subjects.filter(s => s.name.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="teacher-management">
      <Header 
        title="Subject Management"
        subTitle="Configure curriculum requirements and capacity rules"
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        onAdd={() => { setSelectedSubject(null); setIsModalOpen(true); }}
        addLabel="Add Subject"
        searchPlaceholder="Filter subjects..."
      />

      <div className="content">
        <div className="table-content ss-card" style={{ margin: '0', background: 'white' }}>
          <div className="table-header" style={{ gridTemplateColumns: isTeacher ? '2fr 1fr 1fr 1fr' : '2fr 1fr 1fr 1fr 1fr', background: 'linear-gradient(135deg, #1e293b, #334155)', color: 'white' }}>
            <div>Subject Name</div>
            <div>Requirement</div>
            <div>Lectures/Week</div>
            <div>Type</div>
            {!isTeacher && <div>Actions</div>}
          </div>
          
          <div className="table-body">
            {loading ? (
              <p className="no-data">Loading curriculum...</p>
            ) : filtered.length === 0 ? (
              <p className="no-data">No subjects found.</p>
            ) : filtered.map(s => (
              <div key={s.id} className="row" style={{ gridTemplateColumns: isTeacher ? '2fr 1fr 1fr 1fr' : '2fr 1fr 1fr 1fr 1fr' }}>
                <div style={{ fontWeight: '600', color: '#1e293b' }}>{s.name}</div>
                <div>
                  <span className="ss-chip" style={{ background: '#e0f2fe', color: '#0369a1' }}>
                    {s.roomTypeRequirement}
                  </span>
                </div>
                <div>{s.lecturesPerWeek}</div>
                <div>
                  <span className={`ss-chip ${s.isElective ? 'elective' : ''}`} style={{ background: s.isElective ? '#fef3c7' : '#f0fdf4', color: s.isElective ? '#92400e' : '#166534' }}>
                    {s.isElective ? "Elective" : "Regular"}
                  </span>
                </div>
                {!isTeacher && (
                  <div className="actions">
                    <button onClick={() => openEdit(s)} className="edit-btn">Edit</button>
                    <button onClick={() => handleDelete(s.id)} className="delete-btn">Delete</button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal" style={{ width: '500px' }}>
            <h2>{selectedSubject ? "Edit Subject" : "Register New Subject"}</h2>
            <Subjectform 
              initialData={selectedSubject}
              onSave={handleSave}
              onCancel={() => { setIsModalOpen(false); setSelectedSubject(null); }}
            />
          </div>
        </div>
      )}
    </div>
  );
}

export default SubjectManagement;
