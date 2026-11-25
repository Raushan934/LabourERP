import React, { useState, useEffect } from 'react';
import { useBusiness } from '../../context/BusinessContext';
import { Breadcrumbs } from '../../components/common/Breadcrumbs';
import api from '../../services/api';
import { History, ShieldCheck, Search, Filter } from 'lucide-react';

export const Audit = () => {
  const { activeBusinessId } = useBusiness();
  const [logs, setLogs] = useState([]);
  const [pageData, setPageData] = useState({ pageNumber: 0, pageSize: 20, totalElements: 0, totalPages: 1 });
  const [entityFilter, setEntityFilter] = useState('');
  const [actionFilter, setActionFilter] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchLogs = async (page = 0) => {
    setLoading(true);
    try {
      let url = `/audit-logs?page=${page}&size=20`;
      if (activeBusinessId) url += `&businessId=${activeBusinessId}`;
      if (entityFilter) url += `&entityType=${entityFilter}`;
      if (actionFilter) url += `&actionType=${actionFilter}`;

      const res = await api.get(url);
      if (res.data?.data) {
        setLogs(res.data.data.content || []);
        setPageData({
          pageNumber: res.data.data.pageNumber,
          pageSize: res.data.data.pageSize,
          totalElements: res.data.data.totalElements,
          totalPages: res.data.data.totalPages
        });
      }
    } catch (err) {
      console.error('Failed to load audit logs', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(0);
  }, [activeBusinessId, entityFilter, actionFilter]);

  return (
    <div className="gov-page-wrapper">
      <Breadcrumbs items={[{ label: 'Audit History & Logs' }]} />

      <div className="gov-page-header">
        <div className="gov-page-title">
          <h2>Forensic System Audit History & Change Register</h2>
          <p>Immutable tamper-evident logging of administrative actions, attendance corrections, and financial disbursements</p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="gov-filter-bar">
        <div style={{ width: '200px' }}>
          <label className="gov-form-label">Entity Scope</label>
          <select
            className="gov-form-select"
            value={entityFilter}
            onChange={(e) => setEntityFilter(e.target.value)}
          >
            <option value="">All Entities</option>
            <option value="ATTENDANCE">ATTENDANCE</option>
            <option value="LABOURER">LABOURER</option>
            <option value="SITE">SITE</option>
            <option value="PAYMENT">PAYMENT</option>
            <option value="ADVANCE">ADVANCE</option>
            <option value="PAYROLL">PAYROLL</option>
            <option value="BUSINESS">BUSINESS</option>
            <option value="USER">USER</option>
          </select>
        </div>

        <div style={{ width: '200px' }}>
          <label className="gov-form-label">Action Type</label>
          <select
            className="gov-form-select"
            value={actionFilter}
            onChange={(e) => setActionFilter(e.target.value)}
          >
            <option value="">All Actions</option>
            <option value="LOGIN">LOGIN</option>
            <option value="CREATE">CREATE</option>
            <option value="UPDATE">UPDATE</option>
            <option value="CORRECTION">CORRECTION</option>
            <option value="STATUS_CHANGE">STATUS CHANGE</option>
            <option value="BULK_SAVE">BULK SAVE</option>
            <option value="PAYROLL_GENERATE">PAYROLL GENERATE</option>
            <option value="TRANSFER">TRANSFER</option>
          </select>
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="gov-card">
        <div className="gov-card-header">
          <span>Security & Operational Audit Trail</span>
          <span style={{ fontSize: '12px', fontWeight: 600 }}>Total Records: {pageData.totalElements}</span>
        </div>
        <div className="gov-card-body" style={{ padding: 0 }}>
          <div className="gov-table-container" style={{ border: 'none', margin: 0 }}>
            <table className="gov-table">
              <thead>
                <tr>
                  <th>Timestamp (IST)</th>
                  <th>User / Operator</th>
                  <th>Action Type</th>
                  <th>Target Entity</th>
                  <th>Record Key / ID</th>
                  <th>State Transition / Change Details</th>
                  <th>IP Address</th>
                </tr>
              </thead>
              <tbody>
                {logs.length > 0 ? (
                  logs.map((log) => (
                    <tr key={log.id}>
                      <td className="font-mono" style={{ fontSize: '12px' }}>
                        {new Date(log.createdAt).toLocaleString('en-IN')}
                      </td>
                      <td>
                        <strong>{log.username}</strong>
                      </td>
                      <td>
                        <span className="gov-badge draft" style={{ fontWeight: 700 }}>
                          {log.actionType}
                        </span>
                      </td>
                      <td>{log.entityType}</td>
                      <td className="font-mono">{log.entityId}</td>
                      <td style={{ fontSize: '12px', maxWidth: '380px' }}>
                        {log.previousValue && (
                          <div style={{ color: 'var(--gov-danger)', textDecoration: 'line-through' }}>
                            Prev: {log.previousValue}
                          </div>
                        )}
                        <div style={{ color: 'var(--gov-navy)' }}>
                          {log.newValue}
                        </div>
                      </td>
                      <td className="font-mono" style={{ fontSize: '12px' }}>{log.ipAddress || '127.0.0.1'}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" className="text-center" style={{ padding: '24px' }}>
                      {loading ? 'Retrieving audit log entries...' : 'No audit entries match the current filter criteria.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Pagination */}
      {pageData.totalPages > 1 && (
        <div className="gov-pagination">
          <span>Showing page {pageData.pageNumber + 1} of {pageData.totalPages}</span>
          <div className="gov-page-btns">
            <button
              disabled={pageData.pageNumber === 0}
              onClick={() => fetchLogs(pageData.pageNumber - 1)}
              className="gov-page-btn"
            >
              Previous
            </button>
            <button
              disabled={pageData.pageNumber + 1 >= pageData.totalPages}
              onClick={() => fetchLogs(pageData.pageNumber + 1)}
              className="gov-page-btn"
            >
              Next
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
