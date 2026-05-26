import axios from 'axios';

export const api = axios.create({
    baseURL: '/api'
});

export const login = (identifier, password) => {
    return api.post('/auth/login', { identifier, password });
};

// --- APPROVALS ---
export const getPendingUsers = (role) => {
    return api.get(`/approvals/pending?requesterRole=${role}`);
};

export const approveUser = (id) => {
    return api.post(`/approvals/approve/${id}`);
};

export const rejectUser = (id) => {
    return api.delete(`/approvals/reject/${id}`);
};

// --- TICKETS (HOD OVERSIGHT) ---
export const getAllPendingTickets = () => {
    return api.get('/tickets');
};

export const approveTicket = (id) => {
    return api.patch(`/tickets/${id}/approve`);
};

export const rejectTicket = (id) => {
    return api.patch(`/tickets/${id}/reject`);
};

export const createTicket = (ticketData) => {
    return api.post('/tickets', ticketData);
};

export const getTeacherTickets = (teacherId) => {
    return api.get(`/tickets/teacher/${teacherId}`);
};

// --- DASHBOARD ---
export const getDashboardStats = () => {
    return api.get('/dashboard/stats');
};

export const getTeacherStats = (id) => {
    return api.get(`/dashboard/teacher/${id}/stats`);
};

// --- BOOKINGS & REQUESTS ---
export const getAllBookings = () => {
    return api.get('/bookings/all');
};

export const createBooking = (bookingData) => {
    return api.post('/bookings/create', bookingData);
};

export const updateBooking = (id, bookingData) => {
    return api.put(`/bookings/${id}`, bookingData);
};

export const deleteBooking = (id) => {
    return api.delete(`/bookings/${id}`);
};

export const requestBookingCancel = (id) => {
    return api.post(`/bookings/${id}/request-cancel`);
};

export const approveBookingCancel = (id) => {
    return api.post(`/bookings/${id}/approve-cancel`);
};

export const rejectBookingCancel = (id) => {
    return api.post(`/bookings/${id}/reject-cancel`);
};

export const getBookingsByFilter = (type, id, date, startDate, endDate) => {
    let url = `/bookings/filter?type=${type}&id=${id}`;
    if (date) url += `&date=${date}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    return api.get(url);
};

// --- ENTITY MANAGEMENT ---
export const getAllTeachers = () => {
    return api.get('/teachers');
};

export const createTeacher = (teacherData) => {
    return api.post('/teachers', teacherData);
};

export const updateTeacher = (id, teacherData) => {
    return api.put(`/teachers/${id}`, teacherData);
};

export const deleteTeacher = (id) => {
    return api.delete(`/teachers/${id}`);
};

export const getTeacherWorkload = (id) => {
    return api.get(`/teachers/${id}/workload`);
};

export const getAllWorkloads = () => {
    return api.get('/teachers/workloads/all');
};

export const getEligibleRooms = (id) => {
    return api.get(`/teachers/${id}/eligible-rooms`);
};

export const getAllRooms = () => {
    return api.get('/rooms/all');
};

export const createRoom = (roomData) => {
    return api.post('/rooms', roomData);
};

export const updateRoom = (id, roomData) => {
    return api.put(`/rooms/${id}`, roomData);
};

export const deleteRoom = (id) => {
    return api.delete(`/rooms/${id}`);
};

export const getAllSections = () => {
    return api.get('/sections/all');
};

export const createSection = (sectionData) => {
    return api.post('/sections', sectionData);
};

export const updateSection = (id, sectionData) => {
    return api.put(`/sections/${id}`, sectionData);
};

export const deleteSection = (id) => {
    return api.delete(`/sections/${id}`);
};

export const getAllSubjects = () => {
    return api.get('/subjects');
};

export const createSubject = (data) => {
    return api.post('/subjects', data);
};

export const updateSubject = (id, data) => {
    return api.put(`/subjects/${id}`, data);
};

export const deleteSubject = (id) => {
    return api.delete(`/subjects/${id}`);
};

export default api;
