import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../TeacherManagement.css';

import Header from '../components.jsx/header';
import Bookingtable from '../components.jsx/Bookingtable';
import Bookingmodal from '../components.jsx/Bookingmodal';
import { getAllBookings, updateBooking, deleteBooking, createBooking } from '../api/api';

function BookingManagement() {
  const [bookings, setBookings] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const navigate = useNavigate();

  // 📦 FETCH DATA
  useEffect(() => {
    const user = localStorage.getItem('user');
    if (!user) {
      navigate('/login');
      return;
    }
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      const response = await getAllBookings();
      setBookings(response.data);
    } catch (err) {
      console.error("Error fetching bookings:", err);
    }
  };

  // 🔍 FILTER
  const filteredBookings = bookings.filter((b) =>
    b.status !== 'CANCELLED' &&
    (b.teacher?.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    b.room?.roomNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    b.subject?.name?.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  // 💾 SAVE (EDIT ONLY for this view, creation is usually done via scheduling logic)
  const handleSave = async (data) => {
    try {
      if (selectedBooking) {
        // ✏️ UPDATE
        await updateBooking(selectedBooking.id, data);
      } else {
        // ➕ CREATE
        await createBooking(data);
      }
      fetchBookings();
      setIsModalOpen(false);
      setSelectedBooking(null);
    } catch (err) {
      console.error("Error saving booking:", err);
      alert("Failed to save booking.");
    }
  };

  // ❌ DELETE
  const handleDelete = async (id) => {
    const confirmDelete = window.confirm("Are you sure you want to delete this booking?");
    
    if (confirmDelete) {
      try {
        await deleteBooking(id);
        fetchBookings();
      } catch (err) {
        console.error("Error deleting booking:", err);
        alert("Failed to delete booking.");
      }
    }
  };

  // ✏️ EDIT OPEN
  const handleEdit = (booking) => {
    setSelectedBooking(booking);
    setIsModalOpen(true);
  };

  return (
    <div className="teacher-management">

      {/* 🔷 HEADER */}
      <Header 
        title="Booking Management"
        subTitle="Monitor and adjust scheduled slots"
        searchPlaceholder="Search by teacher, room, or subject..."
        addLabel="Add Booking" // Although typically done via specialized logic
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        onAdd={() => {
          setSelectedBooking(null);
          setIsModalOpen(true);
        }}
      />

      {/* 🔷 TABLE */}
      {filteredBookings.length === 0 ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748b' }}>
          No bookings found or loading...
        </div>
      ) : (
        <Bookingtable 
          bookings={filteredBookings}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      )}

      {/* 🔷 MODAL */}
      {isModalOpen && (
        <Bookingmodal 
          booking={selectedBooking}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedBooking(null);
          }}
          onSave={handleSave}
        />
      )}

    </div>
  );
}

export default BookingManagement;
