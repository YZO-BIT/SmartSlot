import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../login.css';
import logo1 from '../assets/bd5ad9d2-e903-40b9-8b49-230d784ed997.png';
import logo2 from '../assets/images.png';
import { login } from '../api/api';

function Login() {
  console.log('Login component rendered');
  const [isHovered, setIsHovered] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await login(email, password);
      if (response.data.success) {
        // Store user info including role
        localStorage.setItem('user', JSON.stringify(response.data));
        const role = response.data.role?.toUpperCase();
        if (role && role !== 'TEACHER') {
          navigate('/admin');
        } else {
          navigate('/dashboard');
        }
      } else {
        setError(response.data.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials or server error');
    }
  };

  return (

    <div className="main">
      <div className="popup">

        <div className="left">
          <form onSubmit={handleLogin}>
            <div className="top-logos">
              <img src={logo1} className="logo1" alt="Smart Slot" />
              <img src={logo2} className="logo2" alt="Logo" />
            </div>
            <div className="login-header">
              <h2>Smart Slot Login</h2>
            </div>

            <input
              type="text"
              placeholder="Email or Username"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            {error && <p style={{ color: 'red', fontSize: '12px' }}>{error}</p>}

            <p className="forgot">Forgot Password?</p>

            <select className="sel" style={{
              width: '100%',
              padding: '12px',
              marginBottom: '20px',
              borderRadius: '8px',
              border: '1px solid #ddd',
              background: 'rgba(255, 255, 255, 0.9)',
              cursor: 'pointer'
            }}>
              <option>Select Role</option>
              <option>Admin</option>
              <option>HOD</option>
              <option>Teacher</option>
            </select>

            <div className="buttons">
              <button type="submit" className="login-btn">Login</button>
            </div>
          </form>
        </div>

        <div
          className="right"
          onMouseEnter={() => setIsHovered(true)}
          onMouseLeave={() => setIsHovered(false)}
        >
          {!isHovered && (
            <div className="overlay-text">
              <div className="text-container">
                <span className="g">Graphic Era</span>
                <span className="h">Hill University</span>
                <span className="d">Dehradun</span>
              </div>
              <p>Smart Slot Timetable Management System</p>
            </div>
          )}
          {isHovered && (
            <div className="overlay-text">
              <h1><span className="manage-text">Manage</span> <span className="your-text">Your</span> <span className="schedule-text">Schedule</span></h1>
              <p>By team - ANATE </p>
            </div>
          )}
        </div>

      </div>

    </div>
  )
}
export default Login;