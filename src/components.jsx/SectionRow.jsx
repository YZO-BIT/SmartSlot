import React from 'react';
import '../TeacherManagement.css';

function SectionRow({ section, onEdit, onDelete, assignments, allTeachers }) {
  if (!section) return null;

  // 👩‍🏫 Find teachers for this section
  const sectionTeachers = (assignments || [])
    .filter(a => a.section?.id === section.id)
    .map(a => a.teacher?.name || allTeachers.find(t => t.id === a.teacher?.id)?.name)
    .filter((name, index, self) => name && self.indexOf(name) === index); // Unique names

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isTeacher = user.role === 'TEACHER';
  const gridStyle = isTeacher ? { gridTemplateColumns: '1fr 1fr 1fr 1.5fr' } : { gridTemplateColumns: '1fr 1fr 1fr 1.5fr 1fr' };

  return (
    <div className="row" style={gridStyle}>
      <span style={{ fontWeight: '600' }}>{section?.name || 'N/A'}</span>
      <span>{section?.batchYear || 'N/A'}</span>
      <span>{section?.studentCount || 0}</span>
      <div className="teacher-chips" style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
        {sectionTeachers.length > 0 ? (
          sectionTeachers.map((t, i) => (
            <span key={i} className="ss-chip" style={{ background: '#f1f5f9', color: '#475569', fontSize: '11px', border: '1px solid #e2e8f0' }}>
              {t}
            </span>
          ))
        ) : (
          <span style={{ color: '#94a3b8', fontSize: '11px' }}>No teachers</span>
        )}
      </div>
      {!isTeacher && (
        <div className="actions">
          <button className="edit-btn" onClick={() => onEdit(section)}>Edit</button>
          <button className="delete-btn" onClick={() => onDelete(section?.id)}>Delete</button>
        </div>
      )}
    </div>
  );
}

export default SectionRow;
