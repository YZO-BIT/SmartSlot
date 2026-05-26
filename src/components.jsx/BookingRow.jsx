import React from 'react';
import '../TeacherManagement.css';

function BookingRow({ booking, onEdit, onDelete }) {
  const entityName = booking?.section ? booking.section.name : (booking?.electiveGroup ? booking.electiveGroup.name : 'N/A');
  
  return (
    <div className="row" style={{ gridTemplateColumns: '1.5fr 1fr 1fr 1.5fr 1fr 1fr 1fr' }}>
      <span>{booking?.teacher?.name || 'N/A'}</span>
      <span>{booking?.room?.roomNumber || 'N/A'}</span>
      <span>{entityName}</span>
      <span>{booking?.subject?.name || 'N/A'}</span>
      <span>Slot {booking?.slotId || '?'}</span>
      <span>{booking?.bookingDate || 'N/A'}</span>
      <div className="actions">
        <button className="edit-btn" onClick={() => onEdit(booking)}>Edit</button>
        <button className="delete-btn" onClick={() => onDelete(booking?.id)}>Delete</button>
      </div>
    </div>
  );
}

export default BookingRow;
