import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(() => localStorage.getItem('erp_token'));
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('erp_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.get('/auth/me')
        .then((res) => {
          if (res.data && res.data.data) {
            const data = res.data.data;
            setUser((prev) => {
              const updated = { ...prev, ...data };
              localStorage.setItem('erp_user', JSON.stringify(updated));
              return updated;
            });
          }
        })
        .catch(() => {
          logout();
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    if (response.data && response.data.data) {
      const authData = response.data.data;
      setToken(authData.token);
      localStorage.setItem('erp_token', authData.token);

      const userInfo = {
        id: authData.userId,
        username: authData.username,
        fullName: authData.fullName,
        email: authData.email,
        roles: authData.roles,
        accessibleBusinesses: authData.accessibleBusinesses,
        primaryBusinessId: authData.primaryBusinessId,
        assignedSiteId: authData.assignedSiteId,
        assignedSiteName: authData.assignedSiteName,
        assignedSiteCode: authData.assignedSiteCode
      };
      setUser(userInfo);
      localStorage.setItem('erp_user', JSON.stringify(userInfo));

      if (authData.primaryBusinessId) {
        localStorage.setItem('erp_active_business_id', authData.primaryBusinessId.toString());
      }
      return authData;
    }
    throw new Error('Authentication failed');
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('erp_token');
    localStorage.removeItem('erp_user');
    localStorage.removeItem('erp_active_business_id');
  };

  const hasRole = (role) => {
    return user && user.roles && user.roles.includes(role);
  };

  const isAdmin = () => hasRole('ROLE_ADMIN');
  const isManager = () => hasRole('ROLE_MANAGER');
  const isSupervisor = () => hasRole('ROLE_SITE_SUPERVISOR');
  const isAccountant = () => hasRole('ROLE_ACCOUNTANT');

  const updateUser = (updated) => {
    setUser((prev) => {
      const merged = { ...prev, ...updated };
      localStorage.setItem('erp_user', JSON.stringify(merged));
      return merged;
    });
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        login,
        logout,
        isAuthenticated: !!token,
        hasRole,
        isAdmin,
        isManager,
        isSupervisor,
        isAccountant,
        updateUser,
        loading
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
