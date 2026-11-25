import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useBusiness } from '../../context/BusinessContext';
import { LogOut, Building, User, Calendar, ShieldCheck, KeyRound } from 'lucide-react';
import { ProfileModal } from '../common/ProfileModal';

export const GovHeader = () => {
  const { user, logout } = useAuth();
  const { businesses, activeBusinessId, switchBusiness, activeBusiness } = useBusiness();
  const [currentDateTime, setCurrentDateTime] = useState('');
  const [profileModalOpen, setProfileModalOpen] = useState(false);

  useEffect(() => {
    const updateDateTime = () => {
      const now = new Date();
      const options = {
        weekday: 'short',
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: true
      };
      setCurrentDateTime(now.toLocaleString('en-IN', options));
    };
    updateDateTime();
    const timer = setInterval(updateDateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <header>
      {/* Top Strip */}
      <div className="gov-top-strip">
        <div className="gov-india-text">
          LABOUR MANAGEMENT ERP — ENTERPRISE CONTRACTOR WORKFORCE SUITE
        </div>
        <div className="gov-meta-items">
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Calendar size={12} /> {currentDateTime}
          </span>
          <span>Language: <strong>English (IN)</strong></span>
          <span>Security: <strong>Authenticated TLS</strong></span>
        </div>
      </div>

      {/* Main Header */}
      <div className="gov-main-header">
        <div className="gov-header-left">
          <div className="gov-emblem-badge" style={{ backgroundColor: '#245A85', color: '#FFFFFF', fontWeight: 800, fontSize: '16px', letterSpacing: '1px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            ERP
          </div>
          <div className="gov-title-block">
            <h1>Labour Management ERP System</h1>
            <p>
              {activeBusiness ? activeBusiness.businessName : 'Enterprise Workforce & Multi-Business Operations Portal'}
            </p>
          </div>
        </div>

        <div className="gov-header-right">
          {/* Active Business Switcher */}
          {businesses && businesses.length > 0 && (
            <div className="gov-business-selector">
              <Building size={14} color="#FBBF24" />
              <label htmlFor="business-select">Organization / Business:</label>
              <select
                id="business-select"
                value={activeBusinessId || ''}
                onChange={(e) => switchBusiness(e.target.value)}
              >
                {businesses.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.businessName} ({b.businessType})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* User info & Logout */}
          <div className="gov-user-panel">
            <div className="gov-user-info">
              <div className="gov-user-name" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <User size={13} /> {user?.fullName || user?.username || 'Staff'}
              </div>
              <div className="gov-user-role">
                {user?.roles?.join(', ').replace(/ROLE_/g, '') || 'ADMINISTRATOR'}
              </div>
            </div>
            <button
              onClick={() => setProfileModalOpen(true)}
              className="gov-btn gov-btn-secondary gov-btn-sm"
              title="Change your password and manage credentials"
              style={{ padding: '4px 8px', fontSize: '11px' }}
            >
              <KeyRound size={12} /> Security & Password
            </button>
            <button
              onClick={logout}
              className="gov-btn gov-btn-danger gov-btn-sm"
              title="End session and log out safely"
            >
              <LogOut size={13} /> Logout
            </button>
          </div>
        </div>
      </div>

      <ProfileModal
        isOpen={profileModalOpen}
        onClose={() => setProfileModalOpen(false)}
      />
    </header>
  );
};
