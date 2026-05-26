import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../TeacherManagement.css';

import Header from '../components.jsx/header'; // We'll make it generic or create RoomHeader
import Roomtable from '../components.jsx/Roomtable';
import Roommodal from '../components.jsx/Roommodal';
import { getAllRooms, createRoom, updateRoom, deleteRoom } from '../api/api';

function RoomManagement() {
  const [rooms, setRooms] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const navigate = useNavigate();

  // 📦 FETCH DATA
  useEffect(() => {
    const user = localStorage.getItem('user');
    if (!user) {
      navigate('/login');
      return;
    }
    fetchRooms();
  }, []);

  const fetchRooms = async () => {
    try {
      const response = await getAllRooms();
      setRooms(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Error fetching rooms:", err);
      setRooms([]);
    }
  };

  // 🔍 FILTER
  const filteredRooms = rooms.filter((r) =>
    (r.roomNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    r.roomCategory?.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  // 💾 SAVE (ADD / EDIT)
  const handleSave = async (data) => {
    try {
      if (selectedRoom) {
        // ✏️ UPDATE
        await updateRoom(selectedRoom.id, data);
      } else {
        // ➕ ADD
        await createRoom(data);
      }
      fetchRooms();
      setIsModalOpen(false);
      setSelectedRoom(null);
    } catch (err) {
      console.error("Error saving room:", err);
      alert("Failed to save room.");
    }
  };

  // ❌ DELETE
  const handleDelete = async (id) => {
    const confirmDelete = window.confirm("Are you sure you want to delete this room?");
    
    if (confirmDelete) {
      try {
        await deleteRoom(id);
        fetchRooms();
      } catch (err) {
        console.error("Error deleting room:", err);
        alert("Failed to delete room.");
      }
    }
  };

  // ✏️ EDIT OPEN
  const handleEdit = (room) => {
    setSelectedRoom(room);
    setIsModalOpen(true);
  };

  return (
    <div className="workplace-container">
      <Header
        title="Room Management"
        subTitle="Manage rooms"
        searchPlaceholder="Search room..."
        addLabel="Add Room"
        searchTerm={searchTerm}
        setSearchTerm={setSearchTerm}
        onAdd={() => { setSelectedRoom(null); setIsModalOpen(true); }}
      />

      <div className="workplace-content">
        <Roomtable 
          rooms={filteredRooms}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      </div>

      {isModalOpen && (
        <Roommodal 
          room={selectedRoom}
          onClose={() => {
            setIsModalOpen(false);
            setSelectedRoom(null);
          }}
          onSave={handleSave}
        />
      )}
    </div>
  );
}

export default RoomManagement;
