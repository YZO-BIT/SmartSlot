import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../TeacherManagement.css';

function Header({ title, subTitle, searchPlaceholder, addLabel, searchTerm, setSearchTerm, onAdd }) {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const userRole = user.role || 'TEACHER';

  return (
    <div className='header'>

     <div className="header-left">
  <div>
    <h1>{title || "Teacher Management"}</h1>
    <p className="sub">{subTitle || "Manage faculty & expertise"}</p>
  </div>
</div>


<div className='mid'>
    
</div>
      <div className="header-right">

        <div className="search-box">
          <input 
            type="text" 
            placeholder={searchPlaceholder || "Search teacher..."}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button className="search-icon">🔍</button>
        </div>


          {(user.role === 'ADMIN' || user.role === 'HOD') && (
            <button 
              className="notify-btn" 
              onClick={() => navigate('/approvals')}
              style={{ position: 'relative' }}
            >
              🔔 Approvals
            </button>
          )}

        <button className="add-btn" style={{ background: '#1e293b' }} onClick={() => navigate('/dashboard')}>
           📊 Dashboard
        </button>

        {user.role !== 'TEACHER' && (
          <button className="add-btn" onClick={onAdd}>
            ➕ {addLabel || "Add Teacher"}
          </button>
        )}

      </div>

    </div>
  );
}

export default Header;