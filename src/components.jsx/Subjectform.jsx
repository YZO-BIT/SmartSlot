import React, { useState, useEffect } from 'react';
import '../TeacherManagement.css';

function Subjectform({ initialData, onSave, onCancel }) {
  const [formData, setFormData] = useState({
    name: '',
    roomTypeRequirement: 'CR',
    lecturesPerWeek: 3,
    isElective: false
  });

  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name || '',
        roomTypeRequirement: initialData.roomTypeRequirement || 'CR',
        lecturesPerWeek: initialData.lecturesPerWeek || 3,
        isElective: initialData.isElective || false
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({ 
      ...prev, 
      [name]: type === 'checkbox' ? checked : (name === 'lecturesPerWeek' ? parseInt(value) || 0 : value)
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="teacher-enrollment-form">
      <form onSubmit={handleSubmit}>
        <div className="form-section">
          <h3>Subject Specification</h3>
          <div className="form-group">
            <label>Subject Name</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g. Data Structures"
              required
            />
          </div>

          <div className="form-grid" style={{ marginTop: '15px' }}>
            <div className="form-group">
              <label>Room Requirement</label>
              <select 
                name="roomTypeRequirement"
                value={formData.roomTypeRequirement} 
                onChange={handleChange}
                className="ss-input"
              >
                <option value="CR">Classroom (CR)</option>
                <option value="LAB">Laboratory (LAB)</option>
                <option value="LT">Lecture Theater (LT)</option>
                <option value="NEW_AUDI">Auditorium</option>
              </select>
            </div>
            <div className="form-group">
              <label>Lectures / Week</label>
              <input
                type="number"
                name="lecturesPerWeek"
                value={formData.lecturesPerWeek}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group" style={{ marginTop: '15px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
              <input 
                type="checkbox"
                name="isElective"
                checked={formData.isElective}
                onChange={handleChange}
              />
              <span style={{ fontSize: '14px' }}>This is an Elective Subject</span>
            </label>
          </div>
        </div>

        <div className="modal-actions" style={{ marginTop: '20px' }}>
          <button type="button" className="cancel-btn" onClick={onCancel}>Cancel</button>
          <button type="submit" className="save-btn">
            {initialData ? 'Update Subject' : 'Add Subject'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default Subjectform;
