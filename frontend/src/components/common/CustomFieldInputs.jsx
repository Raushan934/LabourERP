import React, { useState, useEffect } from 'react';
import api from '../../services/api';

export const CustomFieldInputs = ({ businessId, entityType, values = {}, onChange }) => {
  const [definitions, setDefinitions] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (businessId && entityType) {
      setLoading(true);
      api.get(`/custom-fields/definitions?businessId=${businessId}&entityType=${entityType}&activeOnly=true`)
        .then((res) => {
          if (res.data && res.data.data) {
            setDefinitions(res.data.data);
          }
        })
        .catch((err) => console.error('Failed to load custom fields', err))
        .finally(() => setLoading(false));
    }
  }, [businessId, entityType]);

  if (loading || definitions.length === 0) return null;

  return (
    <div style={{ marginTop: '16px', borderTop: '1px solid var(--gov-border)', paddingTop: '12px' }}>
      <h4 style={{ fontSize: '13px', fontWeight: 700, color: 'var(--gov-navy)', marginBottom: '8px' }}>
        Dynamic Custom Fields (Admin Configured)
      </h4>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px' }}>
        {definitions.map((def) => {
          const val = values[def.fieldKey] !== undefined ? values[def.fieldKey] : (def.defaultValue || '');

          return (
            <div key={def.id} className="gov-form-group">
              <label className={`gov-form-label ${def.required ? 'required' : ''}`}>
                {def.fieldLabel}
              </label>

              {def.fieldType === 'SELECT' ? (
                <select
                  className="gov-form-select"
                  value={val}
                  required={def.required}
                  onChange={(e) => onChange(def.fieldKey, e.target.value)}
                >
                  <option value="">-- Select {def.fieldLabel} --</option>
                  {def.options && def.options.map((opt, idx) => (
                    <option key={idx} value={opt}>{opt}</option>
                  ))}
                </select>
              ) : def.fieldType === 'TEXTAREA' ? (
                <textarea
                  className="gov-form-textarea"
                  rows={2}
                  value={val}
                  placeholder={def.placeholder || ''}
                  required={def.required}
                  onChange={(e) => onChange(def.fieldKey, e.target.value)}
                />
              ) : (
                <input
                  type={def.fieldType === 'NUMBER' || def.fieldType === 'DECIMAL' ? 'number' : def.fieldType === 'DATE' ? 'date' : 'text'}
                  step={def.fieldType === 'DECIMAL' ? '0.01' : '1'}
                  className="gov-form-input"
                  value={val}
                  placeholder={def.placeholder || ''}
                  required={def.required}
                  onChange={(e) => onChange(def.fieldKey, e.target.value)}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
