import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { Modal } from '../../components/common/Modal';
import { PaymentDetailsModal } from '../../components/common/PaymentDetailsModal';
import api from '../../services/api';
import {
  CreditCard,
  Plus,
  Search,
  Calendar,
  FileCheck,
  IndianRupee,
  QrCode,
  Smartphone,
  Building,
  Copy,
  Check
} from 'lucide-react';

export const Payments = () => {
  const { activeBusinessId } = useBusiness();
  const [payments, setPayments] = useState([]);
  const [labourers, setLabourers] = useState([]);
  const [sites, setSites] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);

  const [pageData, setPageData] = useState({ pageNumber: 0, pageSize: 15, totalElements: 0, totalPages: 1 });
  const [methodFilter, setMethodFilter] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [paymentModalOpen, setPaymentModalOpen] = useState(false);
  const [viewWorkerPayment, setViewWorkerPayment] = useState(null);
  const [copiedField, setCopiedField] = useState('');

  const copyToClipboard = (text, fieldName) => {
    if (!text) return;
    navigator.clipboard.writeText(text);
    setCopiedField(fieldName);
    setTimeout(() => setCopiedField(''), 2500);
  };

  const [formData, setFormData] = useState({
    labourerId: '',
    siteId: '',
    paymentDate: new Date().toISOString().split('T')[0],
    amount: '',
    paymentMethod: 'CASH',
    referenceNumber: '',
    receiptAttachmentUrl: '',
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

      api.get(`/master-data/values?category=PAYMENT_METHOD&businessId=${activeBusinessId}&activeOnly=true`)
        .then((res) => res.data?.data && setPaymentMethods(res.data.data))
        .catch(console.error);

      fetchPayments(0);
    }
  }, [activeBusinessId, methodFilter]);

  const fetchPayments = async (page = 0) => {
    if (!activeBusinessId) return;
    setLoading(true);
    try {
      let url = `/payments?businessId=${activeBusinessId}&page=${page}&size=15`;
      if (methodFilter) url += `&paymentMethod=${methodFilter}`;
      const res = await api.get(url);
      if (res.data?.data) {
        setPayments(res.data.data.content || []);
        setPageData({
          pageNumber: res.data.data.pageNumber,
          pageSize: res.data.data.pageSize,
          totalElements: res.data.data.totalElements,
          totalPages: res.data.data.totalPages
        });
      }
    } catch (err) {
      console.error('Failed to load payments', err);
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setFormData({
      labourerId: labourers[0]?.id || '',
      siteId: sites[0]?.id || '',
      paymentDate: new Date().toISOString().split('T')[0],
      amount: '',
      paymentMethod: paymentMethods[0]?.code || 'CASH',
      referenceNumber: '',
      receiptAttachmentUrl: '',
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

      await api.post('/payments', payload);
      setIsModalOpen(false);
      fetchPayments(0);
    } catch (err) {
      setError(err.response?.data?.message || 'Error recording payment disbursement');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Payment Disbursements' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Wage Disbursements & Payment Ledger</h2>
          <p>Disburse partial or full wages via Cash, Bank Transfer, or UPI with reference voucher tracking</p>
        </div>
        <div>
          <button onClick={openAddModal} className="gov-btn gov-btn-primary">
            <Plus size={14} /> Record Wage Disbursement
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="gov-filter-bar">
        <div style={{ width: '220px' }}>
          <label className="gov-form-label">Disbursement Channel Filter</label>
          <select
            className="gov-form-select"
            value={methodFilter}
            onChange={(e) => setMethodFilter(e.target.value)}
          >
            <option value="">All Payment Channels</option>
            {paymentMethods.map((m) => (
              <option key={m.id} value={m.code}>{m.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Payments Register Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Payment Disbursement Journal</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Disbursements: {pageData.totalElements}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Voucher ID</th>
                  <th>Payment Date</th>
                  <th>Labour Name & Code</th>
                  <th>Site Scope</th>
                  <th>Payment Channel</th>
                  <th>Payee QR / Bank</th>
                  <th className="text-right">Amount Disbursed (₹)</th>
                  <th>Reference / Receipt</th>
                  <th>Recorded By</th>
                  <th>Remarks</th>
                </tr>
              </thead>
              <tbody>
                {payments.length > 0 ? (
                  payments.map((p) => (
                    <tr key={p.id}>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{p.paymentCode}</td>
                      <td>{p.paymentDate}</td>
                      <td>
                        <strong>{p.labourName}</strong>
                        <div className="font-mono" style={{ fontSize: '11px', color: '#64748B' }}>{p.labourCode}</div>
                      </td>
                      <td>{p.siteName || 'Central Business Account'}</td>
                      <td>
                        <span style={{ fontWeight: 600, color: 'var(--gov-blue)' }}>
                          {p.paymentMethod?.replace(/_/g, ' ')}
                        </span>
                      </td>
                      <td>
                        <button
                          type="button"
                          onClick={() => {
                            const found = labourers.find((l) => l.id === p.labourId || l.labourCode === p.labourCode);
                            setViewWorkerPayment(found || { fullName: p.labourName, labourCode: p.labourCode });
                            setPaymentModalOpen(true);
                          }}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                          style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', padding: '3px 8px', fontSize: '11px' }}
                          title="View worker payment QR & bank account"
                        >
                          <QrCode size={12} color="#2563EB" /> Pay / QR
                        </button>
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700, color: 'var(--gov-success)' }}>
                        ₹{p.amount.toFixed(2)}
                      </td>
                      <td className="font-mono" style={{ fontSize: '12px' }}>
                        {p.referenceNumber || 'N/A'}
                      </td>
                      <td style={{ fontSize: '12px' }}>{p.recordedBy || 'SYSTEM'}</td>
                      <td>{p.remarks || '--'}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="10" className="text-center" style={{ padding: '24px' }}>
                      {loading ? 'Retrieving payment entries...' : 'No payment disbursements recorded yet.'}
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
              onClick={() => fetchPayments(pageData.pageNumber - 1)}
              className="gov-page-btn"
            >
              Previous
            </button>
            <button
              disabled={pageData.pageNumber + 1 >= pageData.totalPages}
              onClick={() => fetchPayments(pageData.pageNumber + 1)}
              className="gov-page-btn"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Record Payment Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Record Wage Disbursement Entry"
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
              <label className="gov-form-label">Charge to Site</label>
              <select
                className="gov-form-select"
                value={formData.siteId}
                onChange={(e) => setFormData({ ...formData, siteId: e.target.value })}
              >
                <option value="">-- Worker Assigned Site --</option>
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
                ))}
              </select>
            </div>
          </div>

          {/* Payee Settlement Channel Preview */}
          {(() => {
            const selectedWorker = labourers.find((l) => l.id === parseInt(formData.labourerId, 10));
            if (!selectedWorker) return null;
            const hasPaymentInfo = selectedWorker.upiId || selectedWorker.bankAccountNumber || selectedWorker.qrCodeUrl;
            return (
              <div style={{
                margin: '12px 0 16px',
                padding: '12px 14px',
                backgroundColor: '#F8FAFC',
                border: '1px solid #CBD5E1',
                borderRadius: '8px',
                fontSize: '13px'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                  <strong style={{ color: '#0F172A', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Smartphone size={15} color="#2563EB" /> Payee Settlement Channels ({selectedWorker.fullName})
                  </strong>
                  <button
                    type="button"
                    onClick={() => {
                      setViewWorkerPayment(selectedWorker);
                      setPaymentModalOpen(true);
                    }}
                    className="gov-btn gov-btn-secondary gov-btn-sm"
                    style={{ padding: '2px 8px', fontSize: '11px', display: 'flex', alignItems: 'center', gap: '4px' }}
                  >
                    <QrCode size={12} /> Scan QR & View Bank
                  </button>
                </div>
                {hasPaymentInfo ? (
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px' }}>
                    {selectedWorker.upiId && (
                      <div style={{ background: '#EFF6FF', padding: '8px 10px', borderRadius: '6px', border: '1px solid #BFDBFE' }}>
                        <div style={{ fontSize: '11px', color: '#1E40AF', fontWeight: 600 }}>UPI ID / Mobile</div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '2px' }}>
                          <span className="font-mono" style={{ fontWeight: 600, color: '#1E3A8A' }}>{selectedWorker.upiId}</span>
                          <button
                            type="button"
                            onClick={() => copyToClipboard(selectedWorker.upiId, 'upi')}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#2563EB', padding: '2px' }}
                            title="Copy UPI"
                          >
                            {copiedField === 'upi' ? <Check size={14} color="#16A34A" /> : <Copy size={14} />}
                          </button>
                        </div>
                      </div>
                    )}
                    {selectedWorker.bankAccountNumber && (
                      <div style={{ background: '#F0FDF4', padding: '8px 10px', borderRadius: '6px', border: '1px solid #BBF7D0' }}>
                        <div style={{ fontSize: '11px', color: '#166534', fontWeight: 600 }}>
                          {selectedWorker.bankName || 'Bank A/c'} ({selectedWorker.bankIfscCode || 'IFSC'})
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '2px' }}>
                          <span className="font-mono" style={{ fontWeight: 600, color: '#14532D' }}>{selectedWorker.bankAccountNumber}</span>
                          <button
                            type="button"
                            onClick={() => copyToClipboard(selectedWorker.bankAccountNumber, 'acc')}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#16A34A', padding: '2px' }}
                            title="Copy A/C No"
                          >
                            {copiedField === 'acc' ? <Check size={14} color="#16A34A" /> : <Copy size={14} />}
                          </button>
                        </div>
                      </div>
                    )}
                    {selectedWorker.qrCodeUrl && (
                      <div style={{ background: '#FAF5FF', padding: '8px 10px', borderRadius: '6px', border: '1px solid #E9D5FF', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <QrCode size={18} color="#7C3AED" />
                        <div>
                          <div style={{ fontSize: '11px', color: '#6B21A8', fontWeight: 600 }}>Payee QR Code</div>
                          <span style={{ fontSize: '11px', color: '#7E22CE' }}>Ready to Scan & Pay</span>
                        </div>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ color: '#64748B', fontSize: '12px', fontStyle: 'italic' }}>
                    No UPI or Bank details saved for this worker yet. You can disburse cash or update their payment details in the Labour directory.
                  </div>
                )}
              </div>
            );
          })()}

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Disbursement Amount (₹)</label>
              <input
                type="number"
                step="0.01"
                required
                className="gov-form-input font-mono"
                value={formData.amount}
                onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                placeholder="e.g. 5000.00"
              />
              <div className="gov-form-help">Partial or complete wage payments accepted</div>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Disbursement Date</label>
              <input
                type="date"
                required
                className="gov-form-input"
                value={formData.paymentDate}
                onChange={(e) => setFormData({ ...formData, paymentDate: e.target.value })}
              />
            </div>
          </div>

          <div className="gov-form-row">
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
            <div className="gov-form-col">
              <label className="gov-form-label">Payment UTR / Bank Reference No.</label>
              <input
                type="text"
                className="gov-form-input font-mono"
                value={formData.referenceNumber}
                onChange={(e) => setFormData({ ...formData, referenceNumber: e.target.value })}
                placeholder="NEFT/UPI/Cheque reference"
              />
            </div>
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Disbursement Remarks</label>
            <textarea
              className="gov-form-textarea"
              rows={2}
              value={formData.remarks}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              placeholder="e.g. November first-half partial wage settlement"
            />
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Committing Entry...' : 'Confirm & Disburse Payment'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Payment Details Modal */}
      <PaymentDetailsModal
        isOpen={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        target={viewWorkerPayment}
        type="labour"
        canEdit={false}
      />
    </div>
  );
};
