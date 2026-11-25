import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { useAuth } from '../../context/AuthContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PaymentDetailsModal } from '../../components/common/PaymentDetailsModal';
import api from '../../services/api';
import {
  UserCog,
  ShieldCheck,
  Plus,
  KeyRound,
  MapPin,
  Search,
  CheckCircle2,
  AlertCircle,
  X,
  Building,
  UserCheck,
  QrCode,
  CreditCard,
  Smartphone
} from 'lucide-react';

export const Users = () => {
  const { activeBusinessId } = useBusiness();
  const { isAdmin, isAccountant } = useAuth();
  const canManage = isAdmin() || isAccountant();
  const [users, setUsers] = useState([]);
  const [sites, setSites] = useState([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modals state
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [resetModalOpen, setResetModalOpen] = useState(false);
  const [deputeModalOpen, setDeputeModalOpen] = useState(false);
  const [paymentModalOpen, setPaymentModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [paymentTargetUser, setPaymentTargetUser] = useState(null);

  // Create User Form
  const [createForm, setCreateForm] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
    role: 'ROLE_SITE_SUPERVISOR',
    assignedSiteId: '',
    upiId: '',
    bankAccountNumber: '',
    bankIfscCode: '',
    bankName: '',
    bankBeneficiaryName: '',
    qrCodeUrl: ''
  });
  const [createLoading, setCreateLoading] = useState(false);

  // Reset Password Form
  const [newPassword, setNewPassword] = useState('');
  const [resetLoading, setResetLoading] = useState(false);

  // Depute Site Form
  const [targetSiteId, setTargetSiteId] = useState('');
  const [deputeLoading, setDeputeLoading] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/users');
      if (res.data?.data) {
        setUsers(res.data.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch user directory');
    } finally {
      setLoading(false);
    }
  };

  const fetchSites = async () => {
    if (!activeBusinessId) return;
    try {
      const res = await api.get(`/sites/all?businessId=${activeBusinessId}`);
      if (res.data?.data) {
        setSites(res.data.data);
      }
    } catch (err) {
      console.error('Failed to fetch sites:', err);
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchSites();
  }, [activeBusinessId]);

  const handleCreateUser = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setCreateLoading(true);
    try {
      const payload = {
        ...createForm,
        assignedSiteId: createForm.assignedSiteId ? parseInt(createForm.assignedSiteId) : null,
        businessId: activeBusinessId
      };
      await api.post('/users', payload);
      setSuccess(`User '${createForm.username}' created and synchronized with Supabase Auth successfully!`);
      setCreateModalOpen(false);
      setCreateForm({
        username: '',
        email: '',
        password: '',
        fullName: '',
        phone: '',
        role: 'ROLE_SITE_SUPERVISOR',
        assignedSiteId: ''
      });
      fetchUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create user');
    } finally {
      setCreateLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;
    setError('');
    setSuccess('');
    setResetLoading(true);
    try {
      await api.put(`/users/${selectedUser.id}/reset-password`, { newPassword });
      setSuccess(`Password for '${selectedUser.username}' reset and synchronized with Supabase Auth!`);
      setResetModalOpen(false);
      setNewPassword('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reset password');
    } finally {
      setResetLoading(false);
    }
  };

  const handleDeputeSite = async (e) => {
    e.preventDefault();
    if (!selectedUser) return;
    setError('');
    setSuccess('');
    setDeputeLoading(true);
    try {
      const siteIdVal = targetSiteId === '' ? -1 : parseInt(targetSiteId);
      await api.put(`/users/${selectedUser.id}`, {
        assignedSiteId: siteIdVal
      });
      setSuccess(`Site deputation for '${selectedUser.username}' updated successfully!`);
      setDeputeModalOpen(false);
      fetchUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update site deputation');
    } finally {
      setDeputeLoading(false);
    }
  };

  const filteredUsers = users.filter((u) => {
    const matchesSearch =
      !search ||
      u.username?.toLowerCase().includes(search.toLowerCase()) ||
      u.fullName?.toLowerCase().includes(search.toLowerCase()) ||
      u.email?.toLowerCase().includes(search.toLowerCase());

    const matchesRole = !roleFilter || (u.roles && u.roles.includes(roleFilter));
    return matchesSearch && matchesRole;
  });

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'gov-badge-danger';
      case 'ROLE_MANAGER':
        return 'gov-badge-primary';
      case 'ROLE_SITE_SUPERVISOR':
        return 'gov-badge-warning';
      case 'ROLE_ACCOUNTANT':
        return 'gov-badge-success';
      default:
        return 'gov-badge-secondary';
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Users & Access Permissions' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Enterprise User Directory & Role-Based Access</h2>
          <p>
            Create supervisors, managers, accountants, depute them to construction sites, and synchronize credentials with Supabase
          </p>
        </div>
        {isAdmin() && (
          <button
            type="button"
            onClick={() => setCreateModalOpen(true)}
            className="gov-btn gov-btn-primary"
            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
          >
            <Plus size={16} /> Create User / Depute Officer
          </button>
        )}
      </div>

      {success && (
        <div className="gov-alert gov-alert-success" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '14px' }}>
          <CheckCircle2 size={16} /> {success}
        </div>
      )}
      {error && (
        <div className="gov-alert gov-alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '14px' }}>
          <AlertCircle size={16} /> {error}
        </div>
      )}

      {/* Role Security Matrix Card */}
      <div style={{ backgroundColor: 'var(--gov-light-blue)', border: '1px solid var(--gov-border)', padding: '12px 16px', marginBottom: '16px', fontSize: '13px', borderRadius: '2px' }}>
        <div style={{ fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <ShieldCheck size={16} color="var(--gov-navy)" /> Role-Based Access Control (RBAC) & Site Deputation Matrix:
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '10px', marginTop: '8px' }}>
          <div style={{ backgroundColor: '#FFFFFF', padding: '8px 10px', border: '1px solid var(--gov-border)' }}>
            <span className="gov-badge gov-badge-danger" style={{ fontSize: '10px' }}>ADMIN / OWNER</span>
            <p style={{ margin: '4px 0 0 0', fontSize: '11px', color: '#4B5563' }}>Full CRUD access across all 13 modules, finances, master settings, audit logs, and user management.</p>
          </div>
          <div style={{ backgroundColor: '#FFFFFF', padding: '8px 10px', border: '1px solid var(--gov-border)' }}>
            <span className="gov-badge gov-badge-primary" style={{ fontSize: '10px' }}>OPERATIONS MANAGER</span>
            <p style={{ margin: '4px 0 0 0', fontSize: '11px', color: '#4B5563' }}>Operational control of assigned businesses & sites, workforce management, and muster review.</p>
          </div>
          <div style={{ backgroundColor: '#FFFFFF', padding: '8px 10px', border: '1px solid var(--gov-border)' }}>
            <span className="gov-badge gov-badge-warning" style={{ fontSize: '10px' }}>SITE SUPERVISOR</span>
            <p style={{ margin: '4px 0 0 0', fontSize: '11px', color: '#4B5563' }}>Strict site-specific workforce inspection & daily muster attendance logging. Finances hidden.</p>
          </div>
          <div style={{ backgroundColor: '#FFFFFF', padding: '8px 10px', border: '1px solid var(--gov-border)' }}>
            <span className="gov-badge gov-badge-success" style={{ fontSize: '10px' }}>ACCOUNTANT</span>
            <p style={{ margin: '4px 0 0 0', fontSize: '11px', color: '#4B5563' }}>Wage payroll calculations, worker advance disbursements, and payment voucher records.</p>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="gov-card" style={{ marginBottom: '16px', padding: '12px 16px' }}>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ position: 'relative', flex: 1, minWidth: '220px' }}>
            <Search size={14} style={{ position: 'absolute', left: '10px', top: '10px', color: '#64748B' }} />
            <input
              type="text"
              className="gov-form-input"
              style={{ paddingLeft: '32px' }}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by name, username, or email..."
            />
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              className="gov-form-input"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="">All System Roles</option>
              <option value="ROLE_ADMIN">Admin / Owner</option>
              <option value="ROLE_MANAGER">Operations Manager</option>
              <option value="ROLE_SITE_SUPERVISOR">Site Supervisor</option>
              <option value="ROLE_ACCOUNTANT">Financial Accountant</option>
            </select>
          </div>

          <div style={{ fontSize: '12px', color: 'var(--gov-secondary-text)', marginLeft: 'auto' }}>
            Showing <strong>{filteredUsers.length}</strong> of <strong>{users.length}</strong> users
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Authorized Users & Deputed Site Assignments</span>
          <span style={{ fontSize: '11px', color: '#CBD5E1' }}>Supabase Auth & Database Synchronized</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>User ID</th>
                  <th>Username / Email</th>
                  <th>Full Legal Name</th>
                  <th>Contact Phone</th>
                  <th>System Role</th>
                  <th>Deputed Site Assignment</th>
                  <th>Payment & QR</th>
                  <th className="text-center">Status</th>
                  {canManage && <th className="text-center">Administrative Actions</th>}
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((u) => (
                  <tr key={u.id}>
                    <td className="font-mono">#{u.id}</td>
                    <td>
                      <div className="font-mono" style={{ fontWeight: 700, color: 'var(--gov-navy)' }}>
                        {u.username}
                      </div>
                      <div style={{ fontSize: '11px', color: '#64748B' }}>{u.email}</div>
                    </td>
                    <td>
                      <strong>{u.fullName}</strong>
                    </td>
                    <td>{u.phone || '—'}</td>
                    <td>
                      {u.roles?.map((r) => (
                        <span key={r} className={`gov-badge ${getRoleBadgeClass(r)}`} style={{ marginRight: '4px' }}>
                          {r.replace('ROLE_', '')}
                        </span>
                      ))}
                    </td>
                    <td>
                      {u.assignedSiteName ? (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', color: '#1E40AF', fontWeight: 600 }}>
                          <MapPin size={13} color="#2563EB" />
                          <span>{u.assignedSiteName} ({u.assignedSiteCode})</span>
                        </div>
                      ) : (
                        <span style={{ fontSize: '11px', color: '#64748B', fontStyle: 'italic' }}>
                          {u.roles?.includes('ROLE_ADMIN') ? 'Enterprise-Wide (All Sites)' : 'Unassigned / Global'}
                        </span>
                      )}
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
                        <button
                          type="button"
                          onClick={() => {
                            setPaymentTargetUser(u);
                            setPaymentModalOpen(true);
                          }}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                          style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', padding: '3px 8px', fontSize: '11px', width: 'fit-content' }}
                          title="View / Edit staff QR code, UPI ID, and bank account"
                        >
                          <QrCode size={12} color="#2563EB" /> Pay / QR
                        </button>
                        <div style={{ display: 'flex', gap: '3px', flexWrap: 'wrap' }}>
                          <span className="gov-badge gov-badge-info" style={{ fontSize: '9px', padding: '1px 4px' }}>QR Ready</span>
                          {u.upiId ? (
                            <span className="gov-badge gov-badge-success" style={{ fontSize: '9px', padding: '1px 4px' }}>UPI</span>
                          ) : (
                            <span className="gov-badge gov-badge-success" style={{ fontSize: '9px', padding: '1px 4px' }}>Auto-UPI</span>
                          )}
                          {u.bankAccountNumber && <span className="gov-badge gov-badge-primary" style={{ fontSize: '9px', padding: '1px 4px' }}>Bank</span>}
                        </div>
                      </div>
                    </td>
                    <td className="text-center">
                      <StatusBadge status={u.status} />
                    </td>
                    {canManage && (
                      <td className="text-center">
                        <div style={{ display: 'flex', justifyContent: 'center', gap: '6px' }}>
                          {isAdmin() && (
                            <button
                              type="button"
                              onClick={() => {
                                setSelectedUser(u);
                                setTargetSiteId(u.assignedSiteId ? u.assignedSiteId.toString() : '');
                                setDeputeModalOpen(true);
                              }}
                              className="gov-btn gov-btn-secondary gov-btn-sm"
                              title="Depute / assign to a construction site"
                              style={{ padding: '4px 8px', fontSize: '11px' }}
                            >
                              <MapPin size={12} /> Depute Site
                            </button>
                          )}
                          {isAdmin() && (
                            <button
                              type="button"
                              onClick={() => {
                                setSelectedUser(u);
                                setNewPassword('');
                                setResetModalOpen(true);
                              }}
                              className="gov-btn gov-btn-secondary gov-btn-sm"
                              title="Reset password for this user (syncs to Supabase Auth)"
                              style={{ padding: '4px 8px', fontSize: '11px' }}
                            >
                              <KeyRound size={12} /> Reset Password
                            </button>
                          )}
                          {isAccountant() && !isAdmin() && (
                            <button
                              type="button"
                              onClick={() => {
                                setPaymentTargetUser(u);
                                setPaymentModalOpen(true);
                              }}
                              className="gov-btn gov-btn-secondary gov-btn-sm"
                              title="Update staff payment credentials"
                              style={{ padding: '4px 8px', fontSize: '11px' }}
                            >
                              <CreditCard size={12} /> Edit Payment
                            </button>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Modal: Create User / Depute Officer */}
      {createModalOpen && (
        <div className="gov-modal-backdrop" style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.55)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999, padding: '16px'
        }}>
          <div style={{ backgroundColor: '#FFFFFF', width: '100%', maxWidth: '540px', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{ backgroundColor: 'var(--gov-navy)', color: '#FFFFFF', padding: '14px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ fontWeight: 700, fontSize: '15px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <UserCheck size={18} color="#93C5FD" />
                <span>Create User & Depute to Site</span>
              </div>
              <button type="button" onClick={() => setCreateModalOpen(false)} style={{ background: 'none', border: 'none', color: '#CBD5E1', cursor: 'pointer' }}>
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleCreateUser} style={{ padding: '20px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="gov-form-group">
                  <label className="gov-form-label required">Username</label>
                  <input
                    type="text"
                    required
                    className="gov-form-input"
                    value={createForm.username}
                    onChange={(e) => setCreateForm({ ...createForm, username: e.target.value })}
                    placeholder="e.g. supervisor_delhi"
                  />
                </div>
                <div className="gov-form-group">
                  <label className="gov-form-label required">Email Address</label>
                  <input
                    type="email"
                    required
                    className="gov-form-input"
                    value={createForm.email}
                    onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
                    placeholder="user@labourerp.com"
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="gov-form-group">
                  <label className="gov-form-label required">Full Legal Name</label>
                  <input
                    type="text"
                    required
                    className="gov-form-input"
                    value={createForm.fullName}
                    onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })}
                    placeholder="e.g. Ramesh Yadav"
                  />
                </div>
                <div className="gov-form-group">
                  <label className="gov-form-label">Contact Phone</label>
                  <input
                    type="text"
                    className="gov-form-input"
                    value={createForm.phone}
                    onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })}
                    placeholder="+91 98765 43210"
                  />
                </div>
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label required">Initial Security Password</label>
                <input
                  type="password"
                  required
                  className="gov-form-input"
                  value={createForm.password}
                  onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                  placeholder="Set strong initial password (min 6 characters)"
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div className="gov-form-group">
                  <label className="gov-form-label required">Operational System Role</label>
                  <select
                    className="gov-form-input"
                    value={createForm.role}
                    onChange={(e) => setCreateForm({ ...createForm, role: e.target.value })}
                  >
                    <option value="ROLE_SITE_SUPERVISOR">Site Supervisor (Muster & Site)</option>
                    <option value="ROLE_MANAGER">Operations Manager (Multi-site)</option>
                    <option value="ROLE_ACCOUNTANT">Financial Accountant (Payroll & Vouchers)</option>
                    <option value="ROLE_ADMIN">Administrator / Owner (Full CRUD)</option>
                  </select>
                </div>

                <div className="gov-form-group">
                  <label className="gov-form-label">Deputed Construction Site</label>
                  <select
                    className="gov-form-input"
                    value={createForm.assignedSiteId}
                    onChange={(e) => setCreateForm({ ...createForm, assignedSiteId: e.target.value })}
                  >
                    <option value="">None / Enterprise-Wide</option>
                    {sites.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.siteName} ({s.siteCode})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Payment Details Section */}
              <div style={{ backgroundColor: '#F8FAFC', border: '1px solid #CBD5E1', padding: '12px', borderRadius: '4px', marginBottom: '14px' }}>
                <div style={{ fontSize: '12px', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <CreditCard size={14} color="#2563EB" /> Payment & Disbursement Options (Optional)
                </div>
                <div style={{ fontSize: '11px', color: '#64748B', marginBottom: '8px' }}>
                  Provide any one or all: Mobile UPI ID, QR Pic, or Bank details.
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  <div className="gov-form-group">
                    <label className="gov-form-label">Mobile UPI ID</label>
                    <input
                      type="text"
                      className="gov-form-input font-mono"
                      value={createForm.upiId || ''}
                      onChange={(e) => setCreateForm({ ...createForm, upiId: e.target.value })}
                      placeholder="e.g. 9876543210@upi"
                    />
                  </div>
                  <div className="gov-form-group">
                    <label className="gov-form-label">QR Code Picture URL</label>
                    <input
                      type="text"
                      className="gov-form-input"
                      value={createForm.qrCodeUrl || ''}
                      onChange={(e) => setCreateForm({ ...createForm, qrCodeUrl: e.target.value })}
                      placeholder="Image URL or upload later"
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  <div className="gov-form-group" style={{ marginBottom: 0 }}>
                    <label className="gov-form-label">Bank Account Number</label>
                    <input
                      type="text"
                      className="gov-form-input font-mono"
                      value={createForm.bankAccountNumber || ''}
                      onChange={(e) => setCreateForm({ ...createForm, bankAccountNumber: e.target.value })}
                      placeholder="Account number"
                    />
                  </div>
                  <div className="gov-form-group" style={{ marginBottom: 0 }}>
                    <label className="gov-form-label">Bank IFSC Code</label>
                    <input
                      type="text"
                      className="gov-form-input font-mono"
                      value={createForm.bankIfscCode || ''}
                      onChange={(e) => setCreateForm({ ...createForm, bankIfscCode: e.target.value.toUpperCase() })}
                      placeholder="e.g. HDFC0001234"
                    />
                  </div>
                </div>
              </div>

              <div style={{ fontSize: '11px', color: '#4B5563', backgroundColor: '#F1F5F9', padding: '8px 12px', border: '1px solid #CBD5E1', marginBottom: '16px', borderRadius: '2px' }}>
                <ShieldCheck size={13} style={{ display: 'inline', verticalAlign: 'text-bottom', marginRight: '4px' }} />
                User will be auto-synchronized directly to Supabase Authentication (`auth.users`) table so developer can also manage credentials from the Supabase dashboard.
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={() => setCreateModalOpen(false)} className="gov-btn gov-btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={createLoading} className="gov-btn gov-btn-primary">
                  {createLoading ? 'Creating User...' : 'Create & Depute User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Depute Site */}
      {deputeModalOpen && selectedUser && (
        <div className="gov-modal-backdrop" style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.55)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999, padding: '16px'
        }}>
          <div style={{ backgroundColor: '#FFFFFF', width: '100%', maxWidth: '440px', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{ backgroundColor: 'var(--gov-navy)', color: '#FFFFFF', padding: '14px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ fontWeight: 700, fontSize: '15px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <MapPin size={18} color="#93C5FD" />
                <span>Depute Site: {selectedUser.fullName}</span>
              </div>
              <button type="button" onClick={() => setDeputeModalOpen(false)} style={{ background: 'none', border: 'none', color: '#CBD5E1', cursor: 'pointer' }}>
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleDeputeSite} style={{ padding: '20px' }}>
              <div style={{ marginBottom: '12px', fontSize: '13px', color: '#4B5563' }}>
                Assign <strong>{selectedUser.fullName}</strong> ({selectedUser.username}) to a designated construction or electrical project site:
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label required">Select Assigned Site</label>
                <select
                  className="gov-form-input"
                  value={targetSiteId}
                  onChange={(e) => setTargetSiteId(e.target.value)}
                >
                  <option value="">No Site Assigned (Global / Enterprise-Wide)</option>
                  {sites.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.siteName} ({s.siteCode})
                    </option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '16px' }}>
                <button type="button" onClick={() => setDeputeModalOpen(false)} className="gov-btn gov-btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={deputeLoading} className="gov-btn gov-btn-primary">
                  {deputeLoading ? 'Updating Deputation...' : 'Save Site Assignment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Reset Password */}
      {resetModalOpen && selectedUser && (
        <div className="gov-modal-backdrop" style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.55)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999, padding: '16px'
        }}>
          <div style={{ backgroundColor: '#FFFFFF', width: '100%', maxWidth: '440px', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{ backgroundColor: 'var(--gov-navy)', color: '#FFFFFF', padding: '14px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ fontWeight: 700, fontSize: '15px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <KeyRound size={18} color="#93C5FD" />
                <span>Reset Password: {selectedUser.username}</span>
              </div>
              <button type="button" onClick={() => setResetModalOpen(false)} style={{ background: 'none', border: 'none', color: '#CBD5E1', cursor: 'pointer' }}>
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleResetPassword} style={{ padding: '20px' }}>
              <div style={{ marginBottom: '14px', fontSize: '13px', color: '#4B5563' }}>
                Set a new password for <strong>{selectedUser.fullName}</strong> ({selectedUser.email}).
                This will simultaneously update both ERP and Supabase Authentication!
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label required">New Security Password</label>
                <input
                  type="password"
                  required
                  className="gov-form-input"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password (min 6 characters)"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '16px' }}>
                <button type="button" onClick={() => setResetModalOpen(false)} className="gov-btn gov-btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={resetLoading} className="gov-btn gov-btn-primary">
                  {resetLoading ? 'Resetting...' : 'Reset & Sync Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Payment Details & QR Scan Modal for Staff / User */}
      <PaymentDetailsModal
        isOpen={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        title={`Staff Payment Credentials: ${paymentTargetUser?.fullName || paymentTargetUser?.username}`}
        subtitle={`Role: ${paymentTargetUser?.roles?.join(', ').replace(/ROLE_/g, '')} | Email: ${paymentTargetUser?.email}`}
        paymentData={paymentTargetUser || {}}
        isEditable={isAdmin() || isAccountant()}
        onSave={async (details) => {
          await api.put(`/users/${paymentTargetUser.id}`, { ...details });
          fetchUsers();
        }}
      />
    </div>
  );
};
