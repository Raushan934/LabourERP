import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import api from '../../services/api';
import {
  FileText,
  Download,
  Printer,
  Calendar,
  Building,
  Users,
  Search,
  CheckCircle,
  IndianRupee
} from 'lucide-react';

export const Reports = () => {
  const [searchParams] = useSearchParams();
  const { activeBusinessId } = useBusiness();
  const [activeTab, setActiveTab] = useState('statement'); // 'statement' or 'site'
  const [labourers, setLabourers] = useState([]);
  const [sites, setSites] = useState([]);

  // Labour Statement Filters
  const [selectedLabourerId, setSelectedLabourerId] = useState(searchParams.get('labourId') || '');
  const [statementStart, setStatementStart] = useState(() => {
    const d = new Date();
    d.setDate(1);
    return d.toISOString().split('T')[0];
  });
  const [statementEnd, setStatementEnd] = useState(() => new Date().toISOString().split('T')[0]);
  const [statementData, setStatementData] = useState(null);

  // Site Monthly Report Filters
  const [selectedSiteId, setSelectedSiteId] = useState('');
  const [siteMonth, setSiteMonth] = useState(new Date().getMonth() + 1);
  const [siteYear, setSiteYear] = useState(new Date().getFullYear());
  const [siteReportData, setSiteReportData] = useState(null);

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (activeBusinessId) {
      api.get(`/labour?businessId=${activeBusinessId}&size=100`)
        .then((res) => {
          if (res.data?.data?.content && res.data.data.content.length > 0) {
            setLabourers(res.data.data.content);
            if (!selectedLabourerId) {
              setSelectedLabourerId(res.data.data.content[0].id.toString());
            }
          }
        })
        .catch(console.error);

      api.get(`/sites/all?businessId=${activeBusinessId}`)
        .then((res) => {
          if (res.data?.data && res.data.data.length > 0) {
            setSites(res.data.data);
            if (!selectedSiteId) {
              setSelectedSiteId(res.data.data[0].id.toString());
            }
          }
        })
        .catch(console.error);
    }
  }, [activeBusinessId]);

  const fetchStatement = async () => {
    if (!selectedLabourerId) return;
    setLoading(true);
    try {
      const res = await api.get(`/reports/labour-statement/${selectedLabourerId}?startDate=${statementStart}&endDate=${statementEnd}`);
      if (res.data?.data) {
        setStatementData(res.data.data);
      }
    } catch (err) {
      console.error('Failed to load statement', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSiteReport = async () => {
    if (!selectedSiteId) return;
    setLoading(true);
    try {
      const res = await api.get(`/reports/site-monthly?siteId=${selectedSiteId}&month=${siteMonth}&year=${siteYear}`);
      if (res.data?.data) {
        setSiteReportData(res.data.data);
      }
    } catch (err) {
      console.error('Failed to load site report', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'statement' && selectedLabourerId) {
      fetchStatement();
    } else if (activeTab === 'site' && selectedSiteId) {
      fetchSiteReport();
    }
  }, [activeTab, selectedLabourerId, selectedSiteId]);

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadCsv = () => {
    if (!selectedLabourerId) return;
    window.open(`/api/v1/reports/labour-statement/${selectedLabourerId}/csv?startDate=${statementStart}&endDate=${statementEnd}`, '_blank');
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Administrative Reports' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Workforce Operational & Statutory Reports</h2>
          <p>Generate formal worker statements, site attendance matrices, and audit ledgers</p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button onClick={handlePrint} className="gov-btn gov-btn-secondary">
            <Printer size={13} /> Print Report
          </button>
          {activeTab === 'statement' && (
            <button onClick={handleDownloadCsv} className="gov-btn gov-btn-primary">
              <Download size={13} /> Export CSV
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', borderBottom: '2px solid var(--gov-navy)', marginBottom: '16px' }}>
        <button
          onClick={() => setActiveTab('statement')}
          style={{
            padding: '8px 16px',
            fontSize: '13px',
            fontWeight: 700,
            cursor: 'pointer',
            border: 'none',
            backgroundColor: activeTab === 'statement' ? 'var(--gov-navy)' : '#E2E8F0',
            color: activeTab === 'statement' ? '#FFFFFF' : 'var(--gov-text)',
            borderRadius: '2px 2px 0 0'
          }}
        >
          1. Individual Labour Statement
        </button>
        <button
          onClick={() => setActiveTab('site')}
          style={{
            padding: '8px 16px',
            fontSize: '13px',
            fontWeight: 700,
            cursor: 'pointer',
            border: 'none',
            backgroundColor: activeTab === 'site' ? 'var(--gov-navy)' : '#E2E8F0',
            color: activeTab === 'site' ? '#FFFFFF' : 'var(--gov-text)',
            borderRadius: '2px 2px 0 0',
            marginLeft: '4px'
          }}
        >
          2. Site-Wise Monthly Summary
        </button>
      </div>

      {/* Tab 1: Individual Labour Statement */}
      {activeTab === 'statement' && (
        <div>
          {/* Filter Bar */}
          <div className="gov-filter-bar">
            <div style={{ width: '280px' }}>
              <label className="gov-form-label required">Select Worker</label>
              <select
                className="gov-form-select"
                value={selectedLabourerId}
                onChange={(e) => setSelectedLabourerId(e.target.value)}
              >
                {labourers.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.fullName} ({l.labourCode}) — {l.designation}
                  </option>
                ))}
              </select>
            </div>

            <div style={{ width: '160px' }}>
              <label className="gov-form-label required">From Date</label>
              <input
                type="date"
                className="gov-form-input"
                value={statementStart}
                onChange={(e) => setStatementStart(e.target.value)}
              />
            </div>

            <div style={{ width: '160px' }}>
              <label className="gov-form-label required">To Date</label>
              <input
                type="date"
                className="gov-form-input"
                value={statementEnd}
                onChange={(e) => setStatementEnd(e.target.value)}
              />
            </div>

            <div style={{ alignSelf: 'flex-end' }}>
              <button onClick={fetchStatement} className="gov-btn gov-btn-secondary">
                <Search size={13} /> Generate Statement
              </button>
            </div>
          </div>

          {statementData && (
            <div className="gov-card" style={{ border: '2px solid var(--gov-navy)' }}>
              {/* Official Header */}
              <div style={{ backgroundColor: 'var(--gov-light-blue)', borderBottom: '1px solid var(--gov-border)', padding: '16px', textAlign: 'center' }}>
                <div style={{ fontSize: '11px', fontWeight: 700, letterSpacing: '0.5px', color: 'var(--gov-navy)' }}>
                  ENTERPRISE COMPLIANCE FORMAT — FORM VII (WORKFORCE SERVICE LEDGER)
                </div>
                <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-navy)', margin: '4px 0' }}>
                  INDIVIDUAL LABOUR WAGE & SERVICE STATEMENT
                </h3>
                <div style={{ fontSize: '12px', color: 'var(--gov-secondary-text)' }}>
                  Enterprise: <strong>{statementData.businessName}</strong> | Period: <strong>{statementData.startDate} to {statementData.endDate}</strong>
                </div>
              </div>

              {/* Profile Details Panel */}
              <div style={{ padding: '16px', borderBottom: '1px solid var(--gov-border)', backgroundColor: '#FFFFFF' }}>
                <div className="gov-report-meta-grid">
                  <div>
                    <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Labour Code / ID:</span>
                    <div className="font-mono" style={{ fontWeight: 700 }}>{statementData.labourCode}</div>
                  </div>
                  <div>
                    <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Full Name:</span>
                    <div style={{ fontWeight: 700 }}>{statementData.fullName}</div>
                  </div>
                  <div>
                    <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Designation & Skill:</span>
                    <div style={{ fontWeight: 600 }}>{statementData.designation} ({statementData.labourCategory})</div>
                  </div>
                  <div>
                    <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Base Daily Wage:</span>
                    <div className="font-mono" style={{ fontWeight: 700, color: 'var(--gov-navy)' }}>₹{statementData.dailyWage?.toFixed(2)}</div>
                  </div>
                </div>
              </div>

              {/* Financial Balance Strip */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '1px', backgroundColor: 'var(--gov-border)', borderBottom: '1px solid var(--gov-border)' }}>
                <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                  <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-secondary-text)' }}>GROSS EARNINGS</div>
                  <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-navy)' }}>₹{statementData.grossEarnings?.toFixed(2)}</div>
                  <div style={{ fontSize: '11px', color: '#64748B' }}>{statementData.totalPresentDays} present / {statementData.totalHalfDays} half</div>
                </div>
                <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                  <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-secondary-text)' }}>ADVANCES RECEIVED</div>
                  <div style={{ fontSize: '18px', fontWeight: 700, color: '#D97706' }}>₹{statementData.totalAdvancesReceived?.toFixed(2)}</div>
                  <div style={{ fontSize: '11px', color: '#64748B' }}>Recovered: ₹{statementData.totalAdvancesRecovered?.toFixed(2)}</div>
                </div>
                <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                  <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-secondary-text)' }}>ADVANCE BALANCE</div>
                  <div style={{ fontSize: '18px', fontWeight: 700, color: statementData.outstandingAdvanceBalance > 0 ? 'var(--gov-danger)' : 'var(--gov-navy)' }}>
                    ₹{statementData.outstandingAdvanceBalance?.toFixed(2)}
                  </div>
                  <div style={{ fontSize: '11px', color: '#64748B' }}>Unsettled loans</div>
                </div>
                <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                  <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-secondary-text)' }}>DISBURSED PAYMENTS</div>
                  <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-success)' }}>₹{statementData.totalPaymentsReceived?.toFixed(2)}</div>
                  <div style={{ fontSize: '11px', color: '#64748B' }}>Cash/UPI/NEFT</div>
                </div>
                <div style={{ backgroundColor: 'var(--gov-light-blue)', padding: '10px 14px' }}>
                  <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-navy)' }}>NET OUTSTANDING PAYABLE</div>
                  <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-navy)' }}>
                    ₹{statementData.netOutstandingPayable?.toFixed(2)}
                  </div>
                  <div style={{ fontSize: '11px', color: '#64748B' }}>Remaining balance due</div>
                </div>
              </div>

              {/* Detailed Daily Attendance Section */}
              <div style={{ padding: '16px' }}>
                <h4 style={{ fontSize: '13px', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '8px' }}>
                  Daily Muster Roll & Overtime Ledger
                </h4>
                <div className="gov-table-container">
                  <table className="gov-table">
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Site Location</th>
                        <th>Shift Status</th>
                        <th className="text-right">Overtime Hours</th>
                        <th className="text-right">Calculated Wage (₹)</th>
                      </tr>
                    </thead>
                    <tbody>
                      {statementData.attendanceDetails && statementData.attendanceDetails.length > 0 ? (
                        statementData.attendanceDetails.map((a, idx) => (
                          <tr key={idx}>
                            <td>{a.date}</td>
                            <td>{a.siteName}</td>
                            <td>
                              <span style={{ fontWeight: 600 }}>{a.status}</span>
                            </td>
                            <td className="text-right font-mono">{a.overtimeHours} hrs</td>
                            <td className="text-right font-mono" style={{ fontWeight: 600 }}>
                              ₹{a.wageEarned?.toFixed(2)}
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan="5" className="text-center" style={{ padding: '12px' }}>
                            No muster attendance logged during this period.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>

                {/* Advances and Payments Split Tables */}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginTop: '16px' }}>
                  <div>
                    <h4 style={{ fontSize: '13px', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '8px' }}>
                      Advances Disbursed History
                    </h4>
                    <div className="gov-table-container">
                      <table className="gov-table">
                        <thead>
                          <tr>
                            <th>Voucher</th>
                            <th>Date</th>
                            <th className="text-right">Amount (₹)</th>
                            <th>Status</th>
                          </tr>
                        </thead>
                        <tbody>
                          {statementData.advanceDetails && statementData.advanceDetails.length > 0 ? (
                            statementData.advanceDetails.map((adv, idx) => (
                              <tr key={idx}>
                                <td className="font-mono">{adv.advanceCode}</td>
                                <td>{adv.date}</td>
                                <td className="text-right font-mono" style={{ fontWeight: 600 }}>₹{adv.amount?.toFixed(2)}</td>
                                <td>{adv.recoveryStatus}</td>
                              </tr>
                            ))
                          ) : (
                            <tr><td colSpan="4" className="text-center">No advances logged.</td></tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  <div>
                    <h4 style={{ fontSize: '13px', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '8px' }}>
                      Disbursements & Wage Payments Received
                    </h4>
                    <div className="gov-table-container">
                      <table className="gov-table">
                        <thead>
                          <tr>
                            <th>Receipt ID</th>
                            <th>Date</th>
                            <th className="text-right">Amount (₹)</th>
                            <th>Channel / Ref</th>
                          </tr>
                        </thead>
                        <tbody>
                          {statementData.paymentDetails && statementData.paymentDetails.length > 0 ? (
                            statementData.paymentDetails.map((pay, idx) => (
                              <tr key={idx}>
                                <td className="font-mono">{pay.paymentCode}</td>
                                <td>{pay.date}</td>
                                <td className="text-right font-mono" style={{ fontWeight: 600, color: 'var(--gov-success)' }}>
                                  ₹{pay.amount?.toFixed(2)}
                                </td>
                                <td>{pay.paymentMethod} {pay.referenceNumber ? `(${pay.referenceNumber})` : ''}</td>
                              </tr>
                            ))
                          ) : (
                            <tr><td colSpan="4" className="text-center">No payments disbursed in period.</td></tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>

                {/* Formal Signatures Block */}
                <div style={{ marginTop: '36px', display: 'flex', justifyContent: 'space-between', padding: '0 20px', fontSize: '12px', color: 'var(--gov-text)' }}>
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ borderBottom: '1px solid #000', width: '180px', marginBottom: '6px' }}></div>
                    Worker Signature / Thumb Impression
                  </div>
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ borderBottom: '1px solid #000', width: '180px', marginBottom: '6px' }}></div>
                    Site Supervisor / Engineer Signature
                  </div>
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ borderBottom: '1px solid #000', width: '180px', marginBottom: '6px' }}></div>
                    Authorized Signatory (Electrical Contractor)
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Site-Wise Monthly Summary */}
      {activeTab === 'site' && (
        <div>
          <div className="gov-filter-bar">
            <div style={{ width: '280px' }}>
              <label className="gov-form-label required">Select Site</label>
              <select
                className="gov-form-select"
                value={selectedSiteId}
                onChange={(e) => setSelectedSiteId(e.target.value)}
              >
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
                ))}
              </select>
            </div>

            <div style={{ width: '160px' }}>
              <label className="gov-form-label required">Month</label>
              <select
                className="gov-form-select"
                value={siteMonth}
                onChange={(e) => setSiteMonth(parseInt(e.target.value, 10))}
              >
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((m) => (
                  <option key={m} value={m}>{new Date(2026, m - 1).toLocaleString('en-IN', { month: 'long' })}</option>
                ))}
              </select>
            </div>

            <div style={{ width: '140px' }}>
              <label className="gov-form-label required">Year</label>
              <input
                type="number"
                className="gov-form-input font-mono"
                value={siteYear}
                onChange={(e) => setSiteYear(parseInt(e.target.value, 10))}
              />
            </div>

            <div style={{ alignSelf: 'flex-end' }}>
              <button onClick={fetchSiteReport} className="gov-btn gov-btn-secondary">
                <Search size={13} /> View Site Ledger
              </button>
            </div>
          </div>

          {siteReportData && (
            <div className="gov-card">
              <div className="gov-card-header">
                <span>
                  Site Monthly Ledger: <strong>{siteReportData.siteName} ({siteReportData.siteCode})</strong> — {siteReportData.month}/{siteReportData.year}
                </span>
                <span style={{ fontSize: '12px' }}>Client: {siteReportData.clientName || 'N/A'}</span>
              </div>
              <div className="gov-card-body" style={{ padding: 0 }}>
                {/* Summary Strip */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '1px', backgroundColor: 'var(--gov-border)', borderBottom: '1px solid var(--gov-border)' }}>
                  <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                    <div style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Assigned Labour</div>
                    <div style={{ fontSize: '18px', fontWeight: 700 }}>{siteReportData.totalAssignedLabour}</div>
                  </div>
                  <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                    <div style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Total Present Shifts</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-success)' }}>{siteReportData.totalPresentDays}</div>
                  </div>
                  <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                    <div style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Gross Wage Accrual</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-navy)' }}>₹{siteReportData.totalGrossWages?.toFixed(2)}</div>
                  </div>
                  <div style={{ backgroundColor: 'var(--gov-white)', padding: '10px 14px' }}>
                    <div style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Payments Disbursed</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-success)' }}>₹{siteReportData.totalPaymentsMade?.toFixed(2)}</div>
                  </div>
                  <div style={{ backgroundColor: 'var(--gov-light-blue)', padding: '10px 14px' }}>
                    <div style={{ fontSize: '11px', color: 'var(--gov-navy)' }}>Outstanding Balance</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: 'var(--gov-navy)' }}>₹{siteReportData.totalOutstandingBalance?.toFixed(2)}</div>
                  </div>
                </div>

                <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
                  <table className="gov-table">
                    <thead>
                      <tr>
                        <th>Worker ID</th>
                        <th>Full Name</th>
                        <th>Designation</th>
                        <th className="text-right">Daily Wage</th>
                        <th className="text-center">Present Days</th>
                        <th className="text-center">Half Days</th>
                        <th className="text-center">Absent</th>
                        <th className="text-right">OT Hours</th>
                        <th className="text-right">Gross Earned (₹)</th>
                        <th className="text-right">Advances (₹)</th>
                        <th className="text-right">Payments (₹)</th>
                        <th className="text-right">Balance Due (₹)</th>
                      </tr>
                    </thead>
                    <tbody>
                      {siteReportData.workerRows && siteReportData.workerRows.length > 0 ? (
                        siteReportData.workerRows.map((r) => (
                          <tr key={r.labourerId}>
                            <td className="font-mono" style={{ fontWeight: 600 }}>{r.labourCode}</td>
                            <td><strong>{r.fullName}</strong></td>
                            <td>{r.designation}</td>
                            <td className="text-right font-mono">₹{r.dailyWage?.toFixed(2)}</td>
                            <td className="text-center font-mono" style={{ color: 'var(--gov-success)', fontWeight: 600 }}>{r.presentDays}</td>
                            <td className="text-center font-mono">{r.halfDays}</td>
                            <td className="text-center font-mono" style={{ color: 'var(--gov-danger)' }}>{r.absentDays}</td>
                            <td className="text-right font-mono">{r.overtimeHours}</td>
                            <td className="text-right font-mono" style={{ fontWeight: 600 }}>₹{r.grossEarnings?.toFixed(2)}</td>
                            <td className="text-right font-mono" style={{ color: '#D97706' }}>₹{r.advancesDisbursed?.toFixed(2)}</td>
                            <td className="text-right font-mono" style={{ color: 'var(--gov-success)' }}>₹{r.paymentsMade?.toFixed(2)}</td>
                            <td className="text-right font-mono" style={{ fontWeight: 700, color: r.balanceDue > 0 ? 'var(--gov-danger)' : undefined }}>
                              ₹{r.balanceDue?.toFixed(2)}
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan="12" className="text-center" style={{ padding: '20px' }}>
                            No worker muster records for this site in the selected month.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
