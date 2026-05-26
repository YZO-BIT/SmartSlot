import React, { useState, useEffect } from 'react';
import '../TeacherManagement.css';
import { getAllSections, createTicket } from '../api/api';

function TicketModal({ onClose, onSave }) {
  const toLocalYYYYMMDD = (date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  const [formData, setFormData] = useState({
    requestedDate: toLocalYYYYMMDD(new Date()),
    requestedSlotId: 1,
    requestedSection: { id: '' },
    reason: 'Lunch Break Override',
    conflictDetails: ''
  });
  
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    const fetchSections = async () => {
      try {
        const response = await getAllSections();
        setSections(response.data || []);
        if (response.data && response.data.length > 0) {
          setFormData(prev => ({
            ...prev,
            requestedSection: { id: response.data[0].id }
          }));
        }
      } catch (err) {
        console.error("Error loading sections:", err);
        setError("Failed to load sections.");
      } finally {
        setLoading(false);
      }
    };
    fetchSections();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'sectionId') {
      setFormData(prev => ({ ...prev, requestedSection: { id: parseInt(value) } }));
    } else if (name === 'requestedSlotId') {
      setFormData(prev => ({ ...prev, requestedSlotId: parseInt(value) }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.requestedSection.id) {
      setError("Please select a section.");
      return;
    }
    setError('');
    setSubmitting(true);

    try {
      const payload = {
        teacher: { id: user.id },
        requestedDate: formData.requestedDate,
        requestedSlotId: formData.requestedSlotId,
        requestedSection: { id: formData.requestedSection.id },
        reason: formData.reason,
        conflictDetails: formData.conflictDetails
      };

      const res = await createTicket(payload);
      onSave(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to submit ticket request.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal" style={{ width: '450px', background: 'rgba(255, 255, 255, 0.95)', backdropFilter: 'blur(10px)', border: '1px solid rgba(255, 255, 255, 0.25)', boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.15)', borderRadius: '12px' }}>
        <h2 style={{ color: '#1e293b', marginBottom: '20px', fontWeight: '700' }}>🎫 Raise Oversight Ticket</h2>
        
        {loading ? (
          <div style={{ padding: '20px', textAlign: 'center' }}>Loading form options...</div>
        ) : (
          <form className="form" onSubmit={handleSubmit}>
            {error && (
              <div style={{ padding: '10px', background: '#fef2f2', color: '#991b1b', border: '1px solid #fee2e2', borderRadius: '6px', fontSize: '13px', marginBottom: '15px' }}>
                {error}
              </div>
            )}

            <div className="form-group">
              <label style={{ fontWeight: '600', color: '#475569' }}>Requested Date</label>
              <input 
                type="date" 
                name="requestedDate" 
                value={formData.requestedDate} 
                onChange={handleChange} 
                required 
                style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1' }}
              />
            </div>

            <div className="form-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '15px' }}>
              <div className="form-group">
                <label style={{ fontWeight: '600', color: '#475569' }}>Requested Slot</label>
                <select 
                  name="requestedSlotId" 
                  value={formData.requestedSlotId} 
                  onChange={handleChange} 
                  required 
                  className="sel"
                  style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1' }}
                >
                  {[...Array(10)].map((_, i) => (
                    <option key={i + 1} value={i + 1}>Slot {i + 1}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label style={{ fontWeight: '600', color: '#475569' }}>Section</label>
                <select 
                  name="sectionId" 
                  value={formData.requestedSection.id} 
                  onChange={handleChange} 
                  required 
                  className="sel"
                  style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1' }}
                >
                  <option value="">Select Section</option>
                  {sections.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
              </div>
            </div>

            <div className="form-group">
              <label style={{ fontWeight: '600', color: '#475569' }}>Request Reason</label>
              <select 
                name="reason" 
                value={formData.reason} 
                onChange={handleChange} 
                required 
                className="sel"
                style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1' }}
              >
                <option value="Lunch Break Override">Lunch Break Override</option>
                <option value="Consecutive Lecture Override">Consecutive Lecture Override</option>
                <option value="Subject Lecture Limit Override">Subject Lecture Limit Override</option>
                <option value="Room Type Eligibility Override">Room Type Eligibility Override</option>
                <option value="Urgent Slot Exchange">Urgent Slot Exchange</option>
                <option value="Other Policy Exception">Other Policy Exception</option>
              </select>
            </div>

            <div className="form-group">
              <label style={{ fontWeight: '600', color: '#475569' }}>Conflict / Request Details</label>
              <textarea 
                name="conflictDetails" 
                placeholder="Explain the conflict or reason for override here..."
                value={formData.conflictDetails} 
                onChange={handleChange} 
                rows="3"
                required
                style={{ width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontFamily: 'inherit', resize: 'vertical' }}
              />
            </div>

            <div className="modal-actions" style={{ marginTop: '20px', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button type="button" className="cancel-btn" onClick={onClose} disabled={submitting} style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #cbd5e1', cursor: 'pointer', background: 'transparent' }}>
                Cancel
              </button>
              <button type="submit" className="save-btn" disabled={submitting} style={{ background: '#f59e0b', color: 'white', border: 'none', padding: '8px 20px', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' }}>
                {submitting ? "Submitting..." : "Submit Ticket"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}

export default TicketModal;
