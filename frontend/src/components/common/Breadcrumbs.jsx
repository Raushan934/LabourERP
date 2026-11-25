import React from 'react';
import { Link } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';

export const Breadcrumbs = ({ items = [] }) => {
  return (
    <div className="gov-breadcrumbs">
      <Link to="/dashboard" style={{ display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
        <Home size={12} /> Home
      </Link>
      {items.map((item, idx) => (
        <React.Fragment key={idx}>
          <ChevronRight size={12} />
          {item.link ? (
            <Link to={item.link}>{item.label}</Link>
          ) : (
            <span style={{ color: '#222222', fontWeight: 600 }}>{item.label}</span>
          )}
        </React.Fragment>
      ))}
    </div>
  );
};
