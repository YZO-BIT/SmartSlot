import React from 'react';
import '../TeacherManagement.css';
import RoomRow from './RoomRow';

function Roomtable({ rooms, onEdit, onDelete }) {
  return (
    <div className="table-content">
      <div className="table-header">
        <span> Room Number</span>
        <span> Category</span>
        <span> Type</span>
        <span> Capacity</span>
        <span> Actions</span>
      </div>
      {rooms.length === 0 ? (
        <p className='no-data'>No rooms found ⚠️</p>
      ) : (
        rooms.map((room) => (
          <RoomRow
            key={room.id}
            room={room}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))
      )}
    </div>
  );
}

export default Roomtable;
