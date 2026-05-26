import React, { useState, useEffect } from 'react';
import '../TeacherManagement.css';

function Roomform({ initialData, onSave, onCancel }) {
  const [formData, setFormData] = useState({
    roomNumber: '',
    capacity: '',
    roomCategory: '',
    roomType: 'CR' // Default as per enum
  });

  useEffect(() => {
    if (initialData) {
      setFormData({
        roomNumber: initialData.roomNumber || '',
        capacity: initialData.capacity || '',
        roomCategory: initialData.roomCategory || '',
        roomType: initialData.roomType || 'CR'
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ 
      ...prev, 
      [name]: name === 'capacity' ? parseInt(value) || 0 : value 
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Room Number</label>
        <input
          type="text"
          name="roomNumber"
          value={formData.roomNumber}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label>Capacity</label>
        <input
          type="number"
          name="capacity"
          value={formData.capacity}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label>Room Category</label>
        <input
          type="text"
          name="roomCategory"
          value={formData.roomCategory}
          onChange={handleChange}
          required
        />
      </div>

      <div className="form-group">
        <label>Room Type</label>
        <select 
          name="roomType" 
          value={formData.roomType} 
          onChange={handleChange}
          className="sel" 
          style={{ padding: '10px', borderRadius: '8px', border: '1px solid #bee6de' }}
        >
          <option value="CR">Classroom (CR)</option>
          <option value="LAB">Laboratory (LAB)</option>
          <option value="LT">Lecture Hall (LT)</option>
          <option value="NEW_AUDI">New Auditorium</option>
        </select>
      </div>

      <div className="modal-actions">
        <button type="button" className="cancel-btn" onClick={onCancel}>Cancel</button>
        <button type="submit" className="save-btn">Save</button>
      </div>
    </form>
  );
}

export default Roomform;
