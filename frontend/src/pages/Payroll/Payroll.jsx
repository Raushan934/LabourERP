import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Modal } from '../../components/common/Modal';
import api from '../../services/api';
import {
  Calculator,
  Plus,
  FileText,
  Calendar,
  CheckCircle,
  IndianRupee,
  Layers,
  ArrowRight
} from 'lucide-react';

export const Payroll = () => {
  const { activeBusinessId } = useBusiness();
  const [runs, setRuns] = useState([]);
  const [sites, setSites] = useState([]);
  const [selectedRun, setSelectedRun] = useState(null);
  const [isGenerateModalOpen, setIsGenerateModalOpen] = useState(false);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);

  const [generateForm, setGenerateForm] = useState({
    siteId: '',
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear(),
  });

  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (activeBusinessId) {
      api.get(`/sites/all?businessId=${activeBusinessId}`)
        .then((res) => res.data?.data && setSites(res.data.data))
        .catch(console.error);
      fetchRuns();
    }
  }, [activeBusinessId]);

  const fetchRuns = async () => {
    if (!activeBusinessId) return;
    setLoading(true);
    try {
      const res = await api.get(`/payroll?businessId=${activeBusinessId}&size=20`);
      if (res.data?.data) {
        setRuns(res.data.data.content || []);
      }
    } catch (err) {
      console.error('Failed to load payroll runs', err);
    } finally {
      setLoading(false);
    }
  };

  const openDetails = async (runId) => {
    try {
      const res = await api.get(`/payroll/${runId}`);
      if (res.data?.data) {
        setSelectedRun(res.data.data);
        setIsDetailsModalOpen(true);
      }
    } catch (err) {
      console.error('Failed to load payroll details', err);
    }
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    setGenerating(true);
    setError('');
    try {
      const payload = {
        businessId: activeBusinessId,
        siteId: generateForm.siteId ? parseInt(generateForm.siteId, 10) : null,
        month: parseInt(generateForm.month, 10),
        year: parseInt(generateForm.year, 10),
      };

      const res = await api.post('/payroll/generate', payload);
      setIsGenerateModalOpen(false);
      fetchRuns();
      if (res.data?.data?.id) {
        openDetails(res.data.data.id);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error generating payroll');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Wage & Payroll Ledger' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Periodic Wage & Payroll Settlement Ledger</h2>
          <p>Execute automated statutory wage calculations from daily muster rosters and advance recovery rules</p>
        </div>
        <div>
          <button onClick={() => setIsGenerateModalOpen(true)} className="gov-btn gov-btn-primary">
            <Calculator size={14} /> Calculate & Generate Payroll
          </button>
        </div>
      </div>

      {/* Payroll Runs Register */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Executed Periodic Payroll Runs</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Cycles: {runs.length}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Run Code</th>
                  <th>Period (Month/Year)</th>
                  <th>Site Scope</th>
                  <th className="text-center">Workers</th>
                  <th className="text-right">Gross Wages (₹)</th>
                  <th className="text-right">Advance Recovery (₹)</th>
                  <th className="text-right">Net Payable (₹)</th>
                  <th className="text-right">Disbursed (₹)</th>
                  <th className="text-right">Outstanding (₹)</th>
                  <th className="text-center">Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {runs.length > 0 ? (
                  runs.map((r) => (
                    <tr key={r.id}>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{r.runCode}</td>
                      <td>
                        <strong>{r.periodMonth}/{r.periodYear}</strong>
                        <div style={{ fontSize: '11px', color: '#64748B' }}>
                          {r.startDate} to {r.endDate}
                        </div>
                      </td>
                      <td>{r.siteName || 'All Business Sites'}</td>
                      <td className="text-center font-mono" style={{ fontWeight: 600 }}>{r.totalWorkers}</td>
                      <td className="text-right font-mono">₹{r.totalGrossEarnings.toFixed(2)}</td>
                      <td className="text-right font-mono" style={{ color: 'var(--gov-danger)' }}>
                        ₹{r.totalAdvanceDeductions.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700, color: 'var(--gov-navy)' }}>
                        ₹{r.totalNetPayable.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ color: 'var(--gov-success)' }}>
                        ₹{r.totalPaidAmount.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700, color: r.totalOutstandingBalance > 0 ? 'var(--gov-danger)' : undefined }}>
                        ₹{r.totalOutstandingBalance.toFixed(2)}
                      </td>
                      <td className="text-center">
                        <StatusBadge status={r.status} />
                      </td>
                      <td>
                        <button
                          onClick={() => openDetails(r.id)}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                        >
                          <FileText size={12} /> View Ledger
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="11" className="text-center" style={{ padding: '24px' }}>
                      {loading ? 'Retrieving payroll records...' : 'No payroll runs have been generated yet.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Generate Payroll Modal */}
      <Modal
        isOpen={isGenerateModalOpen}
        onClose={() => setIsGenerateModalOpen(false)}
        title="Execute Periodic Wage & Payroll Calculation"
        size="default"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}

        <div style={{ backgroundColor: 'var(--gov-light-blue)', border: '1px solid var(--gov-border)', padding: '10px 12px', fontSize: '12px', marginBottom: '14px' }}>
          <strong>Configurable Engine:</strong> The system automatically evaluates daily full days, half days (at business-configured percentage), paid leaves, and overtime hours. It automatically bounds advance recovery against the enterprise recovery cap.
        </div>

        <form onSubmit={handleGenerate}>
          <div className="gov-form-group">
            <label className="gov-form-label required">Payroll Month</label>
            <select
              className="gov-form-select"
              required
              value={generateForm.month}
              onChange={(e) => setGenerateForm({ ...generateForm, month: e.target.value })}
            >
              {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map((m) => (
                <option key={m} value={m}>
                  {new Date(2026, m - 1).toLocaleString('en-IN', { month: 'long' })} ({m})
                </option>
              ))}
            </select>
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label required">Payroll Year</label>
            <input
              type="number"
              required
              className="gov-form-input font-mono"
              value={generateForm.year}
              onChange={(e) => setGenerateForm({ ...generateForm, year: e.target.value })}
              min="2020"
              max="2035"
            />
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Site Scope (Optional)</label>
            <select
              className="gov-form-select"
              value={generateForm.siteId}
              onChange={(e) => setGenerateForm({ ...generateForm, siteId: e.target.value })}
            >
              <option value="">-- All Operating Sites in Business --</option>
              {sites.map((s) => (
                <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
              ))}
            </select>
            <div className="gov-form-help">Leave empty to aggregate and calculate for all business sites</div>
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsGenerateModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={generating} className="gov-btn gov-btn-primary">
              {generating ? 'Calculating Ledger...' : 'Run Payroll Calculation'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Itemized Payroll Ledger Modal */}
      <Modal
        isOpen={isDetailsModalOpen}
        onClose={() => setIsDetailsModalOpen(false)}
        title={`Itemized Wage Ledger: ${selectedRun?.runCode}`}
        size="xl"
      >
        {selectedRun && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', marginBottom: '14px', backgroundColor: 'var(--gov-light-grey)', padding: '10px', border: '1px solid var(--gov-border)' }}>
              <div>
                <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Period:</span>
                <div style={{ fontWeight: 700 }}>{selectedRun.periodMonth}/{selectedRun.periodYear} ({selectedRun.startDate} to {selectedRun.endDate})</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Total Workers:</span>
                <div style={{ fontWeight: 700 }}>{selectedRun.totalWorkers} labourers</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Total Net Payable:</span>
                <div style={{ fontWeight: 700, color: 'var(--gov-navy)' }}>₹{selectedRun.totalNetPayable?.toFixed(2)}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', color: 'var(--gov-secondary-text)' }}>Total Outstanding Balance:</span>
                <div style={{ fontWeight: 700, color: 'var(--gov-danger)' }}>₹{selectedRun.totalOutstandingBalance?.toFixed(2)}</div>
              </div>
            </div>

            <div className="gov-table-container">
              <table className="gov-table">
                <thead>
                  <tr>
                    <th>Worker ID</th>
                    <th>Full Name</th>
                    <th>Designation</th>
                    <th className="text-right">Daily Rate</th>
                    <th className="text-center">Full/Half/Abs</th>
                    <th className="text-center">Paid Leaves</th>
                    <th className="text-right">OT (Hrs / ₹)</th>
                    <th className="text-right">Gross (₹)</th>
                    <th className="text-right">Adv. Recovery (₹)</th>
                    <th className="text-right">Net Payable (₹)</th>
                    <th className="text-right">Disbursed (₹)</th>
                    <th className="text-right">Balance Due (₹)</th>
                    <th className="text-center">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedRun.items && selectedRun.items.map((item) => (
                    <tr key={item.id}>
                      <td className="font-mono" style={{ fontWeight: 600 }}>{item.labourCode}</td>
                      <td><strong>{item.labourName}</strong></td>
                      <td>{item.designation}</td>
                      <td className="text-right font-mono">₹{item.dailyWageRate.toFixed(2)}</td>
                      <td className="text-center font-mono">
                        {item.presentDays} / {item.halfDays} / {item.absentDays}
                      </td>
                      <td className="text-center font-mono">{item.paidLeaveDays}</td>
                      <td className="text-right font-mono">
                        {item.overtimeHours} hrs (₹{item.overtimeAmount.toFixed(2)})
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 600 }}>
                        ₹{item.grossEarnings.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ color: 'var(--gov-danger)' }}>
                        -₹{item.advanceRecovery.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700, color: 'var(--gov-navy)' }}>
                        ₹{item.netPayable.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ color: 'var(--gov-success)' }}>
                        ₹{item.amountPaid.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700, color: item.remainingBalance > 0 ? 'var(--gov-danger)' : undefined }}>
                        ₹{item.remainingBalance.toFixed(2)}
                      </td>
                      <td className="text-center">
                        <StatusBadge status={item.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="gov-modal-footer">
              <button type="button" onClick={() => setIsDetailsModalOpen(false)} className="gov-btn gov-btn-primary">
                Close Ledger
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
