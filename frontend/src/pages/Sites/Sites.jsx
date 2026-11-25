import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { useAuth } from '../../context/AuthContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { Modal } from '../../components/common/Modal';
import { CustomFieldInputs } from '../../components/common/CustomFieldInputs';
import api from '../../services/api';
import { MapPin, Plus, Search, Edit3, Users, Filter, CheckCircle } from 'lucide-react';

export const Sites = () => {
  const { activeBusinessId } = useBusiness();
  const { isAdmin, isManager, isSupervisor, user } = useAuth();
  const [sites, setSites] = useState([]);
  const [pageData, setPageData] = useState({ pageNumber: 0, pageSize: 15, totalElements: 0, totalPages: 1 });
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedSite, setSelectedSite] = useState(null);
  const [formData, setFormData] = useState({
    siteCode: '',
    siteName: '',
    clientName: '',
    siteCategory: 'ELECTRICAL_INSTALLATION',
    locationAddress: '',
    city: '',
    state: '',
    siteManagerId: '',
    supervisorId: '',
    startDate: '',
    expectedEndDate: '',
    status: 'ACTIVE',
    notes: '',
    customFields: {}
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchSites = async (page = 0) => {
    if (!activeBusinessId) return;
    setLoading(true);
    try {
      let url = `/sites?businessId=${activeBusinessId}&page=${page}&size=15`;
      if (search) url += `&search=${encodeURIComponent(search)}`;
      if (statusFilter) url += `&status=${encodeURIComponent(statusFilter)}`;

      const res = await api.get(url);
      if (res.data && res.data.data) {
        setSites(res.data.data.content);
        setPageData({
          pageNumber: res.data.data.pageNumber,
          pageSize: res.data.data.pageSize,
          totalElements: res.data.data.totalElements,
          totalPages: res.data.data.totalPages
        });
      }
    } catch (err) {
      console.error('Failed to load sites', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSites(0);
  }, [activeBusinessId, statusFilter]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    fetchSites(0);
  };

  const openAddModal = () => {
    setSelectedSite(null);
    setFormData({
      siteCode: '',
      siteName: '',
      clientName: '',
      siteCategory: 'ELECTRICAL_INSTALLATION',
      locationAddress: '',
      city: '',
      state: '',
      siteManagerId: '',
      supervisorId: '',
      startDate: new Date().toISOString().split('T')[0],
      expectedEndDate: '',
      status: 'ACTIVE',
      notes: '',
      customFields: {}
    });
    setError('');
    setIsModalOpen(true);
  };

  const openEditModal = (site) => {
    setSelectedSite(site);
    setFormData({
      ...site,
      customFields: site.customFields || {}
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
        businessId: activeBusinessId
      };
      if (selectedSite) {
        await api.put(`/sites/${selectedSite.id}`, payload);
      } else {
        await api.post('/sites', payload);
      }
      setIsModalOpen(false);
      fetchSites(pageData.pageNumber);
    } catch (err) {
      setError(err.response?.data?.message || 'Error saving site details');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Site Management' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Construction & Project Site Management</h2>
          <p>Register, inspect, and assign workforce to electrical contract and work sites</p>
        </div>
        {(isAdmin() || isManager()) && (
          <div>
            <button onClick={openAddModal} className="gov-btn gov-btn-primary">
              <Plus size={14} /> Add New Site
            </button>
          </div>
        )}
      </div>

      {isSupervisor() && user?.assignedSiteName && (
        <div className="gov-alert gov-alert-info" style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
          <MapPin size={16} />
          <span>You are deputed as Site Supervisor for <strong>{user.assignedSiteName} ({user.assignedSiteCode})</strong>.</span>
        </div>
      )}

      {/* Filter and Search Bar */}
      <form onSubmit={handleSearchSubmit} className="gov-filter-bar">
        <div style={{ flex: '1 1 240px' }}>
          <label className="gov-form-label">Search Sites</label>
          <div style={{ display: 'flex', gap: '6px' }}>
            <input
              type="text"
              className="gov-form-input"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by site code, name, client..."
            />
            <button type="submit" className="gov-btn gov-btn-secondary">
              <Search size={13} />
            </button>
          </div>
        </div>

        <div style={{ width: '180px' }}>
          <label className="gov-form-label">Status Filter</label>
          <select
            className="gov-form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="ON_HOLD">ON HOLD</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
      </form>

      {/* Sites Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Active Site Register</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Records: {pageData.totalElements}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Site Code</th>
                  <th>Site Name</th>
                  <th>Client Organization</th>
                  <th>Category</th>
                  <th>Location (City, State)</th>
                  <th>Supervisor In-Charge</th>
                  <th className="text-center">Assigned Labour</th>
                  <th className="text-center">Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sites.length > 0 ? (
                  sites.map((s) => (
                    <tr key={s.id}>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{s.siteCode}</td>
                      <td>
                        <strong>{s.siteName}</strong>
                        {s.notes && <div style={{ fontSize: '11px', color: '#64748B' }}>{s.notes}</div>}
                      </td>
                      <td>{s.clientName || 'N/A'}</td>
                      <td>{s.siteCategory?.replace(/_/g, ' ')}</td>
                      <td>{s.city || 'N/A'}, {s.state || ''}</td>
                      <td>{s.supervisorName || 'Unassigned'}</td>
                      <td className="text-center" style={{ fontWeight: 700 }}>
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <Users size={12} /> {s.assignedLabourCount}
                        </span>
                      </td>
                      <td className="text-center">
                        <StatusBadge status={s.status} />
                      </td>
                      <td>
                        <button
                          onClick={() => openEditModal(s)}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                        >
                          <Edit3 size={12} /> Edit
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="9" className="text-center" style={{ padding: '20px' }}>
                      {loading ? 'Retrieving site records...' : 'No site records match the search criteria.'}
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
              onClick={() => fetchSites(pageData.pageNumber - 1)}
              className="gov-page-btn"
            >
              Previous
            </button>
            <button
              disabled={pageData.pageNumber + 1 >= pageData.totalPages}
              onClick={() => fetchSites(pageData.pageNumber + 1)}
              className="gov-page-btn"
            >
              Next
            </button>
          </div>
        </div>
      )}

      {/* Add / Edit Site Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={selectedSite ? `Edit Site: ${selectedSite.siteName}` : 'Add New Work / Construction Site'}
        size="lg"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Site Code (Unique)</label>
              <input
                type="text"
                required
                disabled={!!selectedSite}
                className="gov-form-input font-mono"
                value={formData.siteCode}
                onChange={(e) => setFormData({ ...formData, siteCode: e.target.value.toUpperCase() })}
                placeholder="e.g. SITE-NOI-62"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Site / Project Name</label>
              <input
                type="text"
                required
                className="gov-form-input"
                value={formData.siteName}
                onChange={(e) => setFormData({ ...formData, siteName: e.target.value })}
                placeholder="e.g. Sector 62 Substation Expansion"
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Client Organization / Authority</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.clientName}
                onChange={(e) => setFormData({ ...formData, clientName: e.target.value })}
                placeholder="e.g. Noida Power Distribution Ltd"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Site Classification</label>
              <select
                className="gov-form-select"
                value={formData.siteCategory}
                onChange={(e) => setFormData({ ...formData, siteCategory: e.target.value })}
              >
                <option value="ELECTRICAL_SUBSTATION">Electrical Substation</option>
                <option value="COMMERCIAL_TRANSIT">Metro / Rail Electrification</option>
                <option value="COMMERCIAL_BUILDING">Commercial High-Rise</option>
                <option value="INDUSTRIAL_PLANT">Industrial Factory / Plant</option>
                <option value="RESIDENTIAL">Residential Complex</option>
              </select>
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Physical Site Address</label>
              <input
                type="text"
                className="gov-form-input"
                value={formData.locationAddress}
                onChange={(e) => setFormData({ ...formData, locationAddress: e.target.value })}
                placeholder="Plot / Corridor / Milepost"
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
              <label className="gov-form-label">Project Start Date</label>
              <input
                type="date"
                className="gov-form-input"
                value={formData.startDate || ''}
                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Expected Completion Date</label>
              <input
                type="date"
                className="gov-form-input"
                value={formData.expectedEndDate || ''}
                onChange={(e) => setFormData({ ...formData, expectedEndDate: e.target.value })}
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
                <option value="ON_HOLD">ON HOLD</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Operational Notes & Remarks</label>
            <textarea
              className="gov-form-textarea"
              rows={2}
              value={formData.notes || ''}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              placeholder="High-tension safety guidelines, security contact, etc."
            />
          </div>

          {/* Dynamic Custom Fields Integration */}
          <CustomFieldInputs
            businessId={activeBusinessId}
            entityType="SITE"
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
              {loading ? 'Saving Site...' : selectedSite ? 'Update Site' : 'Register Site'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
