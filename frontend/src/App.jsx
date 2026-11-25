import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { BusinessProvider } from './context/BusinessContext';

import { GovHeader } from './components/layout/GovHeader';
import { GovNavbar } from './components/layout/GovNavbar';
import { GovFooter } from './components/layout/GovFooter';

import { Login } from './pages/Login/Login';
import { Dashboard } from './pages/Dashboard/Dashboard';
import { Businesses } from './pages/Businesses/Businesses';
import { Sites } from './pages/Sites/Sites';
import { Labour } from './pages/Labour/Labour';
import { Attendance } from './pages/Attendance/Attendance';
import { Payroll } from './pages/Payroll/Payroll';
import { Advances } from './pages/Advances/Advances';
import { Payments } from './pages/Payments/Payments';
import { Transfers } from './pages/Transfers/Transfers';
import { Reports } from './pages/Reports/Reports';
import { Users } from './pages/Users/Users';
import { Audit } from './pages/Audit/Audit';
import { Settings } from './pages/Settings/Settings';

const ProtectedLayout = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'var(--gov-light-grey)' }}>
        <div style={{ textAlign: 'center', color: 'var(--gov-navy)' }}>
          <div style={{ fontWeight: 700, fontSize: '16px' }}>LABOUR MANAGEMENT ERP SYSTEM</div>
          <div style={{ fontSize: '13px', marginTop: '6px' }}>Loading Enterprise Portal Session...</div>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <BusinessProvider>
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <GovHeader />
        <GovNavbar />
        <main style={{ flex: 1 }}>{children}</main>
        <GovFooter />
      </div>
    </BusinessProvider>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProtectedLayout><Navigate to="/dashboard" replace /></ProtectedLayout>} />
          <Route path="/dashboard" element={<ProtectedLayout><Dashboard /></ProtectedLayout>} />
          <Route path="/businesses" element={<ProtectedLayout><Businesses /></ProtectedLayout>} />
          <Route path="/sites" element={<ProtectedLayout><Sites /></ProtectedLayout>} />
          <Route path="/labour" element={<ProtectedLayout><Labour /></ProtectedLayout>} />
          <Route path="/attendance" element={<ProtectedLayout><Attendance /></ProtectedLayout>} />
          <Route path="/payroll" element={<ProtectedLayout><Payroll /></ProtectedLayout>} />
          <Route path="/advances" element={<ProtectedLayout><Advances /></ProtectedLayout>} />
          <Route path="/payments" element={<ProtectedLayout><Payments /></ProtectedLayout>} />
          <Route path="/transfers" element={<ProtectedLayout><Transfers /></ProtectedLayout>} />
          <Route path="/reports" element={<ProtectedLayout><Reports /></ProtectedLayout>} />
          <Route path="/users" element={<ProtectedLayout><Users /></ProtectedLayout>} />
          <Route path="/audit" element={<ProtectedLayout><Audit /></ProtectedLayout>} />
          <Route path="/settings" element={<ProtectedLayout><Settings /></ProtectedLayout>} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
