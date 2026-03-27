# SmartSlot - Frontend Architecture & Design

**Project:** SmartSlot - Intelligent College Timetable Generation Engine  
**Component:** Frontend Application  
**Status:** ❌ NOT STARTED (0% complete)  
**Tech Stack:** React.js / Angular (To be decided)

---

## 📱 Frontend Overview

The frontend is a web-based admin dashboard and teacher portal for managing college timetables, bookings, and schedules. It communicates with the backend via REST API.

**Target Users:**
- 👨‍💼 College Administrators - Full system management
- 👨‍🏫 Heads of Department (HOD) - Approval workflows
- 👨‍🎓 Teachers - View personal timetable, request changes

---

## 🏗️ Frontend Architecture

```
Frontend Application (React/Angular)
        ↓
REST API Client (Axios)
        ↓
Backend API (Spring Boot)
        ↓
Database (H2/PostgreSQL)
```

---

## 📄 Frontend Pages (8 Required)

### 1️⃣ **Authentication & Login Page**

**Purpose:** User authentication and role-based access

**Components:**
- Login form with email + password
- Role selector (Admin / HOD / Teacher)
- "Forgot Password" link
- "Remember Me" checkbox
- JWT token storage

**Features:**
- ✅ Login validation
- ✅ JWT token management
- ✅ Session persistence
- ✅ Redirect to dashboard after login
- ✅ Logout functionality

**Tech Stack:**
- React Hook Form / Reactive Forms (Angular)
- JWT authentication
- localStorage for token storage

**Status:** ❌ Not started

---

### 2️⃣ **Admin Dashboard**

**Purpose:** Overview of entire timetable system with key metrics

**Layout:**
```
┌─────────────────────────────────────────┐
│  SmartSlot Dashboard                    │
├─────────────────────────────────────────┤
│  Quick Stats                            │
│  ┌─────────────────────────────────┐   │
│  │ Teachers: 13  │ Rooms: 6        │   │
│  │ Sections: 4   │ Bookings: 45    │   │
│  └─────────────────────────────────┘   │
├─────────────────────────────────────────┤
│  Timetable Overview (7 days × 8 slots)  │
│  ┌─────────────────────────────────┐   │
│  │ Mon Tue Wed Thu Fri Sat Sun      │   │
│  │ [Slot1]                         │   │
│  │ [Slot2]                         │   │
│  │ ...                             │   │
│  └─────────────────────────────────┘   │
├─────────────────────────────────────────┤
│  Recent Bookings                        │
│  ┌─────────────────────────────────┐   │
│  │ Dr. Sharma - CR101 - 2:00 PM    │   │
│  │ Dr. Patel - LAB-01 - 3:00 PM    │   │
│  └─────────────────────────────────┘   │
├─────────────────────────────────────────┤
│  Conflict Alerts                        │
│  ┌─────────────────────────────────┐   │
│  │ ⚠️ 3 conflicts detected         │   │
│  │ → View Details                  │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

**Components:**
- Quick stats cards (Teachers, Rooms, Sections, Bookings count)
- Weekly timetable grid
- Recent activity feed
- Conflict alerts section

**Features:**
- ✅ Real-time stats from API
- ✅ Color-coded conflicts
- ✅ Click to view details
- ✅ Refresh button

**Status:** ❌ Not started

---

### 3️⃣ **Teacher Management Page**

**Purpose:** Add, edit, delete teachers and manage their expertise

**Features:**
```
┌──────────────────────────────────┐
│ Teacher Management               │
├──────────────────────────────────┤
│ [Search box] [Add New Teacher]   │
├──────────────────────────────────┤
│ Table of teachers:               │
│ ┌──────────────────────────────┐ │
│ │ Name │ Email │ Phone │ Actions│ │
│ ├──────────────────────────────┤ │
│ │ Dr. Sharma │ ... │ Edit|Delete│ │
│ │ Dr. Patel  │ ... │ Edit|Delete│ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘

