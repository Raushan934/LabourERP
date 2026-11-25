import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { Lock, User, ShieldCheck, CheckCircle2, AlertCircle, X, KeyRound } from 'lucide-react';

export const ProfileModal = ({ isOpen, onClose }) => {
  const { user, updateUser } = useAuth();
  const [activeTab, setActiveTab] = useState('password'); // 'password' | 'profile'

  // Change Password state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [passwordError, setPasswordError] = useState('');

  // Profile state
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileError, setProfileError] = useState('');

  if (!isOpen) return null;

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');

    if (newPassword.length < 6) {
      setPasswordError('New password must be at least 6 characters.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError('New passwords do not match.');
      return;
    }

    setPasswordLoading(true);
    try {
      await api.post('/users/change-password', { currentPassword, newPassword });
      setPasswordSuccess('Password updated and synchronized with Supabase Authentication successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setPasswordError(err.response?.data?.message || 'Failed to update password. Please check your current password.');
    } finally {
      setPasswordLoading(false);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setProfileError('');
    setProfileSuccess('');

    setProfileLoading(true);
    try {
      const res = await api.put('/users/profile', { fullName, phone });
      if (res.data?.data) {
        updateUser(res.data.data);
      }
      setProfileSuccess('Profile updated successfully!');
    } catch (err) {
      setProfileError(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setProfileLoading(false);
    }
  };

  return (
    <div className="gov-modal-backdrop" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.55)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 9999,
      padding: '16px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        width: '100%',
        maxWidth: '520px',
        borderRadius: '4px',
        boxShadow: '0 8px 24px rgba(0, 0, 0, 0.25)',
        overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{
          backgroundColor: 'var(--gov-navy)',
          color: '#FFFFFF',
          padding: '14px 20px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 700, fontSize: '15px' }}>
            <KeyRound size={18} color="#93C5FD" />
            <span>Account Security & Login Credentials</span>
          </div>
          <button
            type="button"
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: '#CBD5E1', cursor: 'pointer', padding: '4px' }}
          >
            <X size={18} />
          </button>
        </div>

        {/* User Card */}
        <div style={{ padding: '14px 20px', backgroundColor: '#F8FAFC', borderBottom: '1px solid var(--gov-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ fontWeight: 700, fontSize: '14px', color: 'var(--gov-navy)' }}>
              {user?.fullName || user?.username}
            </div>
            <div style={{ fontSize: '12px', color: 'var(--gov-secondary-text)' }}>
              Username: <strong>{user?.username}</strong> &bull; {user?.email}
            </div>
            {user?.assignedSiteName && (
              <div style={{ fontSize: '11px', color: '#1E40AF', marginTop: '2px' }}>
                Deputed Site: <strong>{user.assignedSiteName} ({user.assignedSiteCode})</strong>
              </div>
            )}
          </div>
          <div className="gov-badge gov-badge-info" style={{ fontSize: '11px', textTransform: 'uppercase' }}>
            {user?.roles?.join(', ').replace(/ROLE_/g, '')}
          </div>
        </div>

        {/* Tab Switcher */}
        <div style={{ display: 'flex', borderBottom: '1px solid var(--gov-border)', backgroundColor: '#FFFFFF' }}>
          <button
            type="button"
            onClick={() => setActiveTab('password')}
            style={{
              flex: 1,
              padding: '10px 16px',
              border: 'none',
              background: 'none',
              borderBottom: activeTab === 'password' ? '2px solid var(--gov-navy)' : 'none',
              fontWeight: activeTab === 'password' ? 700 : 500,
              color: activeTab === 'password' ? 'var(--gov-navy)' : 'var(--gov-secondary-text)',
              cursor: 'pointer'
            }}
          >
            Change Login Password
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('profile')}
            style={{
              flex: 1,
              padding: '10px 16px',
              border: 'none',
              background: 'none',
              borderBottom: activeTab === 'profile' ? '2px solid var(--gov-navy)' : 'none',
              fontWeight: activeTab === 'profile' ? 700 : 500,
              color: activeTab === 'profile' ? 'var(--gov-navy)' : 'var(--gov-secondary-text)',
              cursor: 'pointer'
            }}
          >
            My Profile Information
          </button>
        </div>

        {/* Tab Body */}
        <div style={{ padding: '20px' }}>
          {activeTab === 'password' && (
            <form onSubmit={handleChangePassword}>
              {passwordSuccess && (
                <div className="gov-alert gov-alert-success" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '14px' }}>
                  <CheckCircle2 size={16} /> {passwordSuccess}
                </div>
              )}
              {passwordError && (
                <div className="gov-alert gov-alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '14px' }}>
                  <AlertCircle size={16} /> {passwordError}
                </div>
              )}

              <div className="gov-form-group">
                <label className="gov-form-label required">Current Password</label>
                <input
                  type="password"
                  required
                  className="gov-form-input"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="Enter current password"
                />
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label required">New Password</label>
                <input
                  type="password"
                  required
                  className="gov-form-input"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Minimum 6 characters"
                />
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label required">Confirm New Password</label>
                <input
                  type="password"
                  required
                  className="gov-form-input"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Re-enter new password"
                />
              </div>

              <div style={{ fontSize: '11px', color: '#4B5563', backgroundColor: '#F1F5F9', padding: '8px 12px', border: '1px solid #CBD5E1', marginBottom: '16px', borderRadius: '2px' }}>
                <ShieldCheck size={13} style={{ display: 'inline', verticalAlign: 'text-bottom', marginRight: '4px' }} />
                Your new password updates both the ERP database and Supabase Auth credentials simultaneously.
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button type="button" onClick={onClose} className="gov-btn gov-btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={passwordLoading} className="gov-btn gov-btn-primary">
                  {passwordLoading ? 'Updating Password...' : 'Save New Password'}
                </button>
              </div>
            </form>
          )}

          {activeTab === 'profile' && (
            <form onSubmit={handleUpdateProfile}>
              {profileSuccess && (
                <div className="gov-alert gov-alert-success" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '14px' }}>
                  <CheckCircle2 size={16} /> {profileSuccess}
                </div>
              )}
              {profileError && (
                <div className="gov-alert gov-alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '14px' }}>
                  <AlertCircle size={16} /> {profileError}
                </div>
              )}

              <div className="gov-form-group">
                <label className="gov-form-label required">Full Legal Name</label>
                <input
                  type="text"
                  required
                  className="gov-form-input"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                />
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label">Phone Number</label>
                <input
                  type="text"
                  className="gov-form-input"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="+91 XXXXX XXXXX"
                />
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label">Registered Email (Read-Only)</label>
                <input
                  type="text"
                  disabled
                  className="gov-form-input"
                  value={user?.email || ''}
                  style={{ backgroundColor: '#F1F5F9' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '16px' }}>
                <button type="button" onClick={onClose} className="gov-btn gov-btn-secondary">
                  Cancel
                </button>
                <button type="submit" disabled={profileLoading} className="gov-btn gov-btn-primary">
                  {profileLoading ? 'Saving...' : 'Update Profile'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
