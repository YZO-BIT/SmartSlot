import React from 'react';
import Teacherform from './Teacherform';
import '../TeacherManagement.css';

function Teachermodal({ teacher, onClose, onSave }) {

  return (
    <div className="modal-overlay">

    <div className="modal">
<div className='up'>
<h2>{teacher ? "Edit Teacher" : "Add Teacher"}</h2>
</div>
    
    <Teacherform 
     teacher={teacher}
     onSave={onSave}
     onClose={onClose}
     />

    </div>

    </div>
  );
}

export default Teachermodal;