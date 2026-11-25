import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { Modal } from '../../components/common/Modal';
import api from '../../services/api';
import { ArrowLeftRight, Plus, MapPin, Calendar, CheckCircle } from 'lucide-react';

export const Transfers = () => {
  const { activeBusinessId } = useBusiness();
  const [transfers, setTransfers] = useState([]);
  const [labourers, setLabourers] = useState([]);
  const [sites, setSites] = useState([]);
  const [pageData, setPageData] = useState({ pageNumber: 0, pageSize: 15, totalElements: 0, totalPages: 1 });
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [formData, setFormData] = useState({
    labourerId: '',
    toSiteId: '',
    transferDate: new Date().toISOString().split('T')[0],
    effectiveDate: new Date().toISOString().split('T')[0],
    reason: '',
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

      fetchTransfers(0);
    }
  }, [activeBusinessId]);

  const fetchTransfers = async (page = 0) => {
    if (!activeBusinessId) return;
    setLoading(true);
    try {
      const res = await api.get(`/transfers?businessId=${activeBusinessId}&page=${page}&size=15`);
      if (res.data?.data) {
        setTransfers(res.data.data.content || []);
        setPageData({
          pageNumber: res.data.data.pageNumber,
          pageSize: res.data.data.pageSize,
          totalElements: res.data.data.totalElements,
          totalPages: res.data.data.totalPages
        });
      }
    } catch (err) {
      console.error('Failed to load transfers', err);
    } finally {
      setLoading(false);
    }
  };

  const openAddModal = () => {
    setFormData({
      labourerId: labourers[0]?.id || '',
      toSiteId: sites[0]?.id || '',
      transferDate: new Date().toISOString().split('T')[0],
      effectiveDate: new Date().toISOString().split('T')[0],
      reason: '',
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
        toSiteId: parseInt(formData.toSiteId, 10)
      };

      await api.post('/transfers', payload);
      setIsModalOpen(false);
      fetchTransfers(0);
    } catch (err) {
      setError(err.response?.data?.message || 'Error executing labour transfer');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Labour Transfers' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Inter-Site Workforce Reassignment & Transfers</h2>
          <p>Reallocate labourers between sites while maintaining strict historical attendance and tenure associations</p>
        </div>
        <div>
          <button onClick={openAddModal} className="gov-btn gov-btn-primary">
            <Plus size={14} /> Execute Labour Transfer
          </button>
        </div>
      </div>

      {/* Transfers Register Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Workforce Movement & Transfer Ledger</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Transfers: {pageData.totalElements}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Transfer Date</th>
                  <th>Effective Date</th>
                  <th>Worker Name & ID</th>
                  <th>Designation</th>
                  <th>Transferred From (Origin)</th>
                  <th>Transferred To (Destination)</th>
                  <th>Reason & Remarks</th>
                  <th>Authorized By</th>
                </tr>
              </thead>
              <tbody>
                {transfers.length > 0 ? (
                  transfers.map((t) => (
                    <tr key={t.id}>
                      <td>{t.transferDate}</td>
                      <td><strong>{t.effectiveDate}</strong></td>
                      <td>
                        <strong>{t.labourName}</strong>
                        <div className="font-mono" style={{ fontSize: '11px', color: '#64748B' }}>{t.labourCode}</div>
                      </td>
                      <td>{t.designation}</td>
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: 'var(--gov-secondary-text)' }}>
                          <MapPin size={12} /> {t.fromSiteName || 'Unassigned / Depot'}
                        </span>
                      </td>
                      <td>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: 'var(--gov-success)', fontWeight: 700 }}>
                          <MapPin size={12} /> {t.toSiteName}
                        </span>
                      </td>
                      <td>
                        <div>{t.reason || 'Operational deployment'}</div>
                        {t.remarks && <div style={{ fontSize: '11px', color: '#64748B' }}>{t.remarks}</div>}
                      </td>
                      <td>{t.transferredBy || 'SYSTEM'}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" className="text-center" style={{ padding: '24px' }}>
                      {loading ? 'Retrieving transfer ledger...' : 'No labour transfers logged yet.'}
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
              onClick={() => fetchTransfers(pageData.pageNumber - 1)}
              className="gov-page-btn"
            >
              Previous
            </button>
            <button
              disabled={pageData.pageNumber + 1 >= pageData.totalPages}
              onClick={() => fetchTransfers(pageData.pageNumber + 1)}
              className="gov-page-btn"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Execute Transfer Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Execute Inter-Site Workforce Transfer"
        size="lg"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Select Labourer</label>
              <select
                className="gov-form-select"
                required
                value={formData.labourerId}
                onChange={(e) => setFormData({ ...formData, labourerId: e.target.value })}
              >
                <option value="">-- Choose Worker --</option>
                {labourers.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.fullName} ({l.labourCode}) — Current Site: {l.currentSiteName || 'None'}
                  </option>
                ))}
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">New Destination Site</label>
              <select
                className="gov-form-select"
                required
                value={formData.toSiteId}
                onChange={(e) => setFormData({ ...formData, toSiteId: e.target.value })}
              >
                <option value="">-- Choose New Site --</option>
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
                ))}
              </select>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Transfer Order Date</label>
              <input
                type="date"
                required
                className="gov-form-input"
                value={formData.transferDate}
                onChange={(e) => setFormData({ ...formData, transferDate: e.target.value })}
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Effective Reporting Date</label>
              <input
                type="date"
                required
                className="gov-form-input"
                value={formData.effectiveDate}
                onChange={(e) => setFormData({ ...formData, effectiveDate: e.target.value })}
              />
            </div>
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label required">Operational Transfer Reason</label>
            <input
              type="text"
              required
              className="gov-form-input"
              value={formData.reason}
              onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
              placeholder="e.g. Critical high-tension cable pulling required at Sector 62 Substation"
            />
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Additional Remarks & Instructions</label>
            <textarea
              className="gov-form-textarea"
              rows={2}
              value={formData.remarks}
              onChange={(e) => setFormData({ ...formData, remarks: e.target.value })}
              placeholder="Reporting instructions, travel compensation notes"
            />
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Executing Transfer...' : 'Commit Site Transfer'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
