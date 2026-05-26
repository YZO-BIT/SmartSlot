import React from 'react';
import '../TeacherManagement.css';
import SectionRow from './SectionRow';

function Sectiontable({ sections, onEdit, onDelete, assignments, teachers }) {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isTeacher = user.role === 'TEACHER';
  const gridStyle = isTeacher ? { gridTemplateColumns: '1fr 1fr 1fr 1.5fr' } : { gridTemplateColumns: '1fr 1fr 1fr 1.5fr 1fr' };

  return (
    <div className="table-content">
      <div className="table-header" style={gridStyle}>
        <span> Section Name</span>
        <span> Batch Year</span>
        <span> Student Count</span>
        <span> Assigned Teachers</span>
        {!isTeacher && <span> Actions</span>}
      </div>
      {sections.length === 0 ? (
        <p className='no-data'>No sections found ⚠️</p>
      ) : (
        sections.map((section) => (
          <SectionRow
            key={section.id}
            section={section}
            assignments={assignments}
            allTeachers={teachers}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))
      )}
    </div>
  );
}

export default Sectiontable;
