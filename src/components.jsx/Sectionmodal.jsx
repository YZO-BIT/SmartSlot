import React from 'react';
import '../TeacherManagement.css';
import Sectionform from './Sectionform';

function Sectionmodal({ section, onClose, onSave }) {
  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{section ? 'Edit Section' : 'Add New Section'}</h2>
        <Sectionform 
          initialData={section} 
          onSave={onSave} 
          onCancel={onClose} 
        />
      </div>
    </div>
  );
}

export default Sectionmodal;
