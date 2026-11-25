import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import api from '../../services/api';
import {
  Building2,
  MapPin,
  Users,
  CheckCircle2,
  XCircle,
  Clock,
  AlertTriangle,
  IndianRupee,
  CalendarCheck,
  ArrowRight,
  RefreshCw
} from 'lucide-react';

export const Dashboard = () => {
  const { activeBusinessId, activeBusiness } = useBusiness();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchSummary = () => {
    if (!activeBusinessId) return;
    setLoading(true);
    api.get(`/dashboard/summary?businessId=${activeBusinessId}`)
      .then((res) => {
        if (res.data && res.data.data) {
          setSummary(res.data.data);
        }
      })
      .catch((err) => console.error('Error fetching dashboard summary', err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchSummary();
  }, [activeBusinessId]);

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Administrative Dashboard' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Administrative Operations Dashboard</h2>
          <p>
            {activeBusiness ? activeBusiness.businessName : 'Enterprise Operations'} — Daily Workforce & Financial Overview
          </p>
        </div>
        <div>
          <button onClick={fetchSummary} className="gov-btn gov-btn-secondary gov-btn-sm">
            <RefreshCw size={13} className={loading ? 'animate-spin' : ''} /> Refresh Ledger
          </button>
        </div>
      </div>

      {/* Row 1: High-Level Business & Operational KPIs */}
      <div className="gov-kpi-grid">
        <div className="gov-kpi-card blue">
          <div className="gov-kpi-label">Active Construction Sites</div>
          <div className="gov-kpi-value">{summary ? summary.totalActiveSites : '--'}</div>
          <div className="gov-kpi-sub">Assigned across contracting operations</div>
        </div>
        <div className="gov-kpi-card blue">
          <div className="gov-kpi-label">Active Labour Workforce</div>
          <div className="gov-kpi-value">{summary ? summary.totalActiveLabourers : '--'}</div>
          <div className="gov-kpi-sub">Enrolled technicians & helpers</div>
        </div>
        <div className="gov-kpi-card success">
          <div className="gov-kpi-label">Today's Present Workforce</div>
          <div className="gov-kpi-value" style={{ color: 'var(--gov-success)' }}>
            {summary ? summary.todayPresent : '--'}
          </div>
          <div className="gov-kpi-sub">Full shift attendance logged</div>
        </div>
        <div className="gov-kpi-card warning">
          <div className="gov-kpi-label">Today's Half-Day Shifts</div>
          <div className="gov-kpi-value" style={{ color: 'var(--gov-warning)' }}>
            {summary ? summary.todayHalfDay : '--'}
          </div>
          <div className="gov-kpi-sub">Partial daily muster</div>
        </div>
        <div className="gov-kpi-card danger">
          <div className="gov-kpi-label">Today's Absent / Unreported</div>
          <div className="gov-kpi-value" style={{ color: 'var(--gov-danger)' }}>
            {summary ? summary.todayAbsent + summary.todayNotSubmitted : '--'}
          </div>
          <div className="gov-kpi-sub">Absent: {summary?.todayAbsent || 0} | Pending: {summary?.todayNotSubmitted || 0}</div>
        </div>
      </div>

      {/* Row 2: Financial Overview Panel */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Current Period Financial & Wage Disbursement Position</span>
          <Link to="/payroll" className="gov-btn gov-btn-primary gov-btn-sm">
            View Wage Ledger <ArrowRight size={12} />
          </Link>
        </div>
        <div className="gov-card-body">
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '16px' }}>
            <div style={{ padding: '10px', backgroundColor: 'var(--gov-light-blue)', border: '1px solid var(--gov-border)' }}>
              <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-secondary-text)', textTransform: 'uppercase' }}>
                Total Wages Accrued (Month)
              </div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: 'var(--gov-navy)', margin: '4px 0' }}>
                ₹{summary ? summary.periodTotalWages.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
              </div>
              <div style={{ fontSize: '11px', color: '#64748B' }}>Calculated from approved muster</div>
            </div>

            <div style={{ padding: '10px', backgroundColor: '#D4EDDA', border: '1px solid #C3E6CB' }}>
              <div style={{ fontSize: '11px', fontWeight: 700, color: '#155724', textTransform: 'uppercase' }}>
                Disbursed Wage Payments
              </div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#155724', margin: '4px 0' }}>
                ₹{summary ? summary.periodPaymentsMade.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
              </div>
              <div style={{ fontSize: '11px', color: '#155724' }}>Settled via Cash/NEFT/UPI</div>
            </div>

            <div style={{ padding: '10px', backgroundColor: '#FFF3CD', border: '1px solid #FFEEBA' }}>
              <div style={{ fontSize: '11px', fontWeight: 700, color: '#856404', textTransform: 'uppercase' }}>
                Outstanding Advances Balance
              </div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#856404', margin: '4px 0' }}>
                ₹{summary ? summary.totalOutstandingAdvances.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
              </div>
              <div style={{ fontSize: '11px', color: '#856404' }}>Unrecovered worker advances</div>
            </div>

            <div style={{ padding: '10px', backgroundColor: '#F8D7DA', border: '1px solid #F5C6CB' }}>
              <div style={{ fontSize: '11px', fontWeight: 700, color: '#721C24', textTransform: 'uppercase' }}>
                Remaining Payable Wages
              </div>
              <div style={{ fontSize: '20px', fontWeight: 700, color: '#721C24', margin: '4px 0' }}>
                ₹{summary ? summary.pendingPaymentAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 }) : '0.00'}
              </div>
              <div style={{ fontSize: '11px', color: '#721C24' }}>Pending final disbursement</div>
            </div>
          </div>
        </div>
      </div>

      {/* Row 3: Site Overview Muster Table & Action Reminders */}
      <div className="gov-dashboard-split-grid">
        {/* Site Status Table */}
        <div className="gov-card">
          <div className="gov-card-header">
            <span>Operational Sites Muster Summary</span>
            <Link to="/sites" className="gov-btn gov-btn-secondary gov-btn-sm">
              All Sites
            </Link>
          </div>
          <div className="gov-card-body" style={{ padding: 0 }}>
            <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
              <table className="gov-table">
                <thead>
                  <tr>
                    <th>Site Code</th>
                    <th>Site Name & Client</th>
                    <th className="text-center">Assigned Labour</th>
                    <th className="text-center">Present</th>
                    <th className="text-center">Half Day</th>
                    <th className="text-center">Absent</th>
                    <th className="text-center">Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {summary && summary.siteSummaries && summary.siteSummaries.length > 0 ? (
                    summary.siteSummaries.map((s) => (
                      <tr key={s.siteId}>
                        <td className="font-mono" style={{ fontWeight: 600 }}>{s.siteCode}</td>
                        <td>
                          <strong>{s.siteName}</strong>
                        </td>
                        <td className="text-center" style={{ fontWeight: 600 }}>{s.assignedLabour}</td>
                        <td className="text-center" style={{ color: 'var(--gov-success)', fontWeight: 600 }}>
                          {s.presentCount}
                        </td>
                        <td className="text-center" style={{ color: 'var(--gov-warning)', fontWeight: 600 }}>
                          {s.halfDayCount}
                        </td>
                        <td className="text-center" style={{ color: 'var(--gov-danger)', fontWeight: 600 }}>
                          {s.absentCount}
                        </td>
                        <td className="text-center">
                          <StatusBadge status={s.status} />
                        </td>
                        <td>
                          <Link to={`/attendance?siteId=${s.siteId}`} className="gov-btn gov-btn-secondary gov-btn-sm">
                            Muster
                          </Link>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="8" className="text-center" style={{ padding: '16px' }}>
                        No sites registered for this business entity.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Action Reminders Panel */}
        <div className="gov-card">
          <div className="gov-card-header">
            <span>Administrative Action Reminders</span>
          </div>
          <div className="gov-card-body">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <div style={{ padding: '8px 12px', borderLeft: '3px solid var(--gov-warning)', backgroundColor: 'var(--gov-light-grey)' }}>
                <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--gov-navy)' }}>
                  Daily Attendance Submission
                </div>
                <p style={{ fontSize: '12px', color: 'var(--gov-secondary-text)', margin: '2px 0 6px 0' }}>
                  Ensure all on-site supervisors submit the muster roll by 18:00 IST.
                </p>
                <Link to="/attendance" className="gov-btn gov-btn-primary gov-btn-sm">
                  Record Attendance
                </Link>
              </div>

              <div style={{ padding: '8px 12px', borderLeft: '3px solid var(--gov-info)', backgroundColor: 'var(--gov-light-grey)' }}>
                <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--gov-navy)' }}>
                  Monthly Wage Ledger
                </div>
                <p style={{ fontSize: '12px', color: 'var(--gov-secondary-text)', margin: '2px 0 6px 0' }}>
                  Generate and verify the monthly wage sheet before executing advance settlements.
                </p>
                <Link to="/payroll" className="gov-btn gov-btn-secondary gov-btn-sm">
                  Review Payroll
                </Link>
              </div>

              <div style={{ padding: '8px 12px', borderLeft: '3px solid var(--gov-success)', backgroundColor: 'var(--gov-light-grey)' }}>
                <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--gov-navy)' }}>
                  Workforce KYC Audit
                </div>
                <p style={{ fontSize: '12px', color: 'var(--gov-secondary-text)', margin: '2px 0 6px 0' }}>
                  Verify that all active electricians and helpers have complete Aadhar and emergency contacts.
                </p>
                <Link to="/labour" className="gov-btn gov-btn-secondary gov-btn-sm">
                  Worker Directory
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
