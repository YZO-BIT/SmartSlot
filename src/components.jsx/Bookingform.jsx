import React, { useState, useEffect } from 'react';
import '../TeacherManagement.css';
import {
  getAllTeachers, getAllRooms, getAllSections, getAllSubjects,
  getTeacherWorkload, getEligibleRooms
} from '../api/api';

function Bookingform({ initialData, onSave, onCancel, prefilledDay, prefilledSlot }) {
  const [formData, setFormData] = useState({
    teacher: { id: '' },
    room: { id: '' },
    section: { id: '' },
    sections: [],
    subject: { id: '' },
    slotId: prefilledSlot || 1,
    bookingDate: new Date().toISOString().split('T')[0]
  });

  const [teachers, setTeachers] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [sections, setSections] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [fullWorkload, setFullWorkload] = useState([]); // Array of assignments
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const userRole = user.role;
  const currentTeacherId = user.id;

  // 🗓️ Helper to calculate next occurrence of a day (MON, TUE etc)
  const getNextDateForDay = (dayName) => {
    if (!dayName) return new Date().toISOString().split('T')[0];
    const dayMap = { "MON": 1, "TUE": 2, "WED": 3, "THU": 4, "FRI": 5, "SAT": 6, "SUN": 0 };
    const targetDay = dayMap[dayName];
    const now = new Date();
    const currentDay = now.getDay();

    let diff = targetDay - currentDay;
    if (diff < 0) diff += 7;

    const result = new Date(now);
    result.setDate(now.getDate() + diff);
    return result.toISOString().split('T')[0];
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        if (userRole === 'TEACHER' && currentTeacherId) {
          const [wRes, rRes] = await Promise.all([
            getTeacherWorkload(currentTeacherId),
            getEligibleRooms(currentTeacherId)
          ]);

          const workload = wRes.data || [];
          setFullWorkload(workload);
          setRooms(rRes.data || []);

          // Extract unique sections from grouped assignments (sections is now an array)
          const uniqueSections = [];
          const seenSec = new Set();
          workload.forEach(asgn => {
            // Support both new shape (asgn.sections[]) and legacy (asgn.section)
            const secList = asgn.sections && asgn.sections.length > 0
              ? asgn.sections
              : asgn.section
                ? [asgn.section]
                : [];
            secList.forEach(sec => {
              if (sec && !seenSec.has(sec.id)) {
                uniqueSections.push(sec);
                seenSec.add(sec.id);
              }
            });
          });
          setSections(uniqueSections);
          filterSubjects(workload, formData.section?.id);
          setFormData(prev => ({ ...prev, teacher: { id: currentTeacherId } }));
        } else {
          const [tRes, rRes, sRes, subRes] = await Promise.all([
            getAllTeachers(),
            getAllRooms(),
            getAllSections(),
            getAllSubjects()
          ]);
          setTeachers(tRes.data || []);
          setRooms(rRes.data || []);
          setSections(sRes.data || []);
          setSubjects(subRes.data || []);
        }
      } catch (err) {
        console.error("Error loading for booking form:", err);
      } finally {
        setLoading(false);
      }
    };

    loadData();

    if (initialData) {
      setFormData({
        teacher: initialData.teacher || { id: '' },
        room: initialData.room || { id: '' },
        section: initialData.section || { id: '' },
        sections: initialData.sections || (initialData.section ? [initialData.section] : []),
        subject: initialData.subject || { id: '' },
        slotId: initialData.slotId || 1,
        bookingDate: initialData.bookingDate || new Date().toISOString().split('T')[0]
      });
    } else if (prefilledDay) {
      setFormData(prev => ({
        ...prev,
        slotId: prefilledSlot,
        bookingDate: getNextDateForDay(prefilledDay)
      }));
    }
  }, [initialData, userRole, currentTeacherId, prefilledDay, prefilledSlot]);

  const filterSubjects = (workload, sectionId) => {
    if (!sectionId) {
      setSubjects([]);
      return;
    }
    // Find assignments whose sections set includes this sectionId
    const relevantSubjects = workload
      .filter(asgn => {
        const secList = asgn.sections && asgn.sections.length > 0
          ? asgn.sections
          : asgn.section
            ? [asgn.section]
            : [];
        return secList.some(s => s.id === parseInt(sectionId));
      })
      .map(asgn => asgn.subject);

    const uniqueSubs = [];
    const seenSub = new Set();
    relevantSubjects.forEach(s => {
      if (s && !seenSub.has(s.id)) {
        uniqueSubs.push(s);
        seenSub.add(s.id);
      }
    });
    setSubjects(uniqueSubs);
  };

  const selectedSectionsList = React.useMemo(() => {
    const list = userRole === 'TEACHER' ? (formData.sections || []) : (formData.section?.id ? [formData.section] : []);
    return list.map(sec => {
      const found = sections.find(s => s.id === parseInt(sec.id));
      return found || sec;
    });
  }, [formData.sections, formData.section, userRole, sections]);

  const totalStudents = React.useMemo(() => {
    return selectedSectionsList.reduce((sum, sec) => sum + (sec.studentCount || 50), 0);
  }, [selectedSectionsList]);

  const selectedSubject = React.useMemo(() => {
    const subId = formData.subject?.id;
    if (!subId) return null;

    // Look in subjects array first
    let found = subjects.find(s => s.id === parseInt(subId));
    if (!found && userRole === 'TEACHER') {
      // Look in fullWorkload
      const asgn = fullWorkload.find(a => a.subject?.id === parseInt(subId));
      if (asgn) found = asgn.subject;
    }
    return found;
  }, [formData.subject, subjects, fullWorkload, userRole]);

  const isLabSubject = selectedSubject?.roomTypeRequirement === 'LAB';

  const filteredRooms = React.useMemo(() => {
    return rooms.filter(room => {
      // 1. If it's a LAB subject, only show LAB rooms. Otherwise, do NOT show LAB rooms.
      if (isLabSubject) {
        if (room.roomType !== 'LAB') return false;
      } else {
        if (room.roomType === 'LAB') return false;
      }

      // 1.5. Enforce minimum section constraints for specialized large rooms
      if (userRole === 'TEACHER') {
        if (room.roomType === 'NEW_AUDI' && selectedSectionsList.length < 4) {
          return false;
        }
        if (room.roomType === 'LT' && selectedSectionsList.length < 2) {
          return false;
        }
      }

      // 2. Enforce capacity check
      if (totalStudents > 0) {
        const hasCapacity = room.capacity >= totalStudents;
        if (selectedSectionsList.length > 1) {
          const isEligibleType = room.roomType === 'CR' || room.roomType === 'LT' || room.roomType === 'NEW_AUDI';
          if (isLabSubject) {
            return hasCapacity; // must be LAB and have capacity
          }
          return isEligibleType && hasCapacity;
        }
        return hasCapacity;
      }
      return true;
    });
  }, [rooms, totalStudents, selectedSectionsList, isLabSubject, userRole]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'teacherId') {
      setFormData(prev => ({ ...prev, teacher: { id: value ? parseInt(value) : '' } }));
    } else if (name === 'roomId') {
      setFormData(prev => ({ ...prev, room: { id: value ? parseInt(value) : '' } }));
    } else if (name === 'sectionId') {
      const secId = value ? parseInt(value) : null;
      setFormData(prev => ({ ...prev, section: { id: secId }, subject: { id: '' } }));
      if (userRole === 'TEACHER') {
        filterSubjects(fullWorkload, secId);
      }
    } else if (name === 'subjectId') {
      setFormData(prev => ({ ...prev, subject: { id: value ? parseInt(value) : '' } }));
    } else if (name === 'slotId') {
      setFormData(prev => ({ ...prev, [name]: parseInt(value) || 1 }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMsg("");

    if (isLabSubject && formData.slotId % 2 === 0) {
      setErrorMsg("Lab sessions must start on an odd slot (1, 3, 5, 7, 9) for a 2-hour continuous block. Please select an odd slot.");
      return;
    }

    onSave(formData);
  };

  if (loading) return <div style={{ padding: '20px', textAlign: 'center' }}>Loading selections...</div>;

  // Force grid selection for teachers if no pre-filled data exists
  if (userRole === 'TEACHER' && !prefilledDay && !initialData) {
    return (
      <div style={{ padding: '30px', textAlign: 'center', background: 'white', borderRadius: '12px' }}>
        <div style={{ fontSize: '40px', marginBottom: '15px' }}>📅</div>
        <h3 style={{ color: '#1e293b', marginBottom: '10px' }}>Select Slot from Grid</h3>
        <p style={{ color: '#64748b', fontSize: '14px', lineHeight: '1.6' }}>
          Please close this window and click on an available <strong>[+]</strong> slot in the timetable grid to book your lecture instantly.
        </p>
        <button
          onClick={onCancel}
          style={{ marginTop: '20px', padding: '10px 25px', background: '#3b82f6', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' }}
        >
          Got it
        </button>
      </div>
    );
  }

  return (
    <form className="form" onSubmit={handleSubmit} style={{ minWidth: '350px' }}>
      {errorMsg && (
        <div style={{ color: '#dc2626', background: '#fef2f2', border: '1px solid #fee2e2', padding: '10px', borderRadius: '6px', fontSize: '12px', marginBottom: '15px', fontWeight: '600' }}>
          {errorMsg}
        </div>
      )}

      {prefilledDay && (
        <div style={{ marginBottom: '20px', padding: '10px', background: '#eff6ff', borderRadius: '8px', border: '1px solid #bfdbfe' }}>
          <div style={{ fontSize: '12px', color: '#3b82f6', fontWeight: 'bold' }}>QUICK BOOKING</div>
          <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#1e3a8a' }}>{prefilledDay} (Slot {prefilledSlot})</div>
          <div style={{ fontSize: '11px', color: '#60a5fa' }}>Setting date to: {formData.bookingDate}</div>
        </div>
      )}

      {/* TEACHER */}
      <div className="form-group">
        <label>Professor</label>
        {userRole === 'TEACHER' ? (
          <div style={{ padding: '10px', background: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0', fontSize: '14px' }}>
            {user.name} (You)
          </div>
        ) : (
          <select name="teacherId" value={formData?.teacher?.id || ""} onChange={handleChange} required className="sel">
            <option value="">Select Teacher</option>
            {teachers.map(t => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>
        )}
      </div>

      <div className="form-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
        {/* Section & Subject selection */}
        {userRole === 'TEACHER' ? (
          <div className="form-group">
            <label>Subject - Section</label>
            <select
              name="subjectSection"
              value={formData.subject?.id && formData.sections && formData.sections.length > 0 ? `${formData.subject.id}:${formData.sections.map(s => s.id).join(",")}` : ''}
              onChange={(e) => {
                const val = e.target.value;
                if (!val) {
                  setFormData(prev => ({
                    ...prev,
                    subject: { id: '' },
                    section: { id: '' },
                    sections: []
                  }));
                  return;
                }
                const [subId, secIdsStr] = val.split(':');
                const secIds = secIdsStr ? secIdsStr.split(',').map(id => parseInt(id)) : [];

                // Map each secId to its full section object from the sections state array
                const fullSelectedSections = secIds.map(id => {
                  const found = sections.find(s => s.id === id);
                  return found || { id };
                });

                setFormData(prev => ({
                  ...prev,
                  subject: { id: parseInt(subId) },
                  section: { id: secIds[0] || '' }, // fallback for legacy fields
                  sections: fullSelectedSections
                }));
              }}
              required
              className="sel"
            >
              <option value="">Select Subject & Section</option>
              {fullWorkload.map(asgn => {
                const secs = asgn.sections && asgn.sections.length > 0
                  ? asgn.sections
                  : asgn.section
                    ? [asgn.section]
                    : [];
                if (secs.length === 0) return null;

                const secNames = secs.map(s => s.name).join(" + ");
                const secIds = secs.map(s => s.id).join(",");

                return (
                  <option key={`${asgn.subject.id}-${secIds}`} value={`${asgn.subject.id}:${secIds}`}>
                    {asgn.subject.name} ({secNames})
                  </option>
                );
              })}
            </select>
          </div>
        ) : (
          <>
            <div className="form-group">
              <label>Section</label>
              <select name="sectionId" value={formData.section?.id || ''} onChange={handleChange} required className="sel">
                <option value="">Select Section</option>
                {sections.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Subject</label>
              <select name="subjectId" value={formData?.subject?.id || ""} onChange={handleChange} required className="sel" disabled={!formData.section?.id && userRole === 'TEACHER'}>
                <option value="">Select Subject</option>
                {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
          </>
        )}
      </div>

      <div className="form-group">
        <label>Room / Facility</label>
        <select name="roomId" value={formData.room.id} onChange={handleChange} required className="sel">
          <option value="">Select Room</option>
          {filteredRooms.map(r => (
            <option key={r.id} value={r.id}>
              {r.roomNumber} ({r.roomType} - Cap: {r.capacity})
            </option>
          ))}
        </select>
        {totalStudents > 0 && (
          <p className="hint" style={{ fontSize: '11px', marginTop: '4px', color: '#059669', fontWeight: '500' }}>
            Combined strength: {totalStudents} students. Showing compatible rooms.
          </p>
        )}
        {totalStudents > 0 && filteredRooms.length === 0 && (
          <p className="error" style={{ fontSize: '11px', color: '#dc2626', marginTop: '4px', fontWeight: '600' }}>
            ⚠️ No rooms available with capacity ≥ {totalStudents}
          </p>
        )}
      </div>

      {!prefilledDay && (
        <div className="form-row">
          <div className="form-group half">
            <label>Slot ID (1-10)</label>
            <input type="number" name="slotId" min="1" max="10" value={formData.slotId} onChange={handleChange} required />
          </div>
          <div className="form-group half">
            <label>Date</label>
            <input type="date" name="bookingDate" value={formData.bookingDate} onChange={handleChange} required />
          </div>
        </div>
      )}

      <div className="modal-actions" style={{ marginTop: '20px' }}>
        <button type="button" className="cancel-btn" onClick={onCancel}>Cancel</button>
        <button type="submit" className="save-btn" style={{ background: '#3b82f6' }}>Confirm Slot</button>
      </div>
    </form>
  );
}

export default Bookingform;
