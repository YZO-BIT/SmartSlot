import React, { useState, useEffect } from 'react';
import { getAllSubjects } from '../api/api';
import '../TeacherManagement.css';

function Sectionform({ initialData, onSave, onCancel, allAssignments, allTeachers }) {
  const [formData, setFormData] = useState({
    name: '',
    batchYear: new Date().getFullYear(),
    studentCount: 50,
    subjectIds: [] // Store IDs for saving
  });

  // 👩‍🏫 Find teachers for this section
  const sectionTeachers = (allAssignments || [])
    .filter(a => a.section?.id === initialData?.id)
    .map(a => a.teacher?.name || allTeachers?.find(t => t.id === a.teacher?.id)?.name)
    .filter((name, index, self) => name && self.indexOf(name) === index);

  const [availableSubjects, setAvailableSubjects] = useState([]);

  useEffect(() => {
    // Load subjects for selection
    const fetchSubs = async () => {
      try {
        const res = await getAllSubjects();
        setAvailableSubjects(res.data || []);
      } catch (err) {
        console.error("Error loading subjects in section form:", err);
      }
    };
    fetchSubs();

    if (initialData) {
      setFormData({
        name: initialData.name || '',
        batchYear: initialData.batchYear || new Date().getFullYear(),
        studentCount: initialData.studentCount || 50,
        subjectIds: initialData.subjects ? initialData.subjects.map(s => s.id) : []
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ 
      ...prev, 
      [name]: (name === 'batchYear' || name === 'studentCount') ? parseInt(value) || 0 : value 
    }));
  };

  const handleSubjectToggle = (id) => {
    const updated = formData.subjectIds.includes(id)
      ? formData.subjectIds.filter(sid => sid !== id)
      : [...formData.subjectIds, id];
    setFormData({ ...formData, subjectIds: updated });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="teacher-enrollment-form" style={{ maxHeight: '75vh', overflowY: 'auto' }}>
      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-section">
            <h3>Basic Info</h3>
            <div className="form-group">
              <label>Section Name</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder="e.g. CSE-A"
                required
              />
            </div>
            <div className="form-group">
              <label>Batch Year</label>
              <input
                type="number"
                name="batchYear"
                value={formData.batchYear}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Student Count</label>
              <input
                type="number"
                name="studentCount"
                value={formData.studentCount}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-section">
            <h3>Assigned Teachers</h3>
            <p className="sub" style={{ marginBottom: '10px' }}>Faculty currently mapped to this section.</p>
            <div className="checkbox-list" style={{ maxHeight: '180px', overflowY: 'auto' }}>
              {sectionTeachers.length > 0 ? (
                sectionTeachers.map((t, idx) => (
                  <div key={idx} style={{ padding: '8px', background: '#f8fafc', borderRadius: '6px', marginBottom: '5px', fontSize: '13px', border: '1px solid #e2e8f0', color: '#334155' }}>
                    👤 {t}
                  </div>
                ))
              ) : (
                <p style={{ fontSize: '12px', color: '#94a3b8', fontStyle: 'italic' }}>No teachers assigned yet.</p>
              )}
            </div>
            
            <div style={{ marginTop: '20px' }}>
              <h3>Subject Mapping</h3>
              <p className="sub" style={{ marginBottom: '10px' }}>Select subjects for this section.</p>
              <div className="checkbox-list" style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '8px', maxHeight: '180px', overflowY: 'auto', padding: '5px' }}>
              {availableSubjects.map(sub => (
                <label key={sub.id} style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '14px', cursor: 'pointer' }}>
                  <input 
                    type="checkbox"
                    checked={formData.subjectIds.includes(sub.id)}
                    onChange={() => handleSubjectToggle(sub.id)}
                  />
                  {sub.name} <span style={{ fontSize: '11px', color: '#64748b' }}>({sub.roomTypeRequirement})</span>
                </label>
              ))}
            </div>
          </div>
        </div>
        </div> {/* Closes form-grid */}

        <div className="modal-actions" style={{ marginTop: '20px' }}>
          <button type="button" className="cancel-btn" onClick={onCancel}>Cancel</button>
          <button type="submit" className="save-btn">
            {initialData ? 'Update Section' : 'Create Section'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default Sectionform;
