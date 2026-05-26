import React, { useEffect, useState } from "react";
import api from "../api/api";

const RecentBookings = () => {
  const [bookings, setBookings] = useState([]);

  useEffect(() => {
    api.get("/bookings/all")
      .then((res) => {
        const sorted = res.data.sort(
          (a, b) => new Date(b.bookingDate) - new Date(a.bookingDate)
        ).slice(0, 5); // Just show top 5
        setBookings(sorted);
      })
      .catch((err) => console.error("Error fetching bookings:", err));
  }, []);

  return (
    <div className="card-section">
      <h2>Recent Bookings</h2>
      {bookings.length === 0 ? (
        <p>No bookings available</p>
      ) : (
        <ul className="booking-list">
          {bookings.map((b) => (
            <li key={b.id} style={{ marginBottom: '10px', fontSize: '14px' }}>
              <strong>{b.room?.roomNumber}</strong>: {b.subject?.name} by {b.teacher?.name} 
              <br/>
              <small style={{ color: '#64748b' }}>Slot {b.slotId} | {b.bookingDate}</small>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default RecentBookings;
