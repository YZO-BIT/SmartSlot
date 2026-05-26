import React, { useState, useEffect } from 'react';
import { 
  getPendingUsers, approveUser, rejectUser, 
  getAllBookings, approveBookingCancel, rejectBookingCancel, 
  getAllPendingTickets, approveTicket, rejectTicket 
} from '../api/api';
import Header from '../components.jsx/header';

function ApprovalCenter() {
  const [pendingUsers, setPendingUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [activeTab, setActiveTab] = useState('users'); // 'users', 'bookings', or 'tickets'
  const [pendingBookings, setPendingBookings] = useState([]);
  const [pendingTickets, setPendingTickets] = useState([]);
  
  // Get current user to check role
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const userRole = user.role || 'ADMIN'; // Default to Admin if not set for now

  // Fetch functions with "silent" mode support to prevent polling flickers
  const fetchPending = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const response = await getPendingUsers(userRole);
      setPendingUsers(response.data || []);
    } catch (err) {
      console.error("Error fetching pending users:", err);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  const fetchPendingBookings = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const response = await getAllBookings();
      const filtered = Array.isArray(response.data) 
        ? response.data.filter(b => b.status === 'PENDING_CANCEL') 
        : [];
      setPendingBookings(filtered);
    } catch (err) {
      console.error("Error fetching bookings:", err);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  const fetchPendingTickets = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const response = await getAllPendingTickets();
      setPendingTickets(response.data || []);
    } catch (err) {
      console.error("Error fetching tickets:", err);
    } finally {
      if (!silent) setLoading(false);
    }
  };

  useEffect(() => {
    const fetchData = (silent = false) => {
      if (activeTab === 'users') fetchPending(silent);
      else if (activeTab === 'bookings') fetchPendingBookings(silent);
      else fetchPendingTickets(silent);
    };

    fetchData(false); // First fetch displays loading spinner

    // Start 5s polling interval silently
    const interval = setInterval(() => {
      fetchData(true);
    }, 5000);

    return () => clearInterval(interval);
  }, [activeTab]);

  const handleApprove = async (id) => {
    try {
      await approveUser(id);
      setMessage("User approved successfully.");
      fetchPending();
    } catch (err) {
      setMessage("Error approving user.");
    }
  };

  const handleReject = async (id) => {
    try {
      await rejectUser(id);
      setMessage("User rejected and removed.");
      fetchPending();
    } catch (err) {
      setMessage("Error rejecting user.");
    }
  };

  const handleApproveBooking = async (id) => {
    try {
      await approveBookingCancel(id);
      setMessage("Booking cancelled successfully.");
      fetchPendingBookings();
    } catch (err) {
      setMessage("Error approving cancellation.");
    }
  };

  const handleRejectBooking = async (id) => {
    try {
      await rejectBookingCancel(id);
      setMessage("Cancellation request rejected.");
      fetchPendingBookings();
    } catch (err) {
      setMessage("Error rejecting cancellation.");
    }
  };

  const handleApproveTicket = async (id) => {
    try {
      await approveTicket(id);
      setMessage("Ticket approved. Rule override is now active for this request.");
      fetchPendingTickets();
    } catch (err) {
      setMessage("Error approving ticket.");
    }
  };

  const handleRejectTicket = async (id) => {
    try {
      await rejectTicket(id);
      setMessage("Ticket rejected.");
      fetchPendingTickets();
    } catch (err) {
      setMessage("Error rejecting ticket.");
    }
  };

  return (
    <>
      <Header 
        title="Approval Center" 
        subTitle="Verify and activate new account requests"
        onAdd={() => {}} // Dummy as I don't need the button here
        addLabel="Refresh"
      />
      
      {/* Tab Switcher */}
      <div style={{ display: 'flex', gap: '20px', padding: '0 20px', borderBottom: '1px solid #e2e8f0' }}>
        <button 
          onClick={() => setActiveTab('users')}
          style={{ 
            padding: '12px 10px', 
            border: 'none', 
            background: 'none', 
            fontWeight: activeTab === 'users' ? '600' : '400',
            borderBottom: activeTab === 'users' ? '2px solid #2563eb' : '2px solid transparent',
            color: activeTab === 'users' ? '#2563eb' : '#64748b',
            cursor: 'pointer'
          }}
        >
          User Account Requests
        </button>
        <button 
          onClick={() => setActiveTab('bookings')}
          style={{ 
            padding: '12px 10px', 
            border: 'none', 
            background: 'none', 
            fontWeight: activeTab === 'bookings' ? '600' : '400',
            borderBottom: activeTab === 'bookings' ? '2px solid #2563eb' : '2px solid transparent',
            color: activeTab === 'bookings' ? '#2563eb' : '#64748b',
            cursor: 'pointer'
          }}
        >
          Booking Cancellations
        </button>
        <button 
          onClick={() => setActiveTab('tickets')}
          style={{ 
            padding: '12px 10px', 
            border: 'none', 
            background: 'none', 
            fontWeight: activeTab === 'tickets' ? '600' : '400',
            borderBottom: activeTab === 'tickets' ? '2px solid #2563eb' : '2px solid transparent',
            color: activeTab === 'tickets' ? '#2563eb' : '#64748b',
            cursor: 'pointer'
          }}
        >
          Oversight Tickets
        </button>
      </div>

      <div className="approval-container" style={{ padding: '20px' }}>
        <p style={{ color: '#64748b', marginBottom: '20px' }}>
          {activeTab === 'users' 
            ? `Manage new account requests. ${userRole === 'HOD' ? 'You can only approve Teachers.' : 'You can approve HODs and Teachers.'}`
            : activeTab === 'bookings'
            ? `Teachers have requested to release these slots. Approving will delete the booking and free the room.`
            : `Grant exceptions for rule violations (e.g. teaching too many consecutive classes or room overrides).`
          }
        </p>

        {message && (
          <div style={{ 
            padding: '12px', 
            backgroundColor: '#f0fdf4', 
            color: '#166534', 
            borderRadius: '8px', 
            marginBottom: '20px',
            border: '1px solid #bbf7d0'
          }}>
            {message}
          </div>
        )}

        <div className="glass-card" style={{ padding: '0', overflow: 'hidden' }}>
          {activeTab === 'users' ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                <tr>
                  <th style={{ padding: '15px' }}>Name</th>
                  <th style={{ padding: '15px' }}>Email</th>
                  <th style={{ padding: '15px' }}>Department</th>
                  <th style={{ padding: '15px' }}>Role Requested</th>
                  <th style={{ padding: '15px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan="5" style={{ padding: '30px', textAlign: 'center' }}>Loading requests...</td></tr>
                ) : pendingUsers.length === 0 ? (
                  <tr><td colSpan="5" style={{ padding: '30px', textAlign: 'center' }}>No pending account requests.</td></tr>
                ) : pendingUsers.map(u => (
                  <tr key={u.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '15px' }}>{u.name}</td>
                    <td style={{ padding: '15px' }}>{u.email}</td>
                    <td style={{ padding: '15px' }}>{u.department}</td>
                    <td style={{ padding: '15px' }}>
                      <span style={{ 
                        padding: '4px 8px', 
                        borderRadius: '4px', 
                        fontSize: '11px',
                        backgroundColor: u.role === 'HOD' ? '#fef3c7' : '#dcfce7',
                        color: u.role === 'HOD' ? '#92400e' : '#166534'
                      }}>
                        {u.role}
                      </span>
                    </td>
                    <td style={{ padding: '15px' }}>
                      <button 
                        onClick={() => handleApprove(u.id)}
                        style={{ 
                          marginRight: '10px', 
                          padding: '6px 12px', 
                          backgroundColor: '#2563eb', 
                          color: 'white', 
                          border: 'none', 
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Approve
                      </button>
                      <button 
                        onClick={() => handleReject(u.id)}
                        style={{ 
                          padding: '6px 12px', 
                          backgroundColor: '#ef4444', 
                          color: 'white', 
                          border: 'none', 
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Reject
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : activeTab === 'bookings' ? (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                <tr>
                  <th style={{ padding: '15px' }}>Subject</th>
                  <th style={{ padding: '15px' }}>Teacher</th>
                  <th style={{ padding: '15px' }}>Room & Slot</th>
                  <th style={{ padding: '15px' }}>Date</th>
                  <th style={{ padding: '15px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan="5" style={{ padding: '30px', textAlign: 'center' }}>Loading requests...</td></tr>
                ) : pendingBookings.length === 0 ? (
                  <tr><td colSpan="5" style={{ padding: '30px', textAlign: 'center' }}>No pending cancellation requests.</td></tr>
                ) : pendingBookings.map(booking => (
                  <tr key={booking.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '15px' }}>
                      <div style={{ fontWeight: '600' }}>{booking.subject?.name}</div>
                      <div style={{ fontSize: '11px', color: '#64748b' }}>Sec: {booking.section?.name}</div>
                    </td>
                    <td style={{ padding: '15px' }}>{booking.teacher?.name}</td>
                    <td style={{ padding: '15px' }}>
                      Room {booking.room?.roomNumber} | Slot {booking.slotId}
                    </td>
                    <td style={{ padding: '15px' }}>{booking.bookingDate}</td>
                    <td style={{ padding: '15px' }}>
                      <button 
                        onClick={() => handleApproveBooking(booking.id)}
                        style={{ 
                          marginRight: '10px', 
                          padding: '6px 12px', 
                          backgroundColor: '#dc2626', 
                          color: 'white', 
                          border: 'none', 
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Approve Cancel
                      </button>
                      <button 
                        onClick={() => handleRejectBooking(booking.id)}
                        style={{ 
                          padding: '6px 12px', 
                          backgroundColor: '#94a3b8', 
                          color: 'white', 
                          border: 'none', 
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Keep Booking
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead style={{ backgroundColor: '#f8fafc', borderBottom: '1px solid #e2e8f0' }}>
                <tr>
                  <th style={{ padding: '15px' }}>Teacher</th>
                  <th style={{ padding: '15px' }}>Requested Target</th>
                  <th style={{ padding: '15px' }}>Special Request</th>
                  <th style={{ padding: '15px' }}>Context/Details</th>
                  <th style={{ padding: '15px' }}>Status</th>
                  <th style={{ padding: '15px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan="6" style={{ padding: '30px', textAlign: 'center' }}>Loading tickets...</td></tr>
                ) : pendingTickets.length === 0 ? (
                  <tr><td colSpan="6" style={{ padding: '30px', textAlign: 'center' }}>No pending oversight tickets.</td></tr>
                ) : pendingTickets.map(ticket => (
                  <tr key={ticket.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '15px' }}>{ticket.teacher?.name}</td>
                    <td style={{ padding: '15px' }}>
                      <div style={{ fontWeight: '600' }}>{ticket.requestedDate}</div>
                      <div style={{ fontSize: '12px', color: '#64748b' }}>
                        Slot {ticket.requestedSlotId} | Sec: {ticket.requestedSection?.name || 'N/A'}
                      </div>
                    </td>
                    <td style={{ padding: '15px', fontWeight: 'bold' }}>{ticket.reason}</td>
                    <td style={{ padding: '15px', fontSize: '12px' }}>{ticket.conflictDetails}</td>
                    <td style={{ padding: '15px' }}>
                      <span style={{ 
                        fontSize: '11px', 
                        background: '#fef3c7', 
                        color: '#92400e', 
                        padding: '4px 8px', 
                        borderRadius: '4px',
                        fontWeight: '600'
                      }}>
                        {ticket.status}
                      </span>
                    </td>
                    <td style={{ padding: '15px' }}>
                      <button 
                        onClick={() => handleApproveTicket(ticket.id)}
                        style={{ 
                          marginRight: '10px', 
                          padding: '6px 12px', 
                          backgroundColor: '#16a34a', 
                          color: 'white', 
                          border: 'none', 
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Grant Exception
                      </button>
                      <button 
                        onClick={() => handleRejectTicket(ticket.id)}
                        style={{ 
                          padding: '6px 12px', 
                          backgroundColor: '#ef4444', 
                          color: 'white', 
                          border: 'none', 
                          borderRadius: '4px',
                          cursor: 'pointer'
                        }}
                      >
                        Reject
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  );
}

export default ApprovalCenter;
