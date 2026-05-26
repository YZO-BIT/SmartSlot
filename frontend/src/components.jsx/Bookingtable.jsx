import React from 'react';
import '../TeacherManagement.css';
import BookingRow from './BookingRow';

function Bookingtable({ bookings, onEdit, onDelete }) {
  return (
    <div className="table-content">
      <div className="table-header" style={{ gridTemplateColumns: '1.5fr 1fr 1fr 1.5fr 1fr 1fr 1fr' }}>
        <span> Teacher</span>
        <span> Room</span>
        <span> Section/Group</span>
        <span> Subject</span>
        <span> Slot</span>
        <span> Date</span>
        <span> Actions</span>
      </div>
      {bookings.length === 0 ? (
        <p className='no-data'>No bookings found ⚠️</p>
      ) : (
        bookings.map((booking) => (
          <BookingRow
            key={booking.id}
            booking={booking}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))
      )}
    </div>
  );
}

export default Bookingtable;
