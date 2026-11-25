import React, { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useBusiness } from '../../context/BusinessContext';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Building2,
  MapPin,
  Users,
  CalendarCheck,
  Calculator,
  HandCoins,
  CreditCard,
  ArrowLeftRight,
  FileBarChart2,
  UserCog,
  History,
  Sliders,
  Menu,
  X
} from 'lucide-react';

export const GovNavbar = () => {
  const { isModuleEnabled } = useBusiness();
  const { user } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  const userRoles = user?.roles || [];
  const hasAnyRole = (allowedRoles) => {
    if (!allowedRoles || allowedRoles.length === 0) return true;
    return userRoles.some((r) => allowedRoles.includes(r));
  };

  const navItems = [
    { to: '/dashboard', label: '1. Dashboard', icon: LayoutDashboard, always: true },
    { to: '/businesses', label: '2. Businesses', icon: Building2, always: true, roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] },
    { to: '/sites', label: '3. Sites', icon: MapPin, moduleCode: 'SITES', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SITE_SUPERVISOR'] },
    { to: '/labour', label: '4. Labour Workforce', icon: Users, moduleCode: 'LABOUR', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SITE_SUPERVISOR', 'ROLE_ACCOUNTANT'] },
    { to: '/attendance', label: '5. Muster & Attendance', icon: CalendarCheck, moduleCode: 'ATTENDANCE', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SITE_SUPERVISOR', 'ROLE_ACCOUNTANT'] },
    { to: '/payroll', label: '6. Wage & Payroll', icon: Calculator, moduleCode: 'PAYROLL', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT'] },
    { to: '/advances', label: '7. Advances & Loans', icon: HandCoins, moduleCode: 'ADVANCES', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT'] },
    { to: '/payments', label: '8. Payment Vouchers', icon: CreditCard, moduleCode: 'PAYMENTS', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT'] },
    { to: '/transfers', label: '9. Site Transfers', icon: ArrowLeftRight, moduleCode: 'TRANSFERS', roles: ['ROLE_ADMIN', 'ROLE_MANAGER'] },
    { to: '/reports', label: '10. Reports & Ledgers', icon: FileBarChart2, moduleCode: 'REPORTS', roles: ['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT'] },
    { to: '/users', label: '11. Staff & Role Payments', icon: UserCog, always: true, roles: ['ROLE_ADMIN', 'ROLE_ACCOUNTANT'] },
    { to: '/audit', label: '12. Audit Trail', icon: History, moduleCode: 'AUDIT_LOG', roles: ['ROLE_ADMIN'] },
    { to: '/settings', label: '13. Master Settings', icon: Sliders, always: true, roles: ['ROLE_ADMIN'] },
  ];

  // Filter items by active module and user role
  const visibleNavItems = navItems.filter((item) => {
    if (item.roles && !hasAnyRole(item.roles)) {
      return false;
    }
    if (!item.always && !isModuleEnabled(item.moduleCode)) {
      return false;
    }
    return true;
  });

  const activeItem = visibleNavItems.find((item) => location.pathname.startsWith(item.to)) || visibleNavItems[0] || navItems[0];
  const ActiveIcon = activeItem.icon;

  return (
    <nav className="gov-navbar">
      {/* Mobile Bar visible on screens <= 992px */}
      <div className="gov-nav-mobile-bar">
        <div className="gov-nav-mobile-current">
          <ActiveIcon size={16} />
          <span>{activeItem.label}</span>
        </div>
        <button
          type="button"
          className="gov-nav-mobile-toggle"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle navigation menu"
        >
          {mobileOpen ? <X size={18} /> : <Menu size={18} />}
          <span>{mobileOpen ? 'Close Menu' : `Modules (${visibleNavItems.length})`}</span>
        </button>
      </div>

      {/* Nav Items Container */}
      <div className={`gov-nav-items-container ${mobileOpen ? 'mobile-open' : ''}`}>
        {visibleNavItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                `gov-nav-item ${isActive ? 'active' : ''}`
              }
            >
              <Icon size={16} />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </div>
    </nav>
  );
};
