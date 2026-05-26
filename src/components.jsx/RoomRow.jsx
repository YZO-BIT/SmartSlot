import React from 'react';
import '../TeacherManagement.css';

function RoomRow({ room, onEdit, onDelete }) {
  return (
    <div className="row">
      <span>{room.roomNumber}</span>
      <span>{room.roomCategory}</span>
      <span>{room.roomType}</span>
      <span>{room.capacity}</span>
      <div className="actions">
        <button className="edit-btn" onClick={() => onEdit(room)}>Edit</button>
        <button className="delete-btn" onClick={() => onDelete(room.id)}>Delete</button>
      </div>
    </div>
  );
}

export default RoomRow;
