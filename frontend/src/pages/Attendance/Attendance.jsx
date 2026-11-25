import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { useAuth } from '../../context/AuthContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Modal } from '../../components/common/Modal';
import api from '../../services/api';
import {
  CalendarCheck,
  Save,
  CheckCheck,
  XCircle,
  Clock,
  Edit2,
  AlertCircle,
  History,
  Users
} from 'lucide-react';

export const Attendance = () => {
  const { activeBusinessId } = useBusiness();
  const { isAccountant, isSupervisor, user } = useAuth();
  const [sites, setSites] = useState([]);
  const [selectedSiteId, setSelectedSiteId] = useState(() => (user?.assignedSiteId ? user.assignedSiteId.toString() : ''));
  const [attendanceDate, setAttendanceDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [statuses, setStatuses] = useState([]);
  const [labourRoster, setLabourRoster] = useState([]);

  const [isCorrectionModalOpen, setIsCorrectionModalOpen] = useState(false);
  const [correctionTarget, setCorrectionTarget] = useState(null);
  const [newStatus, setNewStatus] = useState('PRESENT');
  const [newOvertime, setNewOvertime] = useState('0.00');
  const [correctionReason, setCorrectionReason] = useState('');

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // 1. Fetch available sites and attendance master statuses
  useEffect(() => {
    if (activeBusinessId) {
      api.get(`/sites/all?businessId=${activeBusinessId}`)
        .then((res) => {
          if (res.data?.data && res.data.data.length > 0) {
            setSites(res.data.data);
            if (!selectedSiteId) {
              setSelectedSiteId(res.data.data[0].id.toString());
            }
          } else {
            setSites([]);
          }
        })
        .catch(console.error);

      api.get(`/master-data/values?category=ATTENDANCE_STATUS&businessId=${activeBusinessId}&activeOnly=true`)
        .then((res) => res.data?.data && setStatuses(res.data.data))
        .catch(console.error);
    }
  }, [activeBusinessId]);

  // 2. Fetch or load muster roll whenever site or date changes
  const loadMuster = async () => {
    if (!activeBusinessId || !selectedSiteId || !attendanceDate) return;
    setLoading(true);
    setMessage('');
    setError('');
    try {
      // First check existing recorded attendance for this date & site
      const attRes = await api.get(`/attendance?businessId=${activeBusinessId}&siteId=${selectedSiteId}&date=${attendanceDate}`);
      const existingRecords = attRes.data?.data || [];

      // Also get all active labourers assigned to this site
      const labourRes = await api.get(`/labour/by-site?businessId=${activeBusinessId}&siteId=${selectedSiteId}`);
      const siteWorkers = labourRes.data?.data || [];

      // Merge: if a record exists, use its status & overtime; otherwise initialize with default PRESENT
      const mergedRoster = siteWorkers.map((w) => {
        const found = existingRecords.find((r) => r.labourerId === w.id);
        if (found) {
          return {
            id: found.id, // Existing record ID
            labourerId: w.id,
            labourCode: w.labourCode,
            fullName: w.fullName,
            designation: w.designation,
            dailyWage: w.dailyWage,
            overtimeHourlyRate: w.overtimeHourlyRate || 0,
            status: found.status,
            overtimeHours: found.overtimeHours.toString(),
            calculatedWage: found.calculatedWage,
            overtimeAmount: found.overtimeAmount,
            remarks: found.remarks || '',
            isSaved: true
          };
        } else {
          return {
            id: null,
            labourerId: w.id,
            labourCode: w.labourCode,
            fullName: w.fullName,
            designation: w.designation,
            dailyWage: w.dailyWage,
            overtimeHourlyRate: w.overtimeHourlyRate || 0,
            status: 'PRESENT',
            overtimeHours: '0.00',
            calculatedWage: w.dailyWage,
            overtimeAmount: 0,
            remarks: '',
            isSaved: false
          };
        }
      });

      setLabourRoster(mergedRoster);
    } catch (err) {
      setError('Failed to load site attendance muster');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMuster();
  }, [activeBusinessId, selectedSiteId, attendanceDate]);

  // Bulk status updates
  const setAllStatus = (targetStatus) => {
    setLabourRoster((prev) =>
      prev.map((item) => ({
        ...item,
        status: targetStatus,
        calculatedWage: targetStatus === 'PRESENT' ? item.dailyWage : targetStatus === 'HALF_DAY' ? item.dailyWage * 0.5 : 0
      }))
    );
  };

  const handleRowStatusChange = (index, newStat) => {
    setLabourRoster((prev) => {
      const updated = [...prev];
      const item = updated[index];
      let calcWage = 0;
      if (newStat === 'PRESENT' || newStat === 'PAID_LEAVE' || newStat === 'WEEKLY_OFF' || newStat === 'HOLIDAY') {
        calcWage = item.dailyWage;
      } else if (newStat === 'HALF_DAY') {
        calcWage = item.dailyWage * 0.5;
      }
      updated[index] = { ...item, status: newStat, calculatedWage: calcWage };
      return updated;
    });
  };

  const handleRowOtChange = (index, hours) => {
    setLabourRoster((prev) => {
      const updated = [...prev];
      const item = updated[index];
      const otHrs = parseFloat(hours) || 0;
      const otAmt = otHrs * (item.overtimeHourlyRate || 0);
      updated[index] = { ...item, overtimeHours: hours, overtimeAmount: otAmt };
      return updated;
    });
  };

  const handleRowRemarksChange = (index, rem) => {
    setLabourRoster((prev) => {
      const updated = [...prev];
      updated[index] = { ...updated[index], remarks: rem };
      return updated;
    });
  };

  const handleSaveRoster = async () => {
    if (!selectedSiteId || labourRoster.length === 0) return;
    setSaving(true);
    setMessage('');
    setError('');
    try {
      const payload = {
        businessId: activeBusinessId,
        siteId: parseInt(selectedSiteId, 10),
        attendanceDate: attendanceDate,
        items: labourRoster.map((r) => ({
          labourerId: r.labourerId,
          status: r.status,
          overtimeHours: parseFloat(r.overtimeHours) || 0,
          remarks: r.remarks
        }))
      };

      await api.post('/attendance/bulk', payload);
      setMessage('Attendance muster roll successfully saved and verified.');
      loadMuster();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save attendance muster');
    } finally {
      setSaving(false);
    }
  };

  const openCorrectionDialog = (item) => {
    setCorrectionTarget(item);
    setNewStatus(item.status);
    setNewOvertime(item.overtimeHours);
    setCorrectionReason('');
    setIsCorrectionModalOpen(true);
  };

  const handleSaveCorrection = async () => {
    if (!correctionTarget || !correctionTarget.id) return;
    try {
      await api.post('/attendance/correct', {
        attendanceRecordId: correctionTarget.id,
        newStatus: newStatus,
        newOvertimeHours: parseFloat(newOvertime) || 0,
        reason: correctionReason
      });
      setIsCorrectionModalOpen(false);
      setMessage('Correction audit entry committed.');
      loadMuster();
    } catch (err) {
      alert(err.response?.data?.message || 'Correction failed');
    }
  };

  // Summary counts
  const presentCount = labourRoster.filter((r) => r.status === 'PRESENT').length;
  const halfDayCount = labourRoster.filter((r) => r.status === 'HALF_DAY').length;
  const absentCount = labourRoster.filter((r) => r.status === 'ABSENT').length;
  const leaveCount = labourRoster.filter((r) => ['PAID_LEAVE', 'WEEKLY_OFF', 'HOLIDAY'].includes(r.status)).length;
  const totalDayWage = labourRoster.reduce((sum, r) => sum + (parseFloat(r.calculatedWage) || 0) + (parseFloat(r.overtimeAmount) || 0), 0);

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Daily Muster & Attendance' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Daily Site Muster Roll & Attendance</h2>
          <p>Record, verify, and maintain auditable daily work shifts, overtime, and wage allocations</p>
        </div>
        <div>
          {isAccountant() ? (
            <div className="gov-badge gov-badge-info" style={{ padding: '8px 12px', fontSize: '12px' }}>
              Financial Audit Mode (Muster Read-Only)
            </div>
          ) : (
            <button
              onClick={handleSaveRoster}
              disabled={saving || labourRoster.length === 0}
              className="gov-btn gov-btn-success"
            >
              <Save size={14} /> {saving ? 'Submitting Muster...' : 'Save & Submit Muster Roll'}
            </button>
          )}
        </div>
      </div>

      {isSupervisor() && user?.assignedSiteName && (
        <div className="gov-alert gov-alert-info" style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
          <span>Logging attendance for your deputed site: <strong>{user.assignedSiteName} ({user.assignedSiteCode})</strong>.</span>
        </div>
      )}

      {message && <div className="gov-alert gov-alert-success">{message}</div>}
      {error && <div className="gov-alert gov-alert-danger">{error}</div>}

      {/* Control & Date/Site Selector */}
      <div className="gov-filter-bar">
        <div style={{ width: '280px' }}>
          <label className="gov-form-label required">Select Construction Site</label>
          <select
            className="gov-form-select"
            value={selectedSiteId}
            disabled={isSupervisor() && !!user?.assignedSiteId}
            onChange={(e) => setSelectedSiteId(e.target.value)}
          >
            {sites.map((s) => (
              <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
            ))}
          </select>
        </div>

        <div style={{ width: '180px' }}>
          <label className="gov-form-label required">Muster Date</label>
          <input
            type="date"
            className="gov-form-input"
            value={attendanceDate}
            onChange={(e) => setAttendanceDate(e.target.value)}
          />
        </div>

        {/* Bulk Action Buttons */}
        <div style={{ display: 'flex', gap: '6px', alignItems: 'flex-end', marginLeft: 'auto' }}>
          <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-secondary-text)', alignSelf: 'center', marginRight: '4px' }}>
            BULK ACTIONS:
          </span>
          <button
            type="button"
            onClick={() => setAllStatus('PRESENT')}
            className="gov-btn gov-btn-secondary gov-btn-sm"
          >
            <CheckCheck size={12} color="var(--gov-success)" /> Mark All Present
          </button>
          <button
            type="button"
            onClick={() => setAllStatus('HALF_DAY')}
            className="gov-btn gov-btn-secondary gov-btn-sm"
          >
            <Clock size={12} color="var(--gov-warning)" /> Mark All Half-Day
          </button>
          <button
            type="button"
            onClick={() => setAllStatus('ABSENT')}
            className="gov-btn gov-btn-secondary gov-btn-sm"
          >
            <XCircle size={12} color="var(--gov-danger)" /> Mark All Absent
          </button>
        </div>
      </div>

      {/* Muster Summary Bar */}
      <div className="gov-kpi-grid" style={{ marginBottom: '12px' }}>
        <div className="gov-kpi-card blue">
          <div className="gov-kpi-label">Workers on Roster</div>
          <div className="gov-kpi-value">{labourRoster.length}</div>
        </div>
        <div className="gov-kpi-card success">
          <div className="gov-kpi-label">Full Day Present</div>
          <div className="gov-kpi-value" style={{ color: 'var(--gov-success)' }}>{presentCount}</div>
        </div>
        <div className="gov-kpi-card warning">
          <div className="gov-kpi-label">Half Day</div>
          <div className="gov-kpi-value" style={{ color: 'var(--gov-warning)' }}>{halfDayCount}</div>
        </div>
        <div className="gov-kpi-card danger">
          <div className="gov-kpi-label">Absent (Unpaid)</div>
          <div className="gov-kpi-value" style={{ color: 'var(--gov-danger)' }}>{absentCount}</div>
        </div>
        <div className="gov-kpi-card blue">
          <div className="gov-kpi-label">Estimated Day Wages</div>
          <div className="gov-kpi-value" style={{ fontSize: '18px' }}>
            ₹{totalDayWage.toFixed(2)}
          </div>
        </div>
      </div>

      {/* Daily Muster Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>
            Daily Muster Roll — Date: <strong>{attendanceDate}</strong>
          </span>
          <span style={{ fontSize: '12px' }}>
            {labourRoster.filter((r) => r.isSaved).length === labourRoster.length && labourRoster.length > 0 ? (
              <span style={{ color: 'var(--gov-success)', fontWeight: 700 }}>● ALL ENTRIES SUBMITTED</span>
            ) : (
              <span style={{ color: 'var(--gov-warning)', fontWeight: 700 }}>● UNSAVED CHANGES PRESENT</span>
            )}
          </span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th style={{ width: '40px' }}>#</th>
                  <th>Worker ID</th>
                  <th>Worker Name</th>
                  <th>Designation</th>
                  <th className="text-right">Daily Wage (₹)</th>
                  <th style={{ width: '160px' }}>Shift Attendance</th>
                  <th style={{ width: '110px' }}>Overtime (Hrs)</th>
                  <th className="text-right">OT Earned (₹)</th>
                  <th className="text-right">Day Total (₹)</th>
                  <th>Remarks / Station Note</th>
                  <th className="text-center">Audit</th>
                </tr>
              </thead>
              <tbody>
                {labourRoster.length > 0 ? (
                  labourRoster.map((item, idx) => (
                    <tr key={item.labourerId}>
                      <td className="text-center">{idx + 1}</td>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{item.labourCode}</td>
                      <td>
                        <strong>{item.fullName}</strong>
                      </td>
                      <td>{item.designation}</td>
                      <td className="text-right font-mono">₹{item.dailyWage.toFixed(2)}</td>
                      <td>
                        <select
                          className="gov-form-select"
                          value={item.status}
                          onChange={(e) => handleRowStatusChange(idx, e.target.value)}
                          style={{
                            fontWeight: 700,
                            borderColor:
                              item.status === 'PRESENT' ? 'var(--gov-success)' :
                              item.status === 'HALF_DAY' ? 'var(--gov-warning)' :
                              item.status === 'ABSENT' ? 'var(--gov-danger)' : 'var(--gov-border)'
                          }}
                        >
                          <option value="PRESENT">Full Day Present</option>
                          <option value="HALF_DAY">Half Day</option>
                          <option value="ABSENT">Absent (Unpaid)</option>
                          <option value="PAID_LEAVE">Sanctioned Paid Leave</option>
                          <option value="WEEKLY_OFF">Weekly Off</option>
                          <option value="HOLIDAY">Gazetted Holiday</option>
                        </select>
                      </td>
                      <td>
                        <input
                          type="number"
                          step="0.5"
                          min="0"
                          max="16"
                          className="gov-form-input font-mono text-center"
                          value={item.overtimeHours}
                          onChange={(e) => handleRowOtChange(idx, e.target.value)}
                        />
                      </td>
                      <td className="text-right font-mono" style={{ color: item.overtimeAmount > 0 ? 'var(--gov-blue)' : undefined }}>
                        ₹{(parseFloat(item.overtimeAmount) || 0).toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700 }}>
                        ₹{((parseFloat(item.calculatedWage) || 0) + (parseFloat(item.overtimeAmount) || 0)).toFixed(2)}
                      </td>
                      <td>
                        <input
                          type="text"
                          className="gov-form-input"
                          value={item.remarks}
                          onChange={(e) => handleRowRemarksChange(idx, e.target.value)}
                          placeholder="Shift remarks"
                        />
                      </td>
                      <td className="text-center">
                        {item.isSaved ? (
                          <button
                            type="button"
                            onClick={() => openCorrectionDialog(item)}
                            className="gov-btn gov-btn-secondary gov-btn-sm"
                            title="Correct entry with audit reason"
                          >
                            <Edit2 size={11} /> Correct
                          </button>
                        ) : (
                          <span style={{ fontSize: '11px', color: '#64748B' }}>New</span>
                        )}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="11" className="text-center" style={{ padding: '24px' }}>
                      {loading ? 'Loading site roster...' : 'No active labourers are currently assigned to this site. Assign workers under Labour Management.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Attendance Correction Modal with Mandatory Audit Trail */}
      <Modal
        isOpen={isCorrectionModalOpen}
        onClose={() => setIsCorrectionModalOpen(false)}
        title={`Audit Correction: ${correctionTarget?.fullName} (${correctionTarget?.labourCode})`}
        size="default"
      >
        <div style={{ backgroundColor: 'var(--gov-light-blue)', border: '1px solid var(--gov-border)', padding: '10px 12px', fontSize: '12px', marginBottom: '14px' }}>
          <strong>Compliance Rule:</strong> Modifying an existing submitted muster creates an immutable correction trail documenting the previous values, new values, modifying user, and justification reason.
        </div>

        <div className="gov-form-group">
          <label className="gov-form-label">Previous Submitted Status</label>
          <input
            type="text"
            disabled
            className="gov-form-input"
            value={correctionTarget?.status || ''}
          />
        </div>

        <div className="gov-form-group">
          <label className="gov-form-label required">New Corrected Status</label>
          <select
            className="gov-form-select"
            value={newStatus}
            onChange={(e) => setNewStatus(e.target.value)}
          >
            <option value="PRESENT">Full Day Present</option>
            <option value="HALF_DAY">Half Day</option>
            <option value="ABSENT">Absent (Unpaid)</option>
            <option value="PAID_LEAVE">Sanctioned Paid Leave</option>
            <option value="WEEKLY_OFF">Weekly Off</option>
            <option value="HOLIDAY">Gazetted Holiday</option>
          </select>
        </div>

        <div className="gov-form-group">
          <label className="gov-form-label">Corrected Overtime Hours</label>
          <input
            type="number"
            step="0.5"
            className="gov-form-input font-mono"
            value={newOvertime}
            onChange={(e) => setNewOvertime(e.target.value)}
          />
        </div>

        <div className="gov-form-group">
          <label className="gov-form-label required">Mandatory Justification / Correction Reason</label>
          <textarea
            required
            className="gov-form-textarea"
            rows={3}
            value={correctionReason}
            onChange={(e) => setCorrectionReason(e.target.value)}
            placeholder="e.g. Supervisor logged absent by mistake; biometric gate log confirmed attendance at 08:30..."
          />
        </div>

        <div className="gov-modal-footer">
          <button type="button" onClick={() => setIsCorrectionModalOpen(false)} className="gov-btn gov-btn-secondary">
            Cancel
          </button>
          <button
            type="button"
            disabled={!correctionReason.trim()}
            onClick={handleSaveCorrection}
            className="gov-btn gov-btn-primary"
          >
            Commit Audit Correction
          </button>
        </div>
      </Modal>
    </div>
  );
};