Add Teacher Modal:
┌──────────────────────────────────┐
│ Add New Teacher                  │
├──────────────────────────────────┤
│ Name: [           ]              │
│ Email: [          ]              │
│ Phone: [          ]              │
│ Expertise Areas:                 │
│ ☑ Data Structures               │
│ ☑ Operating Systems            │
│ ☑ Database Systems             │
│ ☐ NEW_AUDI Access              │
│ [Cancel] [Save]                 │
└──────────────────────────────────┘
```

**CRUD Operations:**
- ✅ **Create** - Add new teacher with expertise areas
- ✅ **Read** - List all teachers with search/filter
- ✅ **Update** - Edit teacher details and expertise
- ✅ **Delete** - Remove teacher with confirmation

**Features:**
- ✅ Multi-select expertise areas
- ✅ Toggle for auditorium access (NEW_AUDI)
- ✅ Bulk import from CSV
- ✅ Validation (email format, phone format)

**Status:** ❌ Not started

---

### 4️⃣ **Room Management Page**

**Purpose:** Manage classrooms, labs, and auditorium rooms

**Features:**
```
Room Types:
🏫 CR (Classroom) - Normal classes
🧪 LAB (Laboratory) - Practical labs
🎓 LT (Lecture Hall) - Large lectures
🎤 NEW_AUDI (Auditorium) - Big events

Room Management:
┌──────────────────────────────────┐
│ [Filter by Type] [Add New Room]  │
├──────────────────────────────────┤
│ Room │ Type │ Capacity │ Status  │
│ CR101│ CR   │ 70       │ Active  │
│ LAB01│ LAB  │ 50       │ Active  │
│ AUDI │ NEW  │ 500      │ Limited │
└──────────────────────────────────┘
```

**CRUD Operations:**
- ✅ **Create** - Add room with type and capacity
- ✅ **Read** - List with type filter
- ✅ **Update** - Edit room capacity
- ✅ **Delete** - Remove room (validate no bookings)

**Status:** ❌ Not started

---

### 5️⃣ **Section Management Page**

**Purpose:** Manage student sections and their subjects

**Features:**
```
Sections:
Section A1 | Year 2024 | 68 Students
├── Data Structures
├── Operating Systems
└── Database Systems

Section A2 | Year 2024 | 70 Students
├── Data Structures
├── Web Development
└── Cloud Computing
```

**CRUD Operations:**
- ✅ **Create** - New section with name, year, student count
- ✅ **Read** - List all sections with subjects
- ✅ **Update** - Edit student count, manage subjects
- ✅ **Delete** - Remove section

**Features:**
- ✅ Add/remove subjects from section (multi-select)
- ✅ View section timetable
- ✅ Student count tracking

**Status:** ❌ Not started

---

### 6️⃣ **Subject Management Page**

**Purpose:** Configure subjects and their requirements

**Features:**
```
Subject: Data Structures
├── Code: CS201
├── Room Type Required: LAB (Laboratory)
├── Min Lectures/Week: 2
├── Is Theory/Practical: Both
└── Split Allowed: Yes

Subject Configuration:
┌────────────────────────────┐
│ Subject Name: [          ] │
│ Code: [                  ] │
│ Room Type: [CR ▼]          │
│ Min Lectures/Week: [2]     │
│ ☑ Theory    ☑ Practical   │
│ ☑ Split Allowed            │
│ [Save]                     │
└────────────────────────────┘
```

**CRUD Operations:**
- ✅ **Create** - New subject with requirements
- ✅ **Read** - List all subjects
- ✅ **Update** - Change room type, lecture frequency
- ✅ **Delete** - Remove subject

**Status:** ❌ Not started

---

### 7️⃣ **Booking Management Page**

**Purpose:** Create, view, edit, and delete timetable bookings

**Features:**
```
Booking Calendar:
┌──────────────────────────────────┐
│ Mon  Tue  Wed  Thu  Fri  Sat Sun │
├──────────────────────────────────┤
│  9-10 [Dr. Sharma - CR101]       │
│ 10-11 [          ]               │
│ 11-12 [Dr. Patel - LAB01]        │
│ 12-1  [LUNCH BREAK]              │
│  1-2  [Dr. Kumar - LT101]        │
│  2-3  [          ]               │
│  3-4  [Dr. Singh - CR102]        │
│  4-5  [          ]               │
└──────────────────────────────────┘
```

**CRUD Operations:**
- ✅ **Create** - New booking with validation
- ✅ **Read** - Calendar/grid view with filters
- ✅ **Update** - Edit existing bookings
- ✅ **Delete** - Cancel bookings

**Features:**
- ✅ Drag-and-drop to reschedule
- ✅ Conflict warning display
- ✅ Filter by teacher/section/room/date
- ✅ Bulk upload from CSV file
- ✅ Real-time validation error messages

**Status:** ❌ Not started

---

### 8️⃣ **Elective Group Builder Page**

**Purpose:** Create and manage multi-section elective groups

**Features:**
```
Elective Group: AI & Machine Learning
├── Section A1 (68 students)
├── Section A2 (70 students)
├── Section B1 (68 students)
└── Total: 206 students

