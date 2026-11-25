import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ShieldCheck, Lock, User, AlertCircle } from 'lucide-react';

export const Login = () => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Admin@123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(
        err.response?.data?.message || 'Invalid administrative credentials. Please verify your username and password.'
      );
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = (u, p) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: 'var(--gov-light-grey)', display: 'flex', flexDirection: 'column' }}>
      {/* Top Banner */}
      <div className="gov-top-strip">
        <div className="gov-india-text">LABOUR MANAGEMENT ERP — ENTERPRISE CONTRACTOR WORKFORCE SUITE</div>
        <div className="gov-meta-items">
          <span>Enterprise Portal Security: <strong>AES-256 TLS</strong></span>
        </div>
      </div>

      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '24px' }}>
        <div style={{ width: '100%', maxWidth: '440px', backgroundColor: '#FFFFFF', border: '2px solid var(--gov-navy)', borderRadius: '2px', boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
          {/* Header */}
          <div style={{ backgroundColor: 'var(--gov-navy)', color: '#FFFFFF', padding: '18px 20px', textAlign: 'center', borderBottom: '3px solid #245A85' }}>
            <div style={{ width: '48px', height: '48px', backgroundColor: '#245A85', color: '#FFFFFF', margin: '0 auto 10px auto', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold', fontSize: '18px', border: '1px solid #3B82F6', borderRadius: '4px', letterSpacing: '1px' }}>
              ERP
            </div>
            <h2 style={{ fontSize: '16px', fontWeight: 700, letterSpacing: '0.5px', textTransform: 'uppercase', margin: 0 }}>
              Labour Management ERP
            </h2>
            <p style={{ fontSize: '12px', color: '#CBD5E1', margin: '4px 0 0 0' }}>
              Contractor & Multi-Business Workforce Portal
            </p>
          </div>

          <div style={{ padding: '20px' }}>
            {error && (
              <div className="gov-alert gov-alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <AlertCircle size={16} /> {error}
              </div>
            )}

            <form onSubmit={handleSubmit}>
              <div className="gov-form-group">
                <label className="gov-form-label required">Username or Email Address</label>
                <div style={{ position: 'relative' }}>
                  <input
                    type="text"
                    required
                    className="gov-form-input"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="Enter username or email"
                    autoFocus
                  />
                </div>
              </div>

              <div className="gov-form-group">
                <label className="gov-form-label required">Account Password</label>
                <input
                  type="password"
                  required
                  className="gov-form-input"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter account password"
                />
              </div>

              <div style={{ fontSize: '11px', color: 'var(--gov-secondary-text)', backgroundColor: 'var(--gov-light-blue)', padding: '8px 10px', border: '1px solid var(--gov-border)', marginBottom: '16px' }}>
                <ShieldCheck size={13} style={{ display: 'inline', verticalAlign: 'text-bottom', marginRight: '4px' }} />
                Access restricted strictly to authorized contractor staff, site supervisors, and accountants.
              </div>

              <button
                type="submit"
                disabled={loading}
                className="gov-btn gov-btn-primary"
                style={{ width: '100%', justifyContent: 'center', padding: '10px' }}
              >
                {loading ? 'Verifying Credentials...' : 'Sign In to Labour ERP'}
              </button>
            </form>

            {/* Quick-Fill Role Selector for Testing & Evaluation */}
            <div style={{ marginTop: '20px', paddingTop: '14px', borderTop: '1px solid var(--gov-border)' }}>
              <div style={{ fontSize: '11px', fontWeight: 700, color: 'var(--gov-navy)', textTransform: 'uppercase', marginBottom: '6px' }}>
                Quick Test Credentials (Pre-Configured System Roles):
              </div>
              <div className="gov-quick-credentials-grid">
                <button
                  type="button"
                  onClick={() => handleQuickFill('admin', 'Admin@123')}
                  className="gov-btn gov-btn-secondary gov-btn-sm"
                  style={{ justifyContent: 'center' }}
                >
                  Admin / Owner
                </button>
                <button
                  type="button"
                  onClick={() => handleQuickFill('manager', 'Manager@123')}
                  className="gov-btn gov-btn-secondary gov-btn-sm"
                  style={{ justifyContent: 'center' }}
                >
                  Operations Manager
                </button>
                <button
                  type="button"
                  onClick={() => handleQuickFill('supervisor', 'Supervisor@123')}
                  className="gov-btn gov-btn-secondary gov-btn-sm"
                  style={{ justifyContent: 'center' }}
                >
                  Site Supervisor
                </button>
                <button
                  type="button"
                  onClick={() => handleQuickFill('accountant', 'Accountant@123')}
                  className="gov-btn gov-btn-secondary gov-btn-sm"
                  style={{ justifyContent: 'center' }}
                >
                  Accountant
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <footer className="gov-footer" style={{ padding: '10px 20px' }}>
        <p>Labour Management ERP System © 2026. Confidential and proprietary enterprise platform. All rights reserved.</p>
      </footer>
    </div>
  );
};
