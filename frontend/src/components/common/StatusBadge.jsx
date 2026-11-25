import React from 'react';

export const StatusBadge = ({ status }) => {
  if (!status) return null;
  const normalized = status.toLowerCase().replace(/\s+/g, '_');
  return <span className={`gov-badge ${normalized}`}>{status.replace(/_/g, ' ')}</span>;
};