Builder UI:
┌──────────────────────────────────┐
│ Elective: [AI & ML           ]   │
│ Description: [               ]   │
├──────────────────────────────────┤
│ Select Sections:                 │
│ ☑ Section A1 (68)               │
│ ☑ Section A2 (70)               │
│ ☑ Section B1 (68)               │
│ ☐ Section B2 (72)               │
│ Total Students: 206              │
├──────────────────────────────────┤
│ ⚠️ No room fits 206 students!    │
│ Suggested: LT101 (200) - TIGHT   │
├──────────────────────────────────┤
│ [Cancel] [Save Group]            │
└──────────────────────────────────┘
```

**CRUD Operations:**
- ✅ **Create** - New elective with section selection
- ✅ **Read** - List all elective groups
- ✅ **Update** - Add/remove sections
- ✅ **Delete** - Remove elective group

**Features:**
- ✅ Multi-section checkbox selection
- ✅ Real-time total student count
- ✅ Room capacity warnings
- ✅ Conflict detection display

**Status:** ❌ Not started

---

## 📊 Reporting & Analytics Pages (Optional)

### ❌ Conflict Report
- List all scheduling conflicts
- Color-coded severity levels
- Detailed explanations
- Proposed solutions

### ❌ Room Utilization Heatmap
- Grid: Rooms × Time Slots
- Color intensity = occupancy %
- Identify underutilized rooms

### ❌ Teacher Load Analysis
- Weekly hours per teacher
- Teaching pattern analysis
- Workload fairness metrics

---

## 🔌 REST API Integration

**Base URL:** `http://localhost:8080/api`

### API Calls from Frontend

```javascript
// Booking API
GET    /api/bookings                 // Get all bookings
POST   /api/bookings                 // Create booking
GET    /api/bookings/:id             // Get booking details
PATCH  /api/bookings/:id             // Update booking
DELETE /api/bookings/:id             // Delete booking

// Teacher API
GET    /api/teachers                 // Get all teachers
POST   /api/teachers                 // Create teacher
PATCH  /api/teachers/:id             // Update teacher
DELETE /api/teachers/:id             // Delete teacher

// Room API
GET    /api/rooms                    // Get all rooms
POST   /api/rooms                    // Create room

// Section API
GET    /api/sections                 // Get all sections
POST   /api/sections                 // Create section

// Subject API
GET    /api/subjects                 // Get all subjects
POST   /api/subjects                 // Create subject

// Elective API
GET    /api/electives                // Get all electives
POST   /api/electives                // Create elective

// Validation API
GET    /api/validate/data-consistency
GET    /api/validate/rooms
GET    /api/validate/teachers
```

---

## 🎨 UI/UX Design Principles

- **Dashboard-First:** Admin sees overview immediately
- **Color Coding:** 
  - 🟢 Green = Available/Success
  - 🔴 Red = Conflict/Error
  - 🟡 Yellow = Warning
  - 🔵 Blue = Info
  
- **Drag-and-Drop:** Reschedule by dragging bookings
- **Real-Time Validation:** Show errors as you type
- **Mobile Responsive:** Works on desktop and tablet

