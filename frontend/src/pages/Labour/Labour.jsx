import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { useAuth } from '../../context/AuthContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Modal } from '../../components/common/Modal';
import { CustomFieldInputs } from '../../components/common/CustomFieldInputs';
import { PaymentDetailsModal } from '../../components/common/PaymentDetailsModal';
import api from '../../services/api';
import {
  Users,
  Plus,
  Search,
  Edit3,
  UserX,
  FileText,
  MapPin,
  Phone,
  ShieldCheck,
  IndianRupee,
  QrCode,
  CreditCard,
  Smartphone,
  Building
} from 'lucide-react';

export const Labour = () => {
  const { activeBusinessId } = useBusiness();
  const { isAdmin, isManager, isSupervisor, isAccountant, user } = useAuth();
  const [labourers, setLabourers] = useState([]);
  const [sites, setSites] = useState([]);
  const [categories, setCategories] = useState([]);
  const [designations, setDesignations] = useState([]);
  const [pageData, setPageData] = useState({ pageNumber: 0, pageSize: 15, totalElements: 0, totalPages: 1 });
  const [search, setSearch] = useState('');
  const [siteFilter, setSiteFilter] = useState(() => (user?.assignedSiteId ? user.assignedSiteId.toString() : ''));
  const [categoryFilter, setCategoryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeactivateModalOpen, setIsDeactivateModalOpen] = useState(false);
  const [selectedLabourer, setSelectedLabourer] = useState(null);
  const [deactivateReason, setDeactivateReason] = useState('');

  const [paymentModalOpen, setPaymentModalOpen] = useState(false);
  const [paymentTarget, setPaymentTarget] = useState(null);

  const [formData, setFormData] = useState({
    labourCode: '',
    fullName: '',
    mobileNumber: '',
    altMobileNumber: '',
    aadharNumber: '',
    address: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    dateOfJoining: '',
    designation: '',
    labourCategory: '',
    skillLevel: 'SKILLED',
    dailyWage: '',
    overtimeHourlyRate: '',
    currentSiteId: '',
    status: 'ACTIVE',
    notes: '',
    upiId: '',
    bankAccountNumber: '',
    bankIfscCode: '',
    bankName: '',
    bankBeneficiaryName: '',
    qrCodeUrl: '',
    customFields: {}
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Fetch Master Data (Categories, Designations, Sites) dynamically
  useEffect(() => {
    if (activeBusinessId) {
      api.get(`/sites/all?businessId=${activeBusinessId}`)
        .then((res) => res.data?.data && setSites(res.data.data))
        .catch(console.error);

      api.get(`/master-data/values?category=LABOUR_CATEGORY&businessId=${activeBusinessId}&activeOnly=true`)
        .then((res) => res.data?.data && setCategories(res.data.data))
        .catch(console.error);

      api.get(`/master-data/values?category=DESIGNATION&businessId=${activeBusinessId}&activeOnly=true`)
        .then((res) => res.data?.data && setDesignations(res.data.data))
        .catch(console.error);
    }
  }, [activeBusinessId]);

  const fetchLabourers = async (page = 0) => {
    if (!activeBusinessId) return;
    setLoading(true);
    try {
      let url = `/labour?businessId=${activeBusinessId}&page=${page}&size=15`;
      if (search) url += `&search=${encodeURIComponent(search)}`;
      if (siteFilter) url += `&siteId=${encodeURIComponent(siteFilter)}`;
      if (categoryFilter) url += `&category=${encodeURIComponent(categoryFilter)}`;
      if (statusFilter) url += `&status=${encodeURIComponent(statusFilter)}`;

      const res = await api.get(url);
      if (res.data && res.data.data) {
        setLabourers(res.data.data.content);
        setPageData({
          pageNumber: res.data.data.pageNumber,
          pageSize: res.data.data.pageSize,
          totalElements: res.data.data.totalElements,
          totalPages: res.data.data.totalPages
        });
      }
    } catch (err) {
      console.error('Failed to load labourers', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLabourers(0);
  }, [activeBusinessId, siteFilter, categoryFilter, statusFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchLabourers(0);
  };

  const openAddModal = () => {
    setSelectedLabourer(null);
    setFormData({
      labourCode: 'LBR-' + Math.floor(100 + Math.random() * 900),
      fullName: '',
      mobileNumber: '',
      altMobileNumber: '',
      aadharNumber: '',
      address: '',
      emergencyContactName: '',
      emergencyContactPhone: '',
      dateOfJoining: new Date().toISOString().split('T')[0],
      designation: designations[0]?.name || 'Senior Electrician',
      labourCategory: categories[0]?.code || 'ELECTRICIAN',
      skillLevel: 'SKILLED',
      dailyWage: '800.00',
      overtimeHourlyRate: '100.00',
      currentSiteId: sites[0]?.id || '',
      status: 'ACTIVE',
      notes: '',
      upiId: '',
      bankAccountNumber: '',
      bankIfscCode: '',
      bankName: '',
      bankBeneficiaryName: '',
      qrCodeUrl: '',
      customFields: {}
    });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (labourer) => {
    setSelectedLabourer(labourer);
    setFormData({
      ...labourer,
      dailyWage: labourer.dailyWage.toString(),
      overtimeHourlyRate: labourer.overtimeHourlyRate?.toString() || '0.00',
      currentSiteId: labourer.currentSiteId || '',
      upiId: labourer.upiId || '',
      bankAccountNumber: labourer.bankAccountNumber || '',
      bankIfscCode: labourer.bankIfscCode || '',
      bankName: labourer.bankName || '',
      bankBeneficiaryName: labourer.bankBeneficiaryName || '',
      qrCodeUrl: labourer.qrCodeUrl || '',
      customFields: labourer.customFields || {}
    });
    setError('');
    setIsModalOpen(true);
  };

  const openDeactivateModal = (labourer) => {
    setSelectedLabourer(labourer);
    setDeactivateReason('');
    setIsDeactivateModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const payload = {
        ...formData,
        businessId: activeBusinessId,
        dailyWage: parseFloat(formData.dailyWage),
        overtimeHourlyRate: parseFloat(formData.overtimeHourlyRate || 0)
      };

      if (selectedLabourer) {
        await api.put(`/labour/${selectedLabourer.id}`, payload);
      } else {
        await api.post('/labour', payload);
      }
      setIsModalOpen(false);
      fetchLabourers(pageData.pageNumber);
    } catch (err) {
      setError(err.response?.data?.message || 'Error saving labourer profile');
    } finally {
      setLoading(false);
    }
  };

  const handleDeactivate = async () => {
    if (!selectedLabourer) return;
    try {
      await api.post(`/labour/${selectedLabourer.id}/deactivate`, { reason: deactivateReason });
      setIsDeactivateModalOpen(false);
      fetchLabourers(pageData.pageNumber);
    } catch (err) {
      console.error('Failed to deactivate labourer', err);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Labour Management' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Labour Workforce Directory & Profiles</h2>
          <p>Maintain worker service registers, daily wage rates, site assignments, and historical service logs</p>
        </div>
        {(isAdmin() || isManager()) && (
          <div>
            <button onClick={openAddModal} className="gov-btn gov-btn-primary">
              <Plus size={14} /> Enrol New Worker
            </button>
          </div>
        )}
      </div>

      {isSupervisor() && user?.assignedSiteName && (
        <div className="gov-alert gov-alert-info" style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
          <MapPin size={16} />
          <span>Showing workforce deployed at your assigned site: <strong>{user.assignedSiteName} ({user.assignedSiteCode})</strong>.</span>
        </div>
      )}

      {/* Filter and Search Panel */}
      <form onSubmit={handleSearchSubmit} className="gov-filter-bar">
        <div style={{ flex: '1 1 200px' }}>
          <label className="gov-form-label">Search Worker</label>
          <div style={{ display: 'flex', gap: '6px' }}>
            <input
              type="text"
              className="gov-form-input"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search Name, ID, Mobile..."
            />
            <button type="submit" className="gov-btn gov-btn-secondary">
              <Search size={13} />
            </button>
          </div>
        </div>

        <div style={{ width: '180px' }}>
          <label className="gov-form-label">Site Assignment</label>
          <select
            className="gov-form-select"
            value={siteFilter}
            onChange={(e) => setSiteFilter(e.target.value)}
          >
            <option value="">All Assigned Sites</option>
            {sites.map((s) => (
              <option key={s.id} value={s.id}>{s.siteName}</option>
            ))}
          </select>
        </div>

        <div style={{ width: '160px' }}>
          <label className="gov-form-label">Category</label>
          <select
            className="gov-form-select"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.code}>{c.name}</option>
            ))}
          </select>
        </div>

        <div style={{ width: '140px' }}>
          <label className="gov-form-label">Status</label>
          <select
            className="gov-form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="INACTIVE">INACTIVE</option>
          </select>
        </div>
      </form>

      {/* Workforce Data Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Workforce Register</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Enrolled: {pageData.totalElements}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Labour ID</th>
                  <th>Full Name</th>
                  <th>Mobile / Contact</th>
                  <th>Designation</th>
                  <th>Category</th>
                  <th>Assigned Site</th>
                  <th className="text-right">Daily Wage (₹)</th>
                  <th className="text-right">OT Rate (₹/hr)</th>
                  <th className="text-right">Advance Due (₹)</th>
                  <th>Payment & QR</th>
                  <th className="text-center">Status</th>
                  <th>Administrative Actions</th>
                </tr>
              </thead>
              <tbody>
                {labourers.length > 0 ? (
                  labourers.map((l) => (
                    <tr key={l.id}>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{l.labourCode}</td>
                      <td>
                        <strong>{l.fullName}</strong>
                        {l.aadharNumber && (
                          <div style={{ fontSize: '11px', color: '#64748B' }}>UIDAI: {l.aadharNumber}</div>
                        )}
                      </td>
                      <td>{l.mobileNumber}</td>
                      <td>{l.designation}</td>
                      <td>
                        <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--gov-blue)' }}>
                          {l.labourCategory}
                        </span>
                      </td>
                      <td>
                        {l.currentSiteName ? (
                          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
                            <MapPin size={12} color="var(--gov-navy)" /> {l.currentSiteName}
                          </span>
                        ) : (
                          <span style={{ color: '#94A3B8' }}>Unassigned</span>
                        )}
                      </td>
                      <td className="text-right font-mono" style={{ fontWeight: 700 }}>
                        ₹{l.dailyWage.toFixed(2)}
                      </td>
                      <td className="text-right font-mono">
                        ₹{l.overtimeHourlyRate?.toFixed(2) || '0.00'}
                      </td>
                      <td className="text-right font-mono" style={{ color: l.outstandingAdvance > 0 ? 'var(--gov-danger)' : undefined, fontWeight: 600 }}>
                        ₹{l.outstandingAdvance?.toFixed(2) || '0.00'}
                      </td>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
                          <button
                            type="button"
                            onClick={() => {
                              setPaymentTarget(l);
                              setPaymentModalOpen(true);
                            }}
                            className="gov-btn gov-btn-secondary gov-btn-sm"
                            style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', padding: '3px 7px', fontSize: '11px', width: 'fit-content' }}
                            title="Scan QR code, view UPI ID or bank account"
                          >
                            <QrCode size={12} color="#2563EB" /> Pay / QR
                          </button>
                          <div style={{ display: 'flex', gap: '3px', flexWrap: 'wrap' }}>
                            <span className="gov-badge gov-badge-info" style={{ fontSize: '9px', padding: '1px 4px' }}>QR Ready</span>
                            {l.upiId ? (
                              <span className="gov-badge gov-badge-success" style={{ fontSize: '9px', padding: '1px 4px' }}>UPI</span>
                            ) : (
                              <span className="gov-badge gov-badge-success" style={{ fontSize: '9px', padding: '1px 4px' }}>Auto-UPI</span>
                            )}
                            {l.bankAccountNumber && <span className="gov-badge gov-badge-primary" style={{ fontSize: '9px', padding: '1px 4px' }}>Bank</span>}
                          </div>
                        </div>
                      </td>
                      <td className="text-center">
                        <StatusBadge status={l.status} />
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '4px' }}>
                          <button
                            onClick={() => openEditModal(l)}
                            className="gov-btn gov-btn-secondary gov-btn-sm"
                            title="Edit profile & wages"
                          >
                            <Edit3 size={12} /> Edit
                          </button>
                          <a
                            href={`/reports?labourId=${l.id}`}
                            className="gov-btn gov-btn-secondary gov-btn-sm"
                            title="Generate worker statement"
                          >
                            <FileText size={12} /> Statement
                          </a>
                          {l.status === 'ACTIVE' && (
                            <button
                              onClick={() => openDeactivateModal(l)}
                              className="gov-btn gov-btn-danger gov-btn-sm"
                              title="Soft deactivate worker"
                            >
                              <UserX size={12} />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="12" className="text-center" style={{ padding: '20px' }}>
                      {loading ? 'Retrieving workforce records...' : 'No workers found matching the filters.'}
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
              onClick={() => fetchLabourers(pageData.pageNumber - 1)}
              className="gov-page-btn"
            >
              Previous
            </button>
            <button
              disabled={pageData.pageNumber + 1 >= pageData.totalPages}
              onClick={() => fetchLabourers(pageData.pageNumber + 1)}
              className="gov-page-btn"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Add / Edit Labour Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={selectedLabourer ? `Edit Worker: ${selectedLabourer.fullName} (${selectedLabourer.labourCode})` : 'Enrol New Workforce Member'}
        size="lg"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Labour ID / Code</label>
              <input
                type="text"
                required
                disabled={!!selectedLabourer}
                className="gov-form-input font-mono"
                value={formData.labourCode}
                onChange={(e) => setFormData({ ...formData, labourCode: e.target.value.toUpperCase() })}
                placeholder="e.g. LBR-001"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Worker Full Name</label>
              <input
                type="text"
                required
                className="gov-form-input"
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="Full Name as per KYC/ID"
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Primary Mobile Number</label>
              <input
                type="text"
                required
                className="gov-form-input"
                value={formData.mobileNumber}
                onChange={(e) => setFormData({ ...formData, mobileNumber: e.target.value })}
                placeholder="+91 9876543210"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Alternate Mobile / WhatsApp</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.altMobileNumber || ''}
                onChange={(e) => setFormData({ ...formData, altMobileNumber: e.target.value })}
                placeholder="Alternate phone"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Aadhar Identification Number</label>
              <input
                type="text"
                className="gov-form-input font-mono"
                value={formData.aadharNumber || ''}
                onChange={(e) => setFormData({ ...formData, aadharNumber: e.target.value })}
                placeholder="XXXX-XXXX-XXXX"
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Labour Skill Category (Dynamic Master)</label>
              <select
                className="gov-form-select"
                required
                value={formData.labourCategory}
                onChange={(e) => setFormData({ ...formData, labourCategory: e.target.value })}
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.code}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Designation (Dynamic Master)</label>
              <select
                className="gov-form-select"
                required
                value={formData.designation}
                onChange={(e) => setFormData({ ...formData, designation: e.target.value })}
              >
                {designations.map((d) => (
                  <option key={d.id} value={d.name}>{d.name}</option>
                ))}
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Skill Proficiency Level</label>
              <select
                className="gov-form-select"
                value={formData.skillLevel}
                onChange={(e) => setFormData({ ...formData, skillLevel: e.target.value })}
              >
                <option value="HIGHLY_SKILLED">Highly Skilled</option>
                <option value="SKILLED">Skilled</option>
                <option value="SEMI_SKILLED">Semi-Skilled</option>
                <option value="UNSKILLED">Unskilled Helper</option>
              </select>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Daily Base Wage Rate (₹)</label>
              <input
                type="number"
                step="0.01"
                required
                className="gov-form-input font-mono"
                value={formData.dailyWage}
                onChange={(e) => setFormData({ ...formData, dailyWage: e.target.value })}
                placeholder="e.g. 850.00"
              />
              <div className="gov-form-help">Rate changes are tracked in Wage Rate History</div>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Overtime Hourly Rate (₹)</label>
              <input
                type="number"
                step="0.01"
                required
                className="gov-form-input font-mono"
                value={formData.overtimeHourlyRate}
                onChange={(e) => setFormData({ ...formData, overtimeHourlyRate: e.target.value })}
                placeholder="e.g. 110.00"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Date of Joining</label>
              <input
                type="date"
                required
                className="gov-form-input"
                value={formData.dateOfJoining}
                onChange={(e) => setFormData({ ...formData, dateOfJoining: e.target.value })}
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Assigned Construction Site</label>
              <select
                className="gov-form-select"
                value={formData.currentSiteId || ''}
                onChange={(e) => setFormData({ ...formData, currentSiteId: e.target.value ? parseInt(e.target.value, 10) : '' })}
              >
                <option value="">-- No Site Assigned --</option>
                {sites.map((s) => (
                  <option key={s.id} value={s.id}>{s.siteName} ({s.siteCode})</option>
                ))}
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Emergency Contact Name & Phone</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.emergencyContactName || ''}
                  onChange={(e) => setFormData({ ...formData, emergencyContactName: e.target.value })}
                  placeholder="Contact Person"
                />
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.emergencyContactPhone || ''}
                  onChange={(e) => setFormData({ ...formData, emergencyContactPhone: e.target.value })}
                  placeholder="Emergency Phone"
                />
              </div>
            </div>
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Residential Address</label>
            <input
              type="text"
              className="gov-form-input"
              value={formData.address || ''}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              placeholder="Permanent / Village / Current address"
            />
          </div>

          {/* Payment & Settlement Options (QR, UPI, Bank) */}
          <div style={{ backgroundColor: '#F8FAFC', border: '1px solid #CBD5E1', padding: '14px', borderRadius: '4px', marginBottom: '14px' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <CreditCard size={15} color="#2563EB" /> Payment & Settlement Credentials (Optional: QR Pic, UPI ID, or Bank Account)
            </div>
            <div style={{ fontSize: '11px', color: '#64748B', marginBottom: '10px' }}>
              Fill any one or all options. Leave unused fields blank.
            </div>

            <div className="gov-form-row">
              <div className="gov-form-col">
                <label className="gov-form-label">Mobile UPI ID / Number</label>
                <input
                  type="text"
                  className="gov-form-input font-mono"
                  value={formData.upiId || ''}
                  onChange={(e) => setFormData({ ...formData, upiId: e.target.value })}
                  placeholder="e.g. 9811223344@upi"
                />
              </div>
              <div className="gov-form-col">
                <label className="gov-form-label">QR Code Picture URL / Link</label>
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.qrCodeUrl || ''}
                  onChange={(e) => setFormData({ ...formData, qrCodeUrl: e.target.value })}
                  placeholder="Image URL or generate from Pay / QR modal"
                />
              </div>
            </div>

            <div className="gov-form-row">
              <div className="gov-form-col">
                <label className="gov-form-label">Bank Account Number</label>
                <input
                  type="text"
                  className="gov-form-input font-mono"
                  value={formData.bankAccountNumber || ''}
                  onChange={(e) => setFormData({ ...formData, bankAccountNumber: e.target.value })}
                  placeholder="e.g. 10293847561"
                />
              </div>
              <div className="gov-form-col">
                <label className="gov-form-label">Bank IFSC Code</label>
                <input
                  type="text"
                  className="gov-form-input font-mono"
                  value={formData.bankIfscCode || ''}
                  onChange={(e) => setFormData({ ...formData, bankIfscCode: e.target.value.toUpperCase() })}
                  placeholder="e.g. SBIN0001234"
                />
              </div>
            </div>

            <div className="gov-form-row">
              <div className="gov-form-col">
                <label className="gov-form-label">Bank Name</label>
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.bankName || ''}
                  onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
                  placeholder="e.g. State Bank of India"
                />
              </div>
              <div className="gov-form-col">
                <label className="gov-form-label">Beneficiary Account Name</label>
                <input
                  type="text"
                  className="gov-form-input"
                  value={formData.bankBeneficiaryName || ''}
                  onChange={(e) => setFormData({ ...formData, bankBeneficiaryName: e.target.value })}
                  placeholder="Beneficiary name as per bank"
                />
              </div>
            </div>
          </div>

          {/* Dynamic Custom Fields */}
          <CustomFieldInputs
            businessId={activeBusinessId}
            entityType="LABOURER"
            values={formData.customFields}
            onChange={(key, val) =>
              setFormData({
                ...formData,
                customFields: { ...formData.customFields, [key]: val }
              })
            }
          />

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Processing...' : selectedLabourer ? 'Save Changes' : 'Enrol Worker'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Soft Deactivate Worker Modal */}
      <Modal
        isOpen={isDeactivateModalOpen}
        onClose={() => setIsDeactivateModalOpen(false)}
        title={`Confirm Worker Deactivation: ${selectedLabourer?.fullName}`}
        size="default"
      >
        <div style={{ fontSize: '13px', color: 'var(--gov-text)', marginBottom: '12px' }}>
          Deactivating worker <strong>{selectedLabourer?.fullName} ({selectedLabourer?.labourCode})</strong> marks their service record inactive.
          <br /><br />
          <strong>Regulatory Compliance Assurance:</strong> Their historical attendance, wages, advances, and disbursement ledgers will remain completely intact and permanently auditable.
        </div>

        <div className="gov-form-group">
          <label className="gov-form-label required">Reason for Deactivation</label>
          <textarea
            required
            className="gov-form-textarea"
            rows={3}
            value={deactivateReason}
            onChange={(e) => setDeactivateReason(e.target.value)}
            placeholder="e.g. Resignation, Project completion, Medical absence..."
          />
        </div>

        <div className="gov-modal-footer">
          <button type="button" onClick={() => setIsDeactivateModalOpen(false)} className="gov-btn gov-btn-secondary">
            Cancel
          </button>
          <button
            type="button"
            disabled={!deactivateReason.trim()}
            onClick={handleDeactivate}
            className="gov-btn gov-btn-danger"
          >
            Confirm Deactivation
          </button>
        </div>
      </Modal>

      {/* Payment Credentials & QR Scan Modal */}
      <PaymentDetailsModal
        isOpen={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        title={`Worker Payment Credentials: ${paymentTarget?.fullName}`}
        subtitle={`Labour ID: ${paymentTarget?.labourCode} | Site: ${paymentTarget?.currentSiteName || 'Unassigned'}`}
        paymentData={paymentTarget || {}}
        isEditable={isAdmin() || isAccountant()}
        onSave={async (details) => {
          await api.put(`/labour/${paymentTarget.id}`, { ...paymentTarget, ...details });
          fetchLabourers(pageData.pageNumber);
        }}
      />
    </div>
  );
};
