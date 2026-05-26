import React, { useState, useEffect } from 'react';
import { getAllSections, getAllSubjects, getTeacherWorkload } from '../api/api';


const roomTypeOptions = [
  { value: "CR", label: "Classroom (CR)" },
  { value: "LAB", label: "Laboratory (LAB)" },
  { value: "LT", label: "Lecture Hall (LT)" },
  { value: "NEW_AUDI", label: "New Auditorium" }
];

function Teacherform({ teacher, onSave, onClose }) {

  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    phone: '',
    department: '',
    password: '',
    eligibleRoomTypes: [],
    assignments: [] // Each: { sectionIds: [], subjectId: '' }
  });

  const [sections, setSections] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [errors, setErrors] = useState({});

  // 📦 FETCH DROPDOWNS
  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        const [secRes, subRes] = await Promise.all([getAllSections(), getAllSubjects()]);
        setSections(secRes.data);
        setSubjects(subRes.data);
      } catch (err) {
        console.error("Error fetching metadata:", err);
      }
    };
    fetchMetadata();
  }, []);

  // 🧠 Prefill edit — normalize legacy sectionId → sectionIds
  useEffect(() => {
    const loadTeacherData = async () => {
      if (teacher) {
        let workload = [];
        try {
          const wlRes = await getTeacherWorkload(teacher.id);
          workload = wlRes.data || [];
        } catch (err) {
          console.error("Error fetching teacher workload:", err);
        }

        const normalizedAssignments = workload.map(a => ({
          // Support both old shape {sectionId} and new shape {sectionIds}
          sectionIds: a.sectionIds
            ? a.sectionIds.map(String)
            : a.sections
              ? a.sections.map(s => String(s.id))
              : a.sectionId
                ? [String(a.sectionId)]
                : [],
          subjectId: a.subjectId ? String(a.subjectId) : (a.subject ? String(a.subject.id) : '')
        }));

        setFormData({
          ...teacher,
          eligibleRoomTypes: teacher.eligibleRoomTypes || [],
          assignments: normalizedAssignments,
          password: ''
        });
      } else {
        setFormData({
          name: '',
          username: '',
          email: '',
          phone: '',
          department: '',
          password: '',
          eligibleRoomTypes: [],
          assignments: []
        });
      }
    };
    loadTeacherData();
  }, [teacher]);


  // 📝 Input change
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  // ✅ Checkbox toggle (Room Types)
  const handleRoomTypeToggle = (value) => {
    const updated = formData.eligibleRoomTypes.includes(value)
      ? formData.eligibleRoomTypes.filter(item => item !== value)
      : [...formData.eligibleRoomTypes, value];
    setFormData({ ...formData, eligibleRoomTypes: updated });
  };

  // ➕ ASSIGNMENT LOGIC
  const addAssignment = () => {
    setFormData({
      ...formData,
      assignments: [...formData.assignments, { sectionIds: [], subjectId: '' }]
    });
  };

  const updateAssignmentSubject = (index, value) => {
    const updated = [...formData.assignments];
    updated[index].subjectId = value;
    setFormData({ ...formData, assignments: updated });
  };

  /**
   * Toggle a section in the grouped sections list for a given assignment row.
   * Checking a section adds it; unchecking removes it.
   */
  const toggleSectionInAssignment = (index, sectionId) => {
    const updated = [...formData.assignments];
    const current = updated[index].sectionIds || [];
    const sid = String(sectionId);
    updated[index].sectionIds = current.includes(sid)
      ? current.filter(id => id !== sid)
      : [...current, sid];
    setFormData({ ...formData, assignments: updated });
  };

  const removeAssignment = (index) => {
    const updated = formData.assignments.filter((_, i) => i !== index);
    setFormData({ ...formData, assignments: updated });
  };

  // 🚨 Validation
  const validate = () => {
    let newErrors = {};
    if (!formData.name.trim()) newErrors.name = "Name is required";
    if (!formData.username.trim()) newErrors.username = "Username is required";
    if (!formData.email.includes("@")) newErrors.email = "Invalid email";
    if (formData.phone.length < 10) newErrors.phone = "Invalid phone";
    if (!formData.department.trim()) newErrors.department = "Department is required";
    if (!teacher && !formData.password.trim()) newErrors.password = "Initial password is required";

    // Validate assignments: each must have at least 1 section and a subject
    formData.assignments.forEach((asgn, i) => {
      if (!asgn.sectionIds || asgn.sectionIds.length === 0)
        newErrors[`asgn_sec_${i}`] = "Select at least one section";
      if (!asgn.subjectId)
        newErrors[`asgn_sub_${i}`] = "Select a subject";
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 💾 Submit — convert sectionIds to Long array for the backend
  const handleSubmit = () => {
    if (validate()) {
      const payload = {
        ...formData,
        assignments: formData.assignments.map(a => ({
          sectionIds: (a.sectionIds || []).map(Number),
          subjectId: Number(a.subjectId)
        }))
      };
      
      // Sanitize expertise to match Set<String> expected by backend TeacherDTO
      if (payload.expertise) {
        payload.expertise = payload.expertise.map(e => (e && typeof e === 'object') ? e.name : e);
      }

      onSave(payload);
    }
  };


  const getAlreadySelectedSections = (currentRowIndex, subjectId) => {
    if (!subjectId) return new Set();
    const selected = new Set();
    formData.assignments.forEach((asgn, i) => {
      if (i !== currentRowIndex && String(asgn.subjectId) === String(subjectId)) {
        (asgn.sectionIds || []).forEach(sid => selected.add(String(sid)));
      }
    });
    return selected;
  };

  const getSectionLabel = (sectionIds) => {
    if (!sectionIds || sectionIds.length === 0) return 'No sections selected';
    return sectionIds
      .map(id => {
        const sec = sections.find(s => String(s.id) === String(id));
        return sec ? `${sec.name}` : id;
      })
      .join(' + ');
  };

  return (
    <div className="form teacher-enrollment-form" style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: '15px' }}>
      <div className="form-grid">
        {/* BASIC INFO */}
        <div className="form-section">
          <h3>Basic Profile</h3>
          <div className="form-group">
            <label>Full Name</label>
            <input type="text" name="name" value={formData.name} onChange={handleChange} placeholder="e.g. Dr. John Doe" />
            {errors.name && <p className="error">{errors.name}</p>}
          </div>

          <div className="form-group">
            <label>Email Address</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} placeholder="john@college.edu" />
            {errors.email && <p className="error">{errors.email}</p>}
          </div>

          <div className="form-group">
            <label>Department</label>
            <input type="text" name="department" value={formData.department} onChange={handleChange} placeholder="e.g. Computer Science" />
            {errors.department && <p className="error">{errors.department}</p>}
          </div>

          <div className="form-group">
            <label>Phone</label>
            <input type="text" name="phone" value={formData.phone} onChange={handleChange} />
            {errors.phone && <p className="error">{errors.phone}</p>}
          </div>

          <div className="form-group highlight-input">
            <label>Handling Capacity (Max Combined Sections)</label>
            <input
              type="number"
              name="maxCombinedSections"
              value={formData.maxCombinedSections || 0}
              onChange={handleChange}
              min="0"
              placeholder="0 (Standard)"
            />
            <p className="hint" style={{ fontSize: '11px', marginTop: '4px', color: '#64748b' }}>Limit for combined elective slots.</p>
          </div>
        </div>

        {/* CREDENTIALS */}
        <div className="form-section highlight">
          <h3>Onboarding Credentials</h3>
          <div className="form-group">
            <label>Login Username</label>
            <input type="text" name="username" value={formData.username} onChange={handleChange} placeholder="Set unique username" />
            {errors.username && <p className="error">{errors.username}</p>}
          </div>

          <div className="form-group">
            <label>Initial Password</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder={teacher ? "Leave blank to keep current" : "Set initial password"} />
            {errors.password && <p className="error">{errors.password}</p>}
          </div>
        </div>
      </div>

      {/* ROOM PERMISSIONS */}
      <div className="form-section">
        <h3>Classroom Type Eligibility</h3>
        <p className="section-hint">Select the types of rooms this teacher is authorized to book.</p>
        <div className="checkbox-row">
          {roomTypeOptions.map((opt) => (
            <label key={opt.value} className="checkbox-label">
              <input
                type="checkbox"
                checked={formData.eligibleRoomTypes.includes(opt.value)}
                onChange={() => handleRoomTypeToggle(opt.value)}
              />
              {opt.label}
            </label>
          ))}
        </div>
      </div>

      {/* WORKLOAD ASSIGNMENTS — GROUPED SECTION SLOTS */}
      <div className="form-section">
        <div className="section-header">
          <h3>Teaching Workload Mapping</h3>
          <button className="add-btn-small" onClick={addAssignment}>+ Add Slot</button>
        </div>
        <p className="section-hint" style={{ fontSize: '12px', color: '#64748b', marginBottom: '10px' }}>
          Each slot can cover <strong>multiple sections together</strong> (e.g. A1 + A2 in one combined lecture).
          Add separate slots for different groupings.
        </p>

        <div className="assignments-table">
          {formData.assignments.length === 0 && <p className="empty-hint">No slots assigned yet. Click "+ Add Slot" to begin.</p>}

          {formData.assignments.map((asgn, idx) => (
            <div key={idx} className="assignment-row" style={{
              background: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderRadius: '10px',
              padding: '14px',
              marginBottom: '12px',
              position: 'relative'
            }}>
              {/* Slot header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={{ fontWeight: '600', fontSize: '13px', color: '#334155' }}>
                  Slot {idx + 1}
                  {asgn.sectionIds && asgn.sectionIds.length > 0 && (
                    <span style={{
                      marginLeft: '8px',
                      background: '#dbeafe',
                      color: '#1d4ed8',
                      padding: '2px 8px',
                      borderRadius: '999px',
                      fontSize: '11px',
                      fontWeight: '500'
                    }}>
                      {getSectionLabel(asgn.sectionIds)}
                    </span>
                  )}
                </span>
                <button
                  className="remove-btn"
                  onClick={() => removeAssignment(idx)}
                  style={{ background: '#fee2e2', color: '#dc2626', border: 'none', borderRadius: '6px', padding: '4px 10px', cursor: 'pointer', fontWeight: '700' }}
                >
                  × Remove
                </button>
              </div>

              {/* Subject selector */}
              <div className="form-group" style={{ marginBottom: '10px' }}>
                <label style={{ fontSize: '12px', fontWeight: '600', color: '#475569' }}>Subject</label>
                <select
                  value={asgn.subjectId}
                  onChange={(e) => updateAssignmentSubject(idx, e.target.value)}
                  style={{ width: '100%', padding: '7px 10px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '13px' }}
                >
                  <option value="">Select Subject</option>
                  {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
                {errors[`asgn_sub_${idx}`] && <p className="error" style={{ fontSize: '11px', color: '#ef4444', margin: '3px 0 0' }}>{errors[`asgn_sub_${idx}`]}</p>}
              </div>

              {/* Multi-section checkboxes */}
              <div className="form-group">
                <label style={{ fontSize: '12px', fontWeight: '600', color: '#475569', display: 'block', marginBottom: '6px' }}>
                  Sections for this slot <span style={{ color: '#94a3b8', fontWeight: '400' }}>(check all that attend together)</span>
                </label>
                <div style={{
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: '6px',
                  padding: '8px',
                  background: '#ffffff',
                  borderRadius: '6px',
                  border: '1px solid #e2e8f0',
                  maxHeight: '130px',
                  overflowY: 'auto'
                }}>
                  {(() => {
                    const alreadySelected = getAlreadySelectedSections(idx, asgn.subjectId);
                    const sectionsToShow = sections.filter(sec => !alreadySelected.has(String(sec.id)));
                    
                    if (sectionsToShow.length === 0) {
                      return <span style={{ color: '#94a3b8', fontSize: '12px' }}>No remaining sections available for this subject</span>;
                    }
                    
                    return sectionsToShow.map(sec => {
                      const isChecked = (asgn.sectionIds || []).includes(String(sec.id));
                      return (
                        <label
                          key={sec.id}
                          style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '5px',
                            padding: '4px 10px',
                            borderRadius: '6px',
                            background: isChecked ? '#dbeafe' : '#f1f5f9',
                            border: isChecked ? '1px solid #93c5fd' : '1px solid #e2e8f0',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: isChecked ? '600' : '400',
                            color: isChecked ? '#1d4ed8' : '#475569',
                            transition: 'all 0.15s ease',
                            userSelect: 'none'
                          }}
                        >
                          <input
                            type="checkbox"
                            checked={isChecked}
                            onChange={() => toggleSectionInAssignment(idx, sec.id)}
                            style={{ margin: 0 }}
                          />
                          {sec.name}
                          {sec.batchYear && <span style={{ opacity: 0.6, fontSize: '10px' }}> ({sec.batchYear})</span>}
                        </label>
                      );
                    });
                  })()}
                </div>
                {errors[`asgn_sec_${idx}`] && <p className="error" style={{ fontSize: '11px', color: '#ef4444', margin: '3px 0 0' }}>{errors[`asgn_sec_${idx}`]}</p>}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="modal-actions">
        <button className="cancel-btn" onClick={onClose}>Cancel</button>
        <button className="save-btn" onClick={handleSubmit}>
          {teacher ? "Update Teacher" : "Enroll Teacher"}
        </button>
      </div>
    </div>
  );
}

export default Teacherform;