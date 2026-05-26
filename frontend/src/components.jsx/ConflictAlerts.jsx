import React, { useEffect, useState } from "react";
import api from "../api/api";

const ConflictAlerts = () => {
  const [conflicts, setConflicts] = useState([]);

  useEffect(() => {
    // Current backend doesn't have a specific /conflicts endpoint yet,
    // so we return 0 for now as a placeholder.
    setConflicts([]);
  }, []);

  return (
    <div className="card-section conflict-alerts-card">
      <h2>Conflict Alerts</h2>
      {conflicts.length === 0 ? (
        <p style={{ color: "green", fontWeight: '500' }}>✅ No active conflicts</p>
      ) : (
        <div className="alert-content">
          <p style={{ color: "#991b1b", fontWeight: '600' }}>⚠️ {conflicts.length} conflicts detected</p>
          <button className="view-details-btn">Fix Issues</button>
        </div>
      )}
    </div>
  );
};

export default ConflictAlerts;
