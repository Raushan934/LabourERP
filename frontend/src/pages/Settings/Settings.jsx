import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import { Modal } from '../../components/common/Modal';
import api from '../../services/api';
import {
  Sliders,
  Plus,
  Edit3,
  CheckCircle2,
  XCircle,
  Database,
  Layers,
  Settings as CogIcon
} from 'lucide-react';

export const Settings = () => {
  const { activeBusinessId, settings } = useBusiness();
  const [activeSection, setActiveSection] = useState('masters'); // 'masters', 'custom_fields', 'settings'
  const [selectedCategory, setSelectedCategory] = useState('LABOUR_CATEGORY');
  const [masterValues, setMasterValues] = useState([]);

  // Modals
  const [isMasterModalOpen, setIsMasterModalOpen] = useState(false);
  const [selectedMaster, setSelectedMaster] = useState(null);
  const [masterFormData, setMasterFormData] = useState({
    code: '',
    name: '',
    description: '',
    colorCode: '#1B3557',
    isPayable: true,
    wagePercentage: '100.00',
    sortOrder: 0,
    isActive: true
  });

  // Custom Fields Builder State
  const [customFields, setCustomFields] = useState([]);
  const [targetEntity, setTargetEntity] = useState('LABOURER');
  const [isCustomFieldModalOpen, setIsCustomFieldModalOpen] = useState(false);
  const [customFieldForm, setCustomFieldForm] = useState({
    fieldKey: '',
    fieldLabel: '',
    fieldType: 'TEXT',
    isRequired: false,
    defaultValue: '',
    placeholder: '',
    options: '',
    displayOrder: 0,
    isActive: true
  });

  // Business Settings State
  const [bizSettings, setBizSettings] = useState([]);
  const [savingSettingKey, setSavingSettingKey] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 1. Load Master Values for selected category
  const fetchMasterValues = async () => {
    if (!activeBusinessId || !selectedCategory) return;
    setLoading(true);
    try {
      const res = await api.get(`/master-data/values?category=${selectedCategory}&businessId=${activeBusinessId}&activeOnly=false`);
      if (res.data?.data) {
        setMasterValues(res.data.data);
      }
    } catch (err) {
      console.error('Failed to load master values', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeSection === 'masters') {
      fetchMasterValues();
    } else if (activeSection === 'custom_fields') {
      fetchCustomFields();
    } else if (activeSection === 'settings') {
      fetchBusinessSettings();
    }
  }, [activeSection, selectedCategory, targetEntity, activeBusinessId]);

  // Master Actions
  const openAddMaster = () => {
    setSelectedMaster(null);
    setMasterFormData({
      code: '',
      name: '',
      description: '',
      colorCode: '#1B3557',
      isPayable: true,
      wagePercentage: '100.00',
      sortOrder: masterValues.length + 1,
      isActive: true
    });
    setError('');
    setIsMasterModalOpen(true);
  };

  const openEditMaster = (mv) => {
    setSelectedMaster(mv);
    setMasterFormData({
      ...mv,
      wagePercentage: mv.wagePercentage?.toString() || '100.00'
    });
    setError('');
    setIsMasterModalOpen(true);
  };

  const handleToggleMasterActive = async (mv) => {
    try {
      await api.patch(`/master-data/values/${mv.id}/toggle-active`, { active: !mv.active });
      fetchMasterValues();
    } catch (err) {
      console.error('Failed to toggle status', err);
    }
  };

  const handleMasterSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const payload = {
        ...masterFormData,
        categoryCode: selectedCategory,
        businessId: activeBusinessId,
        wagePercentage: parseFloat(masterFormData.wagePercentage || 100)
      };

      if (selectedMaster) {
        await api.put(`/master-data/values/${selectedMaster.id}`, payload);
      } else {
        await api.post('/master-data/values', payload);
      }
      setIsMasterModalOpen(false);
      fetchMasterValues();
    } catch (err) {
      setError(err.response?.data?.message || 'Error saving master data');
    } finally {
      setLoading(false);
    }
  };

  // 2. Custom Fields Builder Actions
  const fetchCustomFields = async () => {
    if (!activeBusinessId) return;
    try {
      const res = await api.get(`/custom-fields/definitions?businessId=${activeBusinessId}&entityType=${targetEntity}&activeOnly=false`);
      if (res.data?.data) {
        setCustomFields(res.data.data);
      }
    } catch (err) {
      console.error('Failed to load custom fields', err);
    }
  };

  const handleCustomFieldSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const opts = customFieldForm.options
        ? customFieldForm.options.split(',').map((s) => s.trim()).filter(Boolean)
        : [];

      const payload = {
        businessId: activeBusinessId,
        entityType: targetEntity,
        fieldKey: customFieldForm.fieldKey,
        fieldLabel: customFieldForm.fieldLabel,
        fieldType: customFieldForm.fieldType,
        isRequired: customFieldForm.isRequired,
        defaultValue: customFieldForm.defaultValue,
        placeholder: customFieldForm.placeholder,
        options: opts,
        displayOrder: parseInt(customFieldForm.displayOrder || 0, 10),
        isActive: customFieldForm.isActive
      };

      await api.post('/custom-fields/definitions', payload);
      setIsCustomFieldModalOpen(false);
      fetchCustomFields();
    } catch (err) {
      setError(err.response?.data?.message || 'Error creating custom field');
    } finally {
      setLoading(false);
    }
  };

  // 3. Business Settings Actions
  const fetchBusinessSettings = async () => {
    if (!activeBusinessId) return;
    try {
      const res = await api.get(`/businesses/${activeBusinessId}/settings`);
      if (res.data?.data) {
        setBizSettings(res.data.data);
      }
    } catch (err) {
      console.error('Failed to load settings', err);
    }
  };

  const handleUpdateSetting = async (settingKey, value) => {
    setSavingSettingKey(settingKey);
    try {
      await api.put(`/businesses/${activeBusinessId}/settings/${settingKey}`, { settingValue: value });
      fetchBusinessSettings();
    } catch (err) {
      console.error('Failed to update setting', err);
    } finally {
      setSavingSettingKey(null);
    }
  };

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'System Configuration & Masters' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Administrative Settings & Master Data Management</h2>
          <p>Configure operational master registries, custom field schemas, and enterprise business parameters without code changes</p>
        </div>
      </div>

      {/* Main Section Navigation */}
      <div style={{ display: 'flex', borderBottom: '2px solid var(--gov-navy)', marginBottom: '16px' }}>
        <button
          onClick={() => setActiveSection('masters')}
          style={{
            padding: '8px 16px',
            fontSize: '13px',
            fontWeight: 700,
            cursor: 'pointer',
            border: 'none',
            backgroundColor: activeSection === 'masters' ? 'var(--gov-navy)' : '#E2E8F0',
            color: activeSection === 'masters' ? '#FFFFFF' : 'var(--gov-text)',
            borderRadius: '2px 2px 0 0'
          }}
        >
          Master Data Management (Operational Masters)
        </button>
        <button
          onClick={() => setActiveSection('custom_fields')}
          style={{
            padding: '8px 16px',
            fontSize: '13px',
            fontWeight: 700,
            cursor: 'pointer',
            border: 'none',
            backgroundColor: activeSection === 'custom_fields' ? 'var(--gov-navy)' : '#E2E8F0',
            color: activeSection === 'custom_fields' ? '#FFFFFF' : 'var(--gov-text)',
            borderRadius: '2px 2px 0 0',
            marginLeft: '4px'
          }}
        >
          Custom Fields Builder (Entity Schema Extensions)
        </button>
        <button
          onClick={() => setActiveSection('settings')}
          style={{
            padding: '8px 16px',
            fontSize: '13px',
            fontWeight: 700,
            cursor: 'pointer',
            border: 'none',
            backgroundColor: activeSection === 'settings' ? 'var(--gov-navy)' : '#E2E8F0',
            color: activeSection === 'settings' ? '#FFFFFF' : 'var(--gov-text)',
            borderRadius: '2px 2px 0 0',
            marginLeft: '4px'
          }}
        >
          Business Policy & Calculation Parameters
        </button>
      </div>

      {/* SECTION 1: MASTER DATA MANAGEMENT */}
      {activeSection === 'masters' && (
        <div>
          <div style={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: '16px' }}>
            {/* Category Selector Sidebar */}
            <div className="gov-card">
              <div className="gov-card-header">
                <span>Master Categories</span>
              </div>
              <div className="gov-card-body" style={{ padding: 0 }}>
                {[
                  { code: 'LABOUR_CATEGORY', label: 'Labour Categories' },
                  { code: 'DESIGNATION', label: 'Designations' },
                  { code: 'ATTENDANCE_STATUS', label: 'Attendance Statuses' },
                  { code: 'PAYMENT_METHOD', label: 'Payment Channels' },
                  { code: 'ADVANCE_TYPE', label: 'Advance Categories' },
                  { code: 'SITE_CATEGORY', label: 'Site Classifications' },
                  { code: 'BUSINESS_TYPE', label: 'Business Types' }
                ].map((cat) => (
                  <div
                    key={cat.code}
                    onClick={() => setSelectedCategory(cat.code)}
                    style={{
                      padding: '10px 14px',
                      cursor: 'pointer',
                      fontSize: '13px',
                      fontWeight: selectedCategory === cat.code ? 700 : 500,
                      backgroundColor: selectedCategory === cat.code ? 'var(--gov-light-blue)' : '#FFFFFF',
                      borderLeft: selectedCategory === cat.code ? '4px solid var(--gov-navy)' : '4px solid transparent',
                      borderBottom: '1px solid var(--gov-border)'
                    }}
                  >
                    {cat.label}
                  </div>
                ))}
              </div>
            </div>

            {/* Master Values Table */}
            <div className="gov-card">
              <div className="gov-card-header">
                <span>Configure Options for: <strong>{selectedCategory}</strong></span>
                <button onClick={openAddMaster} className="gov-btn gov-btn-primary gov-btn-sm">
                  <Plus size={12} /> Add Option
                </button>
              </div>
              <div className="gov-card-body" style={{ padding: 0 }}>
                <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
                  <table className="gov-table">
                    <thead>
                      <tr>
                        <th>Sort</th>
                        <th>Unique Code</th>
                        <th>Display Name</th>
                        <th>Description</th>
                        {selectedCategory === 'ATTENDANCE_STATUS' && (
                          <th className="text-right">Wage Multiplier (%)</th>
                        )}
                        <th className="text-center">Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {masterValues.length > 0 ? (
                        masterValues.map((mv) => (
                          <tr key={mv.id}>
                            <td className="text-center font-mono">{mv.sortOrder}</td>
                            <td className="font-mono" style={{ fontWeight: 700 }}>{mv.code}</td>
                            <td>
                              <strong>{mv.name}</strong>
                            </td>
                            <td>{mv.description || '--'}</td>
                            {selectedCategory === 'ATTENDANCE_STATUS' && (
                              <td className="text-right font-mono" style={{ fontWeight: 700 }}>
                                {mv.wagePercentage}%
                              </td>
                            )}
                            <td className="text-center">
                              <span className={`gov-badge ${mv.active ? 'active' : 'inactive'}`}>
                                {mv.active ? 'ACTIVE' : 'DEACTIVATED'}
                              </span>
                            </td>
                            <td>
                              <div style={{ display: 'flex', gap: '4px' }}>
                                <button
                                  onClick={() => openEditMaster(mv)}
                                  className="gov-btn gov-btn-secondary gov-btn-sm"
                                  title="Edit master value"
                                >
                                  <Edit3 size={11} /> Edit
                                </button>
                                <button
                                  onClick={() => handleToggleMasterActive(mv)}
                                  className={`gov-btn gov-btn-sm ${mv.active ? 'gov-btn-danger' : 'gov-btn-success'}`}
                                  title={mv.active ? 'Deactivate' : 'Activate'}
                                >
                                  {mv.active ? 'Deactivate' : 'Activate'}
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan="7" className="text-center" style={{ padding: '20px' }}>
                            {loading ? 'Retrieving master values...' : 'No entries registered for this master category.'}
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SECTION 2: CUSTOM FIELDS BUILDER */}
      {activeSection === 'custom_fields' && (
        <div>
          <div className="gov-card">
            <div className="gov-card-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span>Select Target Entity:</span>
                <select
                  className="gov-form-select"
                  style={{ width: '180px', padding: '3px 8px' }}
                  value={targetEntity}
                  onChange={(e) => setTargetEntity(e.target.value)}
                >
                  <option value="LABOURER">Labourer Workforce</option>
                  <option value="SITE">Construction Site</option>
                  <option value="ATTENDANCE">Attendance Record</option>
                  <option value="PAYMENT">Payment Disbursement</option>
                  <option value="ADVANCE">Advance Voucher</option>
                </select>
              </div>
              <button
                onClick={() => {
                  setCustomFieldForm({
                    fieldKey: '',
                    fieldLabel: '',
                    fieldType: 'TEXT',
                    isRequired: false,
                    defaultValue: '',
                    placeholder: '',
                    options: '',
                    displayOrder: customFields.length + 1,
                    isActive: true
                  });
                  setError('');
                  setIsCustomFieldModalOpen(true);
                }}
                className="gov-btn gov-btn-primary gov-btn-sm"
              >
                <Plus size={12} /> Add Custom Field
              </button>
            </div>
            <div className="gov-card-body" style={{ padding: 0 }}>
              <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
                <table className="gov-table">
                  <thead>
                    <tr>
                      <th>Display Order</th>
                      <th>Internal Key</th>
                      <th>Field Label</th>
                      <th>Data Type</th>
                      <th>Required?</th>
                      <th>Options (For Select)</th>
                      <th className="text-center">Active Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customFields.length > 0 ? (
                      customFields.map((cf) => (
                        <tr key={cf.id}>
                          <td className="text-center font-mono">{cf.displayOrder}</td>
                          <td className="font-mono" style={{ fontWeight: 700 }}>{cf.fieldKey}</td>
                          <td><strong>{cf.fieldLabel}</strong></td>
                          <td><span className="gov-badge draft">{cf.fieldType}</span></td>
                          <td>{cf.required ? 'YES (Mandatory)' : 'No'}</td>
                          <td>{cf.options && cf.options.length > 0 ? cf.options.join(', ') : '--'}</td>
                          <td className="text-center">
                            <span className={`gov-badge ${cf.active ? 'active' : 'inactive'}`}>
                              {cf.active ? 'ACTIVE' : 'INACTIVE'}
                            </span>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="7" className="text-center" style={{ padding: '24px' }}>
                          No custom field extensions defined yet for <strong>{targetEntity}</strong>.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SECTION 3: BUSINESS SETTINGS & POLICIES */}
      {activeSection === 'settings' && (
        <div className="gov-card">
          <div className="gov-card-header">
            <span>Configurable Policy Parameters & Operational Thresholds</span>
          </div>
          <div className="gov-card-body" style={{ padding: 0 }}>
            <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
              <table className="gov-table">
                <thead>
                  <tr>
                    <th>Configuration Parameter Key</th>
                    <th>Description & Scope</th>
                    <th>Current Configured Value</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {bizSettings.map((s) => (
                    <tr key={s.id}>
                      <td className="font-mono" style={{ fontWeight: 700 }}>{s.settingKey}</td>
                      <td>{s.description}</td>
                      <td>
                        <input
                          type="text"
                          className="gov-form-input font-mono"
                          defaultValue={s.settingValue}
                          id={`input_${s.settingKey}`}
                        />
                      </td>
                      <td>
                        <button
                          onClick={() => {
                            const input = document.getElementById(`input_${s.settingKey}`);
                            if (input) handleUpdateSetting(s.settingKey, input.value);
                          }}
                          disabled={savingSettingKey === s.settingKey}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                        >
                          {savingSettingKey === s.settingKey ? 'Saving...' : 'Update Policy'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* Master Value Add/Edit Modal */}
      <Modal
        isOpen={isMasterModalOpen}
        onClose={() => setIsMasterModalOpen(false)}
        title={selectedMaster ? `Edit Master: ${selectedMaster.name}` : `Add New Option to ${selectedCategory}`}
        size="default"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleMasterSubmit}>
          <div className="gov-form-group">
            <label className="gov-form-label required">Internal Code (Unique in Category)</label>
            <input
              type="text"
              required
              disabled={!!selectedMaster}
              className="gov-form-input font-mono"
              value={masterFormData.code}
              onChange={(e) => setMasterFormData({ ...masterFormData, code: e.target.value.toUpperCase() })}
              placeholder="e.g. ELEC_GRADE_1"
            />
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label required">Display Name / Label</label>
            <input
              type="text"
              required
              className="gov-form-input"
              value={masterFormData.name}
              onChange={(e) => setMasterFormData({ ...masterFormData, name: e.target.value })}
              placeholder="e.g. Senior Master Electrician"
            />
          </div>

          <div className="gov-form-group">
            <label className="gov-form-label">Description</label>
            <textarea
              className="gov-form-textarea"
              rows={2}
              value={masterFormData.description || ''}
              onChange={(e) => setMasterFormData({ ...masterFormData, description: e.target.value })}
              placeholder="Operational remarks"
            />
          </div>

          {selectedCategory === 'ATTENDANCE_STATUS' && (
            <div className="gov-form-group">
              <label className="gov-form-label required">Wage Multiplier Percentage (%)</label>
              <input
                type="number"
                step="0.01"
                required
                className="gov-form-input font-mono"
                value={masterFormData.wagePercentage}
                onChange={(e) => setMasterFormData({ ...masterFormData, wagePercentage: e.target.value })}
                placeholder="100.00 for full pay, 50.00 for half, 0 for unpaid"
              />
            </div>
          )}

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Sort Order</label>
              <input
                type="number"
                className="gov-form-input font-mono"
                value={masterFormData.sortOrder}
                onChange={(e) => setMasterFormData({ ...masterFormData, sortOrder: parseInt(e.target.value || 0, 10) })}
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Active State</label>
              <select
                className="gov-form-select"
                value={masterFormData.isActive ? 'true' : 'false'}
                onChange={(e) => setMasterFormData({ ...masterFormData, isActive: e.target.value === 'true' })}
              >
                <option value="true">Active (Visible in dropdowns)</option>
                <option value="false">Deactivated (Historical only)</option>
              </select>
            </div>
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsMasterModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Saving...' : 'Commit Master Value'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Custom Field Definition Modal */}
      <Modal
        isOpen={isCustomFieldModalOpen}
        onClose={() => setIsCustomFieldModalOpen(false)}
        title={`Add Custom Field to: ${targetEntity}`}
        size="lg"
      >
        {error && <div className="gov-alert gov-alert-danger">{error}</div>}
        <form onSubmit={handleCustomFieldSubmit}>
          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Field Label (Visible to user)</label>
              <input
                type="text"
                required
                className="gov-form-input"
                value={customFieldForm.fieldLabel}
                onChange={(e) => {
                  const lbl = e.target.value;
                  const key = lbl.toLowerCase().replace(/[^a-z0-9]/g, '_');
                  setCustomFieldForm({ ...customFieldForm, fieldLabel: lbl, fieldKey: key });
                }}
                placeholder="e.g. Electrical License Number"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label required">Internal Key (Database identifier)</label>
              <input
                type="text"
                required
                className="gov-form-input font-mono"
                value={customFieldForm.fieldKey}
                onChange={(e) => setCustomFieldForm({ ...customFieldForm, fieldKey: e.target.value })}
                placeholder="e.g. electrical_license_number"
              />
            </div>
          </div>

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label required">Field Input Type</label>
              <select
                className="gov-form-select"
                value={customFieldForm.fieldType}
                onChange={(e) => setCustomFieldForm({ ...customFieldForm, fieldType: e.target.value })}
              >
                <option value="TEXT">Short Text</option>
                <option value="NUMBER">Number (Integer)</option>
                <option value="DECIMAL">Decimal / Currency</option>
                <option value="DATE">Date Picker</option>
                <option value="SELECT">Dropdown Selection</option>
                <option value="TEXTAREA">Multi-line Text</option>
              </select>
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Mandatory (Required Field)?</label>
              <select
                className="gov-form-select"
                value={customFieldForm.isRequired ? 'true' : 'false'}
                onChange={(e) => setCustomFieldForm({ ...customFieldForm, isRequired: e.target.value === 'true' })}
              >
                <option value="false">Optional</option>
                <option value="true">Required (Must be filled)</option>
              </select>
            </div>
          </div>

          {customFieldForm.fieldType === 'SELECT' && (
            <div className="gov-form-group">
              <label className="gov-form-label required">Dropdown Options (Comma separated)</label>
              <input
                type="text"
                required
                className="gov-form-input"
                value={customFieldForm.options}
                onChange={(e) => setCustomFieldForm({ ...customFieldForm, options: e.target.value })}
                placeholder="e.g. Class A, Class B, Class C"
              />
            </div>
          )}

          <div className="gov-form-row">
            <div className="gov-form-col">
              <label className="gov-form-label">Placeholder / Hint Text</label>
              <input
                type="text"
                className="gov-form-input"
                value={customFieldForm.placeholder}
                onChange={(e) => setCustomFieldForm({ ...customFieldForm, placeholder: e.target.value })}
                placeholder="e.g. Enter statutory license ID"
              />
            </div>
            <div className="gov-form-col">
              <label className="gov-form-label">Display Order</label>
              <input
                type="number"
                className="gov-form-input font-mono"
                value={customFieldForm.displayOrder}
                onChange={(e) => setCustomFieldForm({ ...customFieldForm, displayOrder: e.target.value })}
              />
            </div>
          </div>

          <div className="gov-modal-footer">
            <button type="button" onClick={() => setIsCustomFieldModalOpen(false)} className="gov-btn gov-btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="gov-btn gov-btn-primary">
              {loading ? 'Adding Field...' : 'Add Field Extension'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
