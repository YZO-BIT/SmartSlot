# SmartSlot Algorithms & Data Structures - Quick Reference

## Data Structures at a Glance

### Collections Used
| Structure | Type | Purpose | Time Complexity |
|-----------|------|---------|-----------------|
| HashSet | Hash Table | Many-to-many relationships | O(1) add/lookup |
| ArrayList | Dynamic Array | Error messages collection | O(1) amortized |
| List | Interface | Query results iteration | O(n) |
| Set | Interface | JPA entity collections | Varies |

---

## Algorithms Summary

### BookingService Algorithms

**createBooking() - Multi-Stage Validation**
- Type: Decision Tree
- Checks: 8 sequential validations with early returns
- Flow: Mandatory break → Security → Elective conflicts → Room conflict → Teacher conflict → Teacher break → Save

**checkTeacherNeedsBreak()**
- Type: Linear Scan + Boolean Tracking
- Logic: Check if teacher has bookings at (slot-1) AND (slot-2)
- Time: O(n) where n = teacher's bookings on day
- Space: O(1)

---

### ElectiveValidationService Algorithms

**validateElectiveGroupConflict()**
- Type: Database JOIN Query
- Finds: Sections in elective group with standard class bookings
- Query: Uses DISTINCT + NULL check for non-elective bookings
- Time: O(m) where m = conflicting sections

**validateSectionElectiveConflict()**
- Type: Inverse Relationship Query
- Finds: Elective groups containing section with bookings
- Time: O(k) where k = elective groups with this section

**validateElectiveGroupCapacity()**
- Type: Stream Aggregation
- Logic: Sum student counts via `stream().mapToInt().sum()`
- Compares: Total students vs. room capacity
- Time: O(s) where s = sections in group

**validateStandardSectionBooking()**
- Type: Conditional Validation
- Only validates elective subjects (early termination for standard subjects)
- Time: O(0) to O(k)

---

### ValidationService Algorithms

**validateRoomAvailability()**
- Type: Nested Stream with short-circuit
- Logic: For each subject, check if room type exists
- For each: `stream().anyMatch(room -> room.getRoomType() == required)`
- Time: O(n*m) worst case, with short-circuit optimization
- Short-circuits on first room match per subject

**validateTeacherExpertise()**
- Type: Nested Stream Search
- Outer: Stream all bookings, filter non-null teacher/subject
- Inner: For each booking, stream teacher's expertise
- Uses: `anyMatch(expertiseSubject -> id.equals(subject.id))`
- Time: O(b*e) with short-circuit where b = bookings, e = expertise

**validateSectionCapacity()**
- Type: Stream Aggregation (mapToInt)
- For each section: Sum `stream().mapToInt(Subject::getLecturesPerWeek).sum()`
- Validates: totalSlots ≤ MAX_WEEKLY_SLOTS (40)
- Time: O(n*m) where n = sections, m = subjects per section
- Optimization: Uses primitive IntStream (no boxing)

**validateAllDataConsistency()**
- Type: Composite Facade
- Combines: Room availability + Teacher expertise + Section capacity
- Time: O(n*m + b*e) = time of slowest validator

---

## Optimization Techniques

| Technique | Where Used | Benefit |
|-----------|-----------|---------|
| **N+1 Query Prevention** | ElectiveGroupRepository JOINs | Single query instead of multiple |
| **Short-Circuit Evaluation** | Stream.anyMatch() | Stops after first match |
| **Primitive IntStream** | validateSectionCapacity() | Avoids Integer boxing |
| **Database Filtering** | JPA @Query with WHERE | Reduces result set early |
| **DISTINCT Elimination** | Complex queries | Removed at DB level |
| **Existence Checks** | Boolean exists() queries | Returns immediately |

---

## Repository Query Patterns

| Method | Query Type | Purpose |
|--------|-----------|---------|
| `existsByRoomIdAndSlotIdAndBookingDate()` | Boolean | O(1) room availability check |
| `existsByTeacherIdAndSlotIdAndBookingDate()` | Boolean | O(1) teacher availability check |
| `findByTeacherIdAndBookingDateOrderBySlotIdAsc()` | Sorted List | Sorted for sequential break checking |
| `findConflictingElectiveGroups()` | Complex JOIN | Multi-JOIN conflict detection |
| `findConflictingSectionsInGroup()` | Complex JOIN | NULL check for booking type |
| `findGroupsBySection()` | Simple JOIN | N+1 prevention via DISTINCT JOIN |

---

## Data Model Relationships

```
Teacher ←→ Subject (expertise)
            ↓
        ┌──Booking──┐
        ├─ teacher
        ├─ subject
        ├─ room ──→ Room
        ├─ section ├→ Section ←→ ElectiveGroup
        └─ elective_group ↓
                    Subject (lectures/week)

Key: Set collections for M2M relationships
     getTotalStudentCount() uses stream aggregation
```

---

## Common Patterns

### Stream Pipeline Pattern
```java
collection.stream()           // Create stream
   .filter(predicate)        // Filter (lazy)
   .map(transformation)      // Transform (lazy)
   .collect(Collectors...)   // Terminal operation
```

### Validation Report Aggregation
```
Run validator 1 → Report 1 (errors + feasibility)
Run validator 2 → Report 2 (errors + feasibility)
Merge: report1.errors + report2.errors
Result: combined report with all errors
```

### Conflict Detection with NULL Check
```
WHERE section has NO elective_group = NULL
     AND section has elective booking
     THEN = CONFLICT
```

---

## Complexity Reference

| Operation | Best Case | Average | Worst Case |
|-----------|-----------|---------|-----------|
| Teacher break check | O(1) | O(n/2) | O(n) |
| Room availability | O(1) | O(n*m/2) | O(n*m) |
| Teacher expertise | O(1) | O(b*e/2) | O(b*e) |
| Section capacity | O(1) skip | O(n*m/2) | O(n*m) |
| All consistency | - | O(n*m + b*e) | O(n*m + b*e) |

Where: n=subjects, m=rooms, b=bookings, e=expertise items

---

## Coding Principles Observed

✅ **Fail-Fast**: Early returns prevent cascading checks
✅ **Null-Safe**: Explicit null checks before operations
✅ **Composable**: Small validators combine into larger ones
✅ **Stream-Based**: Functional programming for collections
✅ **Database-Aware**: Queries optimized at DB level
✅ **Type-Safe**: No raw collections, proper generics

---

## Performance Hotspots

🔴 **High Cost**:
- `findAll()` operations (full table scan)
- Nested loops in validateRoomAvailability()
- Complex multi-JOIN queries with large datasets

🟡 **Medium Cost**:
- Stream aggregations with multiple subjects per section
- Multiple validation checks in createBooking()

🟢 **Low Cost**:
- Boolean existence checks (indexed queries)
- Short-circuit anyMatch() operations
- HashSet lookups

---

## Configuration Key Points

| Key | Value | Relevance |
|-----|-------|-----------|
| MAX_WEEKLY_SLOTS | 40 | Section capacity limit (5 days * 8 slots) |
| Mandatory break slot | 4 | 12:00-13:00 lunch (hardcoded) |
| Required expertise | Mandatory | Teachers must have subject in expertise |
| Room capacity | Validated | Elective group students must fit |
| Teacher break | 2 consecutive classes | Requires 1-hour break after |

---
