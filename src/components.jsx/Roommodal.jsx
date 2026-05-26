import React from 'react';
import '../TeacherManagement.css';
import Roomform from './Roomform';

function Roommodal({ room, onClose, onSave }) {
  return (
    <div className="modal-overlay">
      <div className="modal">
        <h2>{room ? 'Edit Room' : 'Add New Room'}</h2>
        <Roomform 
          initialData={room} 
          onSave={onSave} 
          onCancel={onClose} 
        />
      </div>
    </div>
  );
}

export default Roommodal;
