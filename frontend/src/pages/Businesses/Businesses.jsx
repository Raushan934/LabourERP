import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Modal } from '../../components/common/Modal';
import api from '../../services/api';
import { Building2, Plus, Edit3, Sliders, CheckCircle2, XCircle, ShieldAlert } from 'lucide-react';

export const Businesses = () => {
  const { businesses, fetchBusinesses, switchBusiness, activeBusinessId } = useBusiness();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isModuleModalOpen, setIsModuleModalOpen] = useState(false);
  const [selectedBiz, setSelectedBiz] = useState(null);
  const [bizModules, setBizModules] = useState([]);
  const [formData, setFormData] = useState({
    businessCode: '',
    businessName: '',
    businessType: 'ELECTRICAL_CONTRACTOR',
    registrationNo: '',
    gstin: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    pincode: '',
    contactEmail: '',
    contactPhone: '',
    status: 'ACTIVE'
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const openAddModal = () => {
    setSelectedBiz(null);
    setFormData({
      businessCode: '',
      businessName: '',
      businessType: 'ELECTRICAL_CONTRACTOR',
      registrationNo: '',
      gstin: '',
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      pincode: '',
      contactEmail: '',
      contactPhone: '',
      status: 'ACTIVE'
    });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (b) => {
    setSelectedBiz(b);
    setFormData({ ...b });
    setError('');
    setIsModalOpen(true);
  };

  const openModuleConfig = async (b) => {
    setSelectedBiz(b);
    try {
      const res = await api.get(`/businesses/${b.id}/modules`);
      if (res.data && res.data.data) {
        setBizModules(res.data.data);
      }
      setIsModuleModalOpen(true);
    } catch (err) {
      console.error('Failed to load modules', err);
    }
  };

  const handleToggleModule = async (moduleCode, currentStatus) => {
    if (!selectedBiz) return;
    try {
      await api.patch(`/businesses/${selectedBiz.id}/modules/${moduleCode}`, { enabled: !currentStatus });
      setBizModules((prev) =>
        prev.map((m) => (m.moduleCode === moduleCode ? { ...m, enabled: !currentStatus } : m))
      );
    } catch (err) {
      console.error('Error toggling module', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      if (selectedBiz) {
        await api.put(`/businesses/${selectedBiz.id}`, formData);
      } else {
        await api.post('/businesses', formData);
      }
      setIsModalOpen(false);
      fetchBusinesses();
    } catch (err) {
      setError(err.response?.data?.message || 'Error saving business entity');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Multi-Business Management' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Multi-Business & Enterprise Entity Register</h2>
          <p>Centralized multi-tenant registration with strict relational and reporting data isolation</p>
        </div>
        <div>
          <button onClick={openAddModal} className="gov-btn gov-btn-primary">
            <Plus size={14} /> Register New Business
          </button>
        </div>
      </div>

      <div style={{ backgroundColor: 'var(--gov-light-blue)', border: '1px solid var(--gov-border)', padding: '10px 14px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px' }}>
        <ShieldAlert size={16} color="var(--gov-navy)" />
        <span>
          <strong>Data Isolation Assurance:</strong> All sites, labourers, attendance rosters, wages, and financial transactions are strictly partitioned by Business ID. Cross-business leakage is blocked at the backend API gateway.
        </span>
      </div>

      <div className="gov-card">
        <div className="gov-card-header">
          <span>Registered Enterprises & Operating Entities</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total: {businesses.length}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Business Code</th>
                  <th>Enterprise / Organization Name</th>
                  <th>Business Category</th>
                  <th>Registration & GSTIN</th>
                  <th>Location (City, State)</th>
                  <th>Contact Details</th>
                  <th className="text-center">Active Scope</th>
                  <th className="text-center">Status</th>
                  <th>Administrative Actions</th>
                </tr>
              </thead>
              <tbody>
                {businesses.map((b) => (
                  <tr key={b.id} style={{ backgroundColor: b.id === activeBusinessId ? 'var(--gov-light-blue)' : undefined }}>
                    <td className="font-mono" style={{ fontWeight: 700 }}>{b.businessCode}</td>
                    <td>
                      <strong>{b.businessName}</strong>
                    </td>
                    <td>{b.businessType}</td>
                    <td style={{ fontSize: '12px' }}>
                      Reg: {b.registrationNo || 'N/A'}<br />
                      GST: {b.gstin || 'N/A'}
                    </td>
                    <td>{b.city}, {b.state} ({b.pincode})</td>
                    <td style={{ fontSize: '12px' }}>
                      {b.contactPhone}<br />
                      {b.contactEmail}
                    </td>
                    <td className="text-center">
                      {b.id === activeBusinessId ? (
                        <span className="gov-badge active" style={{ backgroundColor: '#245A85', color: '#FFFFFF' }}>
                          CURRENT CONTEXT
                        </span>
                      ) : (
                        <button
                          onClick={() => switchBusiness(b.id)}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                        >
                          Switch To
                        </button>
                      )}
                    </td>
                    <td className="text-center">
                      <StatusBadge status={b.status} />
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '4px' }}>
                        <button
                          onClick={() => openEditModal(b)}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                          title="Edit details"
                        >
                          <Edit3 size={12} /> Edit
                        </button>
                        <button
                          onClick={() => openModuleConfig(b)}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                          title="Configure business modules"
                        >
                          <Sliders size={12} /> Modules
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Add / Edit Business Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={selectedBiz ? `Edit Business: ${selectedBiz.businessName}` : 'Register New Enterprise / Business Entity'}
        size="lg"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Business Code (Unique)</label>
              <input
                type="text"
                required
                disabled={!!selectedBiz}
                className="gov-form-input font-mono"
                value={formData.businessCode}
                onChange={(e) => setFormData({ ...formData, businessCode: e.target.value.toUpperCase() })}
                placeholder="e.g. ELEC-CORP-01"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Business / Company Name</label>
              <input
                type="text"
                required
                className="gov-form-input"
                value={formData.businessName}
                onChange={(e) => setFormData({ ...formData, businessName: e.target.value })}
                placeholder="Full Registered Name"
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Business Type / Industry</label>
              <select
                className="gov-form-select"
                value={formData.businessType}
                onChange={(e) => setFormData({ ...formData, businessType: e.target.value })}
              >
                <option value="ELECTRICAL_CONTRACTOR">Electrical Contractor</option>
                <option value="PETROL_PUMP">Petrol Pump / Fuel Station</option>
                <option value="HOSPITAL">Hospital / Healthcare</option>
                <option value="CONSTRUCTION">Civil Construction</option>
                <option value="RETAIL">Retail / Commercial</option>
                <option value="OTHER">Other Enterprise</option>
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Registration / Incorporation Number</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.registrationNo}
                onChange={(e) => setFormData({ ...formData, registrationNo: e.target.value })}
                placeholder="e.g. REG/DL/2026/..."
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">GSTIN Identification Number</label>
              <input
                type="text"
                className="gov-form-input font-mono"
                value={formData.gstin}
                onChange={(e) => setFormData({ ...formData, gstin: e.target.value })}
                placeholder="15-character GSTIN"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Operational Status</label>
              <select
                className="gov-form-select"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Address Line 1</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.addressLine1}
                onChange={(e) => setFormData({ ...formData, addressLine1: e.target.value })}
                placeholder="Plot / Street / Office number"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">City & State</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  placeholder="City"
                />
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.state}
                  onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                  placeholder="State"
                />
              </div>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Postal Pincode</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.pincode}
                onChange={(e) => setFormData({ ...formData, pincode: e.target.value })}
                placeholder="6-digit PIN"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Contact Phone</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.contactPhone}
                onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                placeholder="Company Phone / Mobile"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Contact Email</label>
              <input
                type="email"
                className="gov-form-input"
                value={formData.contactEmail}
                onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
                placeholder="Company Email"
              />
            </div>
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Saving...' : selectedBiz ? 'Update Business Details' : 'Register Business'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Business Modules Toggle Modal */}
      <Modal
        isOpen={isModuleModalOpen}
        onClose={() => setIsModuleModalOpen(false)}
        title={`Configure Feature Modules for: ${selectedBiz?.businessName}`}
        size="default"
      >
        <p style={{ fontSize: '13px', color: 'var(--gov-secondary-text)', marginBottom: '12px' }}>
          Enable or disable functional modules according to operational needs. Disabled modules are hidden from navigation and inaccessible via APIs.
        </p>

        <div className="gov-table-container">
          <table className="gov-table">
            <thead>
              <tr>
                <th>Module Name</th>
                <th>Code</th>
                <th className="text-center">Status</th>
                <th className="text-center">Action</th>
              </tr>
            </thead>
            <tbody>
              {bizModules.map((m) => (
                <tr key={m.id}>
                  <td><strong>{m.moduleName}</strong></td>
                  <td className="font-mono">{m.moduleCode}</td>
                  <td className="text-center">
                    <span className={`gov-badge ${m.enabled ? 'active' : 'inactive'}`}>
                      {m.enabled ? 'ENABLED' : 'DISABLED'}
                    </span>
                  </td>
                  <td className="text-center">
                    <button
                      onClick={() => handleToggleModule(m.moduleCode, m.enabled)}
                      className={`gov-btn gov-btn-sm ${m.enabled ? 'gov-btn-danger' : 'gov-btn-success'}`}
                    >
                      {m.enabled ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="gov-modal-footer">
          <button type="button" onClick={() => setIsModuleModalOpen(false)} className="gov-btn gov-btn-primary">
            Done
          </button>
        </div>
      </Modal>
    </div>
  );
};
