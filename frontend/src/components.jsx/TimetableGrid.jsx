import React, { useState } from "react";
import { requestBookingCancel } from "../api/api";

const TimetableGrid = ({ bookings, onCellClick }) => {
    const days = ["MON", "TUE", "WED", "THU", "FRI", "SAT"];
    const slots = [
      { id: 1, time: "08:00 - 08:55" },
      { id: 2, time: "08:55 - 09:50" },
      { id: 3, time: "10:10 - 11:05" },
      { id: 4, time: "11:05 - 12:00" },
      { id: 5, time: "12:00 - 12:55" },
      { id: 6, time: "12:55 - 01:50" },
      { id: 7, time: "02:10 - 03:05" },
      { id: 8, time: "03:05 - 04:00" },
      { id: 9, time: "04:00 - 04:55" },
      { id: 10, time: "04:55 - 05:50" }
    ];

    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const [loadingId, setLoadingId] = useState(null);

    // 🕒 Find booking by day and slot, grouping sections together
    const getBooking = (dayName, slotId) => {
      const slotBookings = bookings.filter(b => {
        if (!b.bookingDate) return false;
        // Parse "YYYY-MM-DD" safely without timezone shifts
        const parts = b.bookingDate.split('-');
        if (parts.length !== 3) return false;
        const y = parseInt(parts[0], 10);
        const m = parseInt(parts[1], 10) - 1;
        const d = parseInt(parts[2], 10);
        const date = new Date(y, m, d);
        const day = date.toLocaleDateString('en-US', { weekday: 'short' }).toUpperCase();
        return day === dayName && b.slotId === slotId;
      });
      if (slotBookings.length === 0) return null;

      const primary = slotBookings[0];
      const sectionsLabel = slotBookings
        .map(b => b.section?.name)
        .filter(Boolean)
        .filter((val, idx, self) => self.indexOf(val) === idx)
        .join(" + ");

      return {
        ...primary,
        displaySections: sectionsLabel,
        allIds: slotBookings.map(b => b.id)
      };
    };

    const handleRequestCancel = async (ids) => {
      if (!window.confirm("Are you sure you want to request cancellation for this slot?")) return;
      setLoadingId(ids[0]);
      try {
        await Promise.all(ids.map(id => requestBookingCancel(id)));
        alert("Cancellation request sent.");
        window.location.reload(); 
      } catch (err) {
        alert("Error sending request.");
      } finally {
        setLoadingId(null);
      }
    };

    return (
      <div className="timetable-container" style={{ overflowX: 'auto', marginTop: '20px' }}>
        <table className="institutional-grid" style={{ width: '100%', tableLayout: 'fixed', borderCollapse: 'collapse', border: '1px solid #e2e8f0' }}>
          <thead>
            <tr style={{ backgroundColor: '#f8fafc' }}>
              <th style={{ border: '1px solid #e2e8f0', padding: '8px 4px', fontSize: '11px', width: '70px', color: '#64748b' }}>DAY/TIME</th>
              {slots.map(s => (
                <th key={s.id} style={{ border: '1px solid #e2e8f0', padding: '8px 2px', fontSize: '10px', minWidth: '65px', color: '#64748b' }}>
                  {s.time}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {days.map(day => (
              <tr key={day}>
                <td style={{ 
                  border: '1px solid #e2e8f0', 
                  padding: '8px 4px', 
                  fontWeight: 'bold', 
                  fontSize: '11px',
                  backgroundColor: '#f8fafc',
                  color: '#475569',
                  textAlign: 'center'
                }}>{day}</td>
                {slots.map(slot => {
                  // If it's an even slot, check if the previous slot has a lab booking
                  if (slot.id % 2 === 0) {
                    const prevB = getBooking(day, slot.id - 1);
                    if (prevB && (prevB.room?.roomType === 'LAB' || prevB.subject?.roomTypeRequirement === 'LAB')) {
                      return null; // Skip this cell because it's spanned by the previous slot's colSpan=2
                    }
                  }

                  const b = getBooking(day, slot.id);
                  const isLab = b && (b.room?.roomType === 'LAB' || b.subject?.roomTypeRequirement === 'LAB');
                  const colSpan = isLab ? 2 : 1;
                  const isInteractive = !b && user.role?.toUpperCase() === 'TEACHER' && onCellClick;
                  
                  return (
                    <td 
                      key={slot.id} 
                      colSpan={colSpan}
                      onClick={() => isInteractive && onCellClick(day, slot.id)}
                      style={{ 
                        border: '1px solid #e2e8f0', 
                        padding: '4px', 
                        minHeight: '80px',
                        verticalAlign: 'top',
                        cursor: isInteractive ? 'pointer' : 'default',
                        backgroundColor: b ? (String(b.teacher?.id) === String(user.id) ? 'rgba(59, 130, 246, 0.05)' : 'transparent') : 'transparent',
                        transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                        position: 'relative'
                      }}
                      onMouseEnter={(e) => {
                        if (isInteractive) {
                          e.currentTarget.style.backgroundColor = 'rgba(59, 130, 246, 0.08)';
                          e.currentTarget.style.transform = 'scale(1.02)';
                          e.currentTarget.style.zIndex = '10';
                          e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                        }
                      }}
                      onMouseLeave={(e) => {
                        if (isInteractive) {
                          e.currentTarget.style.backgroundColor = 'transparent';
                          e.currentTarget.style.transform = 'scale(1)';
                          e.currentTarget.style.zIndex = '1';
                          e.currentTarget.style.boxShadow = 'none';
                        }
                      }}
                    >
                      {b ? (
                        <div className="grid-entry" style={{ 
                          fontSize: '10px', 
                          background: String(b.teacher?.id) === String(user.id) ? '#eff6ff' : '#f1f5f9',
                          padding: '4px',
                          borderRadius: '6px',
                          borderLeft: `3px solid ${String(b.teacher?.id) === String(user.id) ? '#3b82f6' : '#94a3b8'}`,
                          boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
                          height: '100%',
                          display: 'flex',
                          flexDirection: 'column',
                          justifyContent: 'space-between'
                        }}>
                          <div style={{ fontWeight: '700', color: '#1e293b', marginBottom: '2px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }} title={b.subject?.name}>
                            {b.subject?.name} {isLab && <span style={{ color: '#3b82f6', fontSize: '9px', fontWeight: 'normal' }}>(Lab Block)</span>}
                          </div>
                          <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748b', fontSize: '9px' }}>
                            <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{b.room?.roomNumber}</span>
                            <span style={{ fontWeight: 'bold', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{b.displaySections || b.section?.name}</span>
                          </div>
                          
                          {user.role?.toUpperCase() === 'TEACHER' && String(b.teacher?.id) === String(user.id) && b.status === 'CONFIRMED' && (
                            <button 
                              onClick={(e) => { 
                                e.stopPropagation(); 
                                let cancelIds = b.allIds || [b.id];
                                if (isLab) {
                                  const partnerB = getBooking(day, slot.id + 1);
                                  if (partnerB) {
                                    cancelIds = [...new Set([...cancelIds, ...(partnerB.allIds || [partnerB.id])])];
                                  }
                                }
                                handleRequestCancel(cancelIds); 
                              }}
                              disabled={loadingId === b.id}
                              style={{ 
                                marginTop: '4px', 
                                width: '100%',
                                padding: '2px 0', 
                                backgroundColor: '#fee2e2', 
                                border: '1px solid #fecaca',
                                color: '#ef4444',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '8px',
                                fontWeight: '600'
                              }}
                            >
                              Cancel
                            </button>
                          )}
                        </div>
                      ) : (
                        isInteractive && (
                          <div className="empty-slot-hint" style={{ 
                            height: '100%', 
                            display: 'flex', 
                            alignItems: 'center', 
                            justifyContent: 'center',
                            fontSize: '24px', 
                            color: '#3b82f6', 
                            opacity: 0.2,
                            fontWeight: '300'
                          }}>
                            +
                          </div>
                        )
                      )}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };
;

export default TimetableGrid;
