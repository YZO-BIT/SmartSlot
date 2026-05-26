import React from 'react';
import '../TeacherManagement.css';
function TeacherRow({ teacher, onEdit, onDelete }) {
return(
<div className='row'>
<span>{teacher.name}</span>
<span>{teacher.email}</span>
<span>{teacher.phone}</span>
<span>{teacher.department}</span>

<div className='actions'>
<button className='edit-btn' onClick={()=>onEdit(teacher)}>✏️ Edit</button>
<button className='delete-btn' onClick={()=>onDelete(teacher.id)}>🗑️ Delete</button>
</div>
</div>
);
}
export default TeacherRow;