import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Modal } from '../../components/common/Modal';
import api from '../../services/api';
import { HandCoins, Plus, Search, Calendar, CheckCircle2 } from 'lucide-react';

export const Advances = () => {
  const { activeBusinessId } = useBusiness();
  const [advances, setAdvances] = useState([]);
  const [labourers, setLabourers] = useState([]);
  const [sites, setSites] = useState([]);
  const [advanceTypes, setAdvanceTypes] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);

  const [pageData, setPageData] = useState({ pageNumber: 0, pageSize: 15, totalElements: 0, totalPages: 1 });
  const [statusFilter, setStatusFilter] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [formData, setFormData] = useState({
    labourerId: '',
    siteId: '',
    advanceDate: new Date().toISOString().split('T')[0],
    amount: '',
    paymentMethod: 'CASH',
    advanceType: 'SALARY_ADVANCE',
    referenceNumber: '',
    purpose: '',
    remarks: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (activeBusinessId) {
      api.get(`/labour?businessId=${activeBusinessId}&size=100`)
        .then((res) => res.data?.data?.content && setLabourers(res.data.data.content))
        .catch(console.error);

      api.get(`/sites/all?businessId=${activeBusinessId}`)
        .then((res) => res.data?.data && setSites(res.data.data))
        .catch(console.error);

      api.get(`/master-data/values?category=ADVANCE_TYPE&businessId=${activeBusinessId}&activeOnly=true`)
        .then((res) => res.data?.data && setAdvanceTypes(res.data.data))
        .catch(console.error);

      api.get(`/master-data/values?category=PAYMENT_METHOD&businessId=${activeBusinessId}&activeOnly=true`)
        .then((res) => res.data?.data && setPaymentMethods(res.data.data))
        .catch(console.error);

      fetchAdvances(0);
    }
  }, [activeBusinessId, statusFilter]);

  const fetchAdvances = async (page = 0) => {
    if (!activeBusinessId) return;
    setLoading(true);
    try {
      let url = `/advances?businessId=${activeBusinessId}&page=${page}&size=15`;
      if (statusFilter) url += `&status=${statusFilter}`;
      const res = await api.get(url);
      if (res.data?.data) {
        setAdvances(res.data.data.content || []);
        setPageData({
          pageNumber: res.data.data.pageNumber,
          pageSize: res.data.data.pageSize,
          totalElements: res.data.data.totalElements,
          totalPages: res.data.data.totalPages
        });
      }
    } catch (err) {
      console.error('Failed to load advances', err);
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setFormData({
      labourerId: labourers[0]?.id || '',
      siteId: sites[0]?.id || '',
      advanceDate: new Date().toISOString().split('T')[0],
      amount: '',
      paymentMethod: paymentMethods[0]?.code || 'CASH',
      advanceType: advanceTypes[0]?.code || 'SALARY_ADVANCE',
      referenceNumber: '',
      purpose: '',
      remarks: ''
    });
    setError('');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const payload = {
        ...formData,
        businessId: activeBusinessId,
        labourerId: parseInt(formData.labourerId, 10),
        siteId: formData.siteId ? parseInt(formData.siteId, 10) : null,
        amount: parseFloat(formData.amount)
      };

      await api.post('/advances', payload);
      setIsModalOpen(false);
      fetchAdvances(0);
    } catch (err) {
      setError(err.response?.data?.message || 'Error recording advance transaction');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Advance Management' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Workforce Advance & Loan Management</h2>
          <p>Disburse, track, and maintain transaction ledger for worker loans and wage advances</p>
        </div>
        <div>
          <button onClick={openAddModal} className="gov-btn gov-btn-primary">
            <Plus size={14} /> Record Advance Disbursement
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="gov-filter-bar">
        <div style={{ width: '200px' }}>
          <label className="gov-form-label">Recovery Status Filter</label>
          <select
            className="gov-form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Transactions</option>
            <option value="OPEN">OPEN (Unrecovered)</option>
            <option value="PARTIALLY_RECOVERED">PARTIALLY RECOVERED</option>
            <option value="SETTLED">SETTLED</option>
          </select>
        </div>
      </div>

      {/* Advance Register Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Advance Disbursement Ledger</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Entries: {pageData.totalElements}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Voucher Code</th>
                  <th>Date</th>
                  <th>Worker Name & ID</th>
                  <th>Site Scope</th>
                  <th>Advance Type</th>
                  <th>Disbursed Mode</th>
                  <th className="text-right">Original Amount (₹)</th>
                  <th className="text-right">Recovered (₹)</th>
                  <th className="text-right">Balance Due (₹)</th>
                  <th className="text-center">Recovery Status</th>
                  <th>Purpose & Ref</th>
                </tr>
              </thead>
              <tbody>
                {advances.length > 0 ? (
                  advances.map((a) => (
                    <tr key={a.id}>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{a.advanceCode}</td>
                      <td>{a.advanceDate}</td>
                      <td>
                        <strong>{a.labourName}</strong>
                        <div className="font-mono" style={{ fontSize: '11px', color: '#64748B' }}>{a.labourCode}</div>
                      </td>
                      <td>{a.siteName || 'General / Central'}</td>
                      <td>{a.advanceType?.replace(/_/g, ' ')}</td>
                      <td>
                        <span style={{ fontWeight: 600 }}>{a.paymentMethod?.replace(/_/g, ' ')}</span>
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700 }}>
                        ₹{a.amount.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ color: 'var(--gov-success)' }}>
                        ₹{a.recoveredAmount.toFixed(2)}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700, color: a.remainingBalance > 0 ? 'var(--gov-danger)' : undefined }}>
                        ₹{a.remainingBalance.toFixed(2)}
                      </td>
                      <td className="text-center">
                        <StatusBadge status={a.recoveryStatus} />
                      </td>
                      <td style={{ fontSize: '12px' }}>
                        <div>{a.purpose || 'N/A'}</div>
                        {a.referenceNumber && (
                          <div className="font-mono" style={{ color: '#64748B' }}>Ref: {a.referenceNumber}</div>
                        )}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="11" className="text-center" style={{ padding: '24px' }}>
                      {loading ? 'Retrieving advance transactions...' : 'No advance records found for this business.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Pagination */}
      {pageData.totalPages > 1 && (
        <div className="gov-pagination">
          <span>Showing page {pageData.pageNumber + 1} of {pageData.totalPages}</span>
          <div className="gov-page-btns">
            <button
              disabled={pageData.pageNumber === 0}
              onClick={() => fetchAdvances(pageData.pageNumber - 1)}
              className="gov-page-btn"
            >
              Previous
            </button>
            <button
              disabled={pageData.pageNumber + 1 >= pageData.totalPages}
              onClick={() => fetchAdvances(pageData.pageNumber + 1)}
              className="gov-page-btn"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Record Advance Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Record New Worker Advance Disbursement"
        size="lg"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Select Worker</label>
              <select
                className="gov-form-select"
                required
                value={formData.labourerId}
                onChange={(e) => setFormData({ ...formData, labourerId: e.target.value })}
              >
                <option value="">-- Choose Labourer --</option>
                {labourers.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.fullName} ({l.labourCode}) - {l.designation}
                  </option>
                ))}
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Charge to Site (Optional)</label>
              <select
                className="gov-form-select"
                value={formData.siteId}
                onChange={(e) => setFormData({ ...formData, siteId: e.target.value })}
              >
                <option value="">-- Worker Current Assigned Site --</option>
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
                ))}
              </select>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Advance Amount (₹)</label>
              <input
                type="number"
                step="0.01"
                required
                className="gov-form-input font-mono"
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                placeholder="e.g. 2000.00"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Disbursement Date</label>
              <input
                type="date"
                required
                className="gov-form-input"
                value={formData.advanceDate}
                onChange={(e) => setFormData({ ...formData, advanceDate: e.target.value })}
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Advance Type (Dynamic Master)</label>
              <select
                className="gov-form-select"
                required
                value={formData.advanceType}
                onChange={(e) => setFormData({ ...formData, advanceType: e.target.value })}
              >
                {advanceTypes.map((t) => (
                  <option key={t.id} value={t.code}>{t.name}</option>
                ))}
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Payment Method (Dynamic Master)</label>
              <select
                className="gov-form-select"
                required
                value={formData.paymentMethod}
                onChange={(e) => setFormData({ ...formData, paymentMethod: e.target.value })}
              >
                {paymentMethods.map((m) => (
                  <option key={m.id} value={m.code}>{m.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Payment Ref / Voucher / UTR Number</label>
              <input
                type="text"
                className="gov-form-input font-mono"
                value={formData.referenceNumber}
                onChange={(e) => setFormData({ ...formData, referenceNumber: e.target.value })}
                placeholder="e.g. UTR-98218721 or Voucher #14"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Purpose of Advance</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.purpose}
                onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
                placeholder="e.g. Medical emergency, family travel, festival"
              />
            </div>
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Remarks</label>
            <textarea
              className="gov-form-textarea"
              rows={2}
              value={formData.remarks}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              placeholder="Approving manager notes"
            />
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Disbursing...' : 'Confirm & Save Advance'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