---

## 📦 Technology Stack (Recommended)

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Framework** | React 18+ OR Angular 17+ | UI framework |
| **UI Library** | Material-UI / Shadcn / Bootstrap | Component library |
| **State Management** | Redux / Context API | Global state |
| **HTTP Client** | Axios | API calls |
| **Routing** | React Router / Angular Router | Page navigation |
| **Charts** | Recharts / Chart.js | Graph visualizations |
| **Drag-Drop** | React Beautiful DnD | Drag functionality |
| **Form Validation** | React Hook Form | Form handling |
| **Date Picker** | React DatePicker | Calendar selection |
| **Build Tool** | Webpack / Vite | Build bundler |

---

## 🚀 Development Tasks

**Week 1-2:** Basic Setup & Auth
- [ ] Project initialization (React/Angular)
- [ ] Authentication page + JWT setup
- [ ] Navigation menu
- [ ] Dashboard skeleton

**Week 3:** Core Management Pages
- [ ] Teacher management CRUD
- [ ] Room management CRUD
- [ ] Section management CRUD
- [ ] Subject management CRUD

**Week 4:** Booking & Electives
- [ ] Booking calendar with drag-drop
- [ ] Elective group builder
- [ ] Validation error display
- [ ] Search/filter functionality

**Week 5:** Analytics (Optional)
- [ ] Conflict report dashboard
- [ ] Room utilization heatmap
- [ ] Teacher load analysis

---

## 💾 Frontend-Backend Communication

**Request Flow:**
```
User fills form
    ↓
Frontend validates inputs
    ↓
Sends POST/PATCH to Backend API
    ↓
Backend validates all rules
    ↓
Returns error (if any) with message
    ↓
Frontend displays error to user
    ↓
User corrects and retries
```

**Example - Create Booking:**
```javascript
// Frontend
const response = await axios.post('/api/bookings', {
  teacherId: 1,
  sectionId: 1,
  roomId: 1,
  subjectId: 1,
  slotId: 4,  // 12-1 PM (lunch break)
  bookingDate: '2026-04-15'
});

// If conflict detected:
{
  error: "Error: 12:00 - 1:00 PM is a mandatory lunch break for everyone!"
}

// Frontend shows error to user with red banner
```

---

## 📝 File Structure (Once Created)

```
frontend/
├── public/
├── src/
│   ├── components/
│   │   ├── Navigation.jsx
│   │   ├── Dashboard/
│   │   ├── Teachers/
│   │   ├── Rooms/
│   │   ├── Sections/
│   │   ├── Subjects/
│   │   ├── Bookings/
│   │   ├── Electives/
│   │   └── Auth/
│   ├── pages/
│   ├── services/ (API calls)
│   ├── store/ (Redux/Context)
│   ├── styles/
│   ├── App.jsx
│   └── index.jsx
├── package.json
└── .env (API URL, credentials)
```

---

## 🔐 Security Considerations

- JWT token storage in localStorage
- Role-based access control (Admin/HOD/Teacher)
- HTTPS required in production
- Input validation before API calls
- CORS handling with backend
- Token refresh mechanism

---

## 📊 Progress Tracking

| Component | Status | Effort |
|-----------|--------|--------|
| **Pages (8)** | ❌ 0% | 20-26 hours |
| **API Integration** | ❌ 0% | 3-4 hours |
| **Authentication** | ❌ 0% | 2-3 hours |
| **Styling/Responsive** | ❌ 0% | 5-6 hours |
| **Testing** | ❌ 0% | 3-4 hours |
| **Documentation** | ❌ 0% | 2 hours |
| **Total Frontend** | **0%** | **35-45 hours** |

---

## Summary

The frontend is a comprehensive admin dashboard and teacher portal with:
- 8 core management pages
- Real-time validation feedback
- Drag-and-drop scheduling
- REST API integration
- Role-based access control
- Mobile-responsive design

**Ready for:** Frontend development to begin after backend testing completes (Week 2 of project)

---

**Last Updated:** 26 March 2026  
**For Questions:** Contact Development Team
