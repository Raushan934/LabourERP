import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';

const BusinessContext = createContext();

export const BusinessProvider = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [businesses, setBusinesses] = useState([]);
  const [activeBusinessId, setActiveBusinessId] = useState(() => {
    const saved = localStorage.getItem('erp_active_business_id');
    return saved ? parseInt(saved, 10) : 1;
  });
  const [modules, setModules] = useState([]);
  const [settings, setSettings] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchBusinesses = async () => {
    if (!isAuthenticated) return;
    try {
      const res = await api.get('/businesses');
      if (res.data && res.data.data) {
        setBusinesses(res.data.data);
        if (!activeBusinessId && res.data.data.length > 0) {
          switchBusiness(res.data.data[0].id);
        }
      }
    } catch (err) {
      console.error('Failed to load businesses', err);
    }
  };

  const fetchBusinessModulesAndSettings = async (bizId) => {
    if (!bizId || !isAuthenticated) return;
    try {
      const [modRes, setRes] = await Promise.all([
        api.get(`/businesses/${bizId}/modules`),
        api.get(`/businesses/${bizId}/settings`)
      ]);
      if (modRes.data && modRes.data.data) setModules(modRes.data.data);
      if (setRes.data && setRes.data.data) setSettings(setRes.data.data);
    } catch (err) {
      console.error('Failed to load modules/settings', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchBusinesses();
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isAuthenticated && activeBusinessId) {
      fetchBusinessModulesAndSettings(activeBusinessId);
    }
  }, [isAuthenticated, activeBusinessId]);

  const switchBusiness = (id) => {
    const numId = parseInt(id, 10);
    setActiveBusinessId(numId);
    localStorage.setItem('erp_active_business_id', numId.toString());
  };

  const isModuleEnabled = (moduleCode) => {
    if (!modules || modules.length === 0) return true;
    const mod = modules.find((m) => m.moduleCode === moduleCode);
    return mod ? mod.enabled : true;
  };

  const activeBusiness = businesses.find((b) => b.id === activeBusinessId) || null;

  return (
    <BusinessContext.Provider
      value={{
        businesses,
        activeBusinessId,
        activeBusiness,
        switchBusiness,
        modules,
        settings,
        isModuleEnabled,
        fetchBusinesses,
        loading
      }}
    >
      {children}
    </BusinessContext.Provider>
  );
};

export const useBusiness = () => useContext(BusinessContext);
