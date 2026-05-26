import React from 'react';
import '../TeacherManagement.css';
import TeacherRow from './TeacherRow';

function Teachertable({ teachers, onEdit, onDelete }) {
return (
<div className="table-content">
<div className="table-header">
<span> Name</span>
<span> Email</span>
<span> Phone</span>
<span> Department</span>
<span> Actions</span>
</div>
{teachers.length===0?(
<p className='no-data'>No teachers found ⚠️</p>):(
    teachers.map((teacher)=>
        <TeacherRow
        key={teacher.id}
        teacher={teacher}
        onEdit={onEdit}
        onDelete={onDelete}
        />
    ))}
</div>
);   
}
export default Teachertable;