import React from 'react';
import '../TeacherManagement.css';
import Bookingform from './Bookingform';

function Bookingmodal({ booking, onClose, onSave, prefilledDay, prefilledSlot }) {
  return (
    <div className="modal-overlay">
      <div className="modal" style={{ width: '450px' }}>
        <h2>{booking ? 'Edit Booking' : (prefilledDay ? 'Claim Time Slot' : 'Add New Booking')}</h2>
        <Bookingform
          initialData={booking}
          onSave={onSave}
          onCancel={onClose}
          prefilledDay={prefilledDay}
          prefilledSlot={prefilledSlot}
        />
      </div>
    </div>
  );
}

export default Bookingmodal;
