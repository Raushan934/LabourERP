import React, { useState, useEffect } from 'react';
import {
  QrCode,
  CreditCard,
  Smartphone,
  Copy,
  Check,
  X,
  Upload,
  AlertCircle,
  Building,
  ShieldCheck,
  ExternalLink,
  Sparkles,
  Download,
  Printer,
  UserCheck
} from 'lucide-react';

export const PaymentDetailsModal = ({
  isOpen,
  onClose,
  title = 'Payment & Settlement Credentials',
  subtitle = '',
  paymentData = {},
  onSave = null,
  isEditable = false
}) => {
  const [copiedField, setCopiedField] = useState('');
  const [formData, setFormData] = useState({
    upiId: '',
    bankAccountNumber: '',
    bankIfscCode: '',
    bankName: '',
    bankBeneficiaryName: '',
    qrCodeUrl: ''
  });
  const [activeTab, setActiveTab] = useState('view'); // 'view' | 'edit'
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Sync state whenever target paymentData changes or modal opens
  useEffect(() => {
    if (paymentData) {
      setFormData({
        upiId: paymentData.upiId || '',
        bankAccountNumber: paymentData.bankAccountNumber || '',
        bankIfscCode: paymentData.bankIfscCode || '',
        bankName: paymentData.bankName || '',
        bankBeneficiaryName: paymentData.bankBeneficiaryName || paymentData.fullName || '',
        qrCodeUrl: paymentData.qrCodeUrl || ''
      });
      setError('');
      setSuccess('');
    }
  }, [paymentData, isOpen]);

  if (!isOpen) return null;

  // Derive guaranteed specific UPI ID if not explicitly saved
  const rawPhone = paymentData.mobileNumber || paymentData.phone || '';
  const digitsOnly = rawPhone.replace(/[^0-9]/g, '');
  const derivedUpiId = digitsOnly.length >= 10
    ? `${digitsOnly.slice(-10)}@upi`
    : (paymentData.username
        ? `${paymentData.username.toLowerCase()}@upi`
        : (paymentData.labourCode ? `${paymentData.labourCode.toLowerCase()}@upi` : 'workforce@upi'));

  const activeUpiId = (formData.upiId || paymentData.upiId || derivedUpiId).trim();
  const activeBeneficiary = (formData.bankBeneficiaryName || paymentData.bankBeneficiaryName || paymentData.fullName || 'Workforce Payee').trim();
  const upiPayUri = `upi://pay?pa=${encodeURIComponent(activeUpiId)}&pn=${encodeURIComponent(activeBeneficiary)}&cu=INR`;
  const qrImageSrc = formData.qrCodeUrl || paymentData.qrCodeUrl || `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(upiPayUri)}`;

  const copyToClipboard = (text, fieldName) => {
    if (!text) return;
    navigator.clipboard.writeText(text);
    setCopiedField(fieldName);
    setTimeout(() => setCopiedField(''), 2500);
  };

  const handleFileUpload = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      setError('Please select an image file (PNG, JPG, or WEBP) for the payment QR code.');
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      setError('QR code image size must be under 2MB.');
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      setFormData((prev) => ({ ...prev, qrCodeUrl: event.target?.result }));
      setError('');
    };
    reader.readAsDataURL(file);
  };

  const generateQrFromUpi = () => {
    const upiToUse = (formData.upiId || activeUpiId).trim();
    if (!upiToUse) {
      setError('Please enter a valid UPI ID or phone number first.');
      return;
    }
    const name = encodeURIComponent(formData.bankBeneficiaryName || activeBeneficiary);
    const uri = `upi://pay?pa=${upiToUse}%26pn=${name}%26cu=INR`;
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${uri}`;
    setFormData((prev) => ({ ...prev, upiId: upiToUse, qrCodeUrl: qrUrl }));
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!onSave) return;
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        ...formData,
        upiId: formData.upiId?.trim() || activeUpiId,
        bankBeneficiaryName: formData.bankBeneficiaryName?.trim() || activeBeneficiary,
        qrCodeUrl: formData.qrCodeUrl || qrImageSrc
      };
      await onSave(payload);
      setSuccess('Payment credentials successfully saved!');
      setTimeout(() => {
        setActiveTab('view');
        setSuccess('');
      }, 1000);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to save payment credentials');
    } finally {
      setSaving(false);
    }
  };

  const handlePrint = () => {
    const printWindow = window.open('', '_blank', 'width=600,height=700');
    if (!printWindow) return;
    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Payment QR Code - ${activeBeneficiary}</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; text-align: center; padding: 30px; background: #fff; }
          .card { border: 2px solid #0F172A; border-radius: 8px; padding: 24px; max-width: 380px; margin: 0 auto; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
          h2 { margin: 0 0 6px 0; color: #1E3A8A; font-size: 20px; }
          .sub { color: #475569; font-size: 13px; margin-bottom: 16px; }
          img { width: 220px; height: 220px; display: block; margin: 0 auto 16px; }
          .upi-box { background: #F1F5F9; border-radius: 6px; padding: 10px; font-family: monospace; font-weight: bold; font-size: 15px; color: #0F172A; margin-bottom: 12px; }
          .bank-info { font-size: 12px; color: #334155; text-align: left; background: #F8FAFC; border: 1px solid #E2E8F0; padding: 10px; border-radius: 6px; }
          .badge { display: inline-block; background: #16A34A; color: #fff; font-size: 11px; font-weight: bold; padding: 3px 8px; border-radius: 4px; margin-bottom: 12px; }
          @media print { button { display: none; } }
        </style>
      </head>
      <body>
        <div class="card">
          <div class="badge">OFFICIAL WORKFORCE PAYMENT QR</div>
          <h2>${activeBeneficiary}</h2>
          <div class="sub">${paymentData.labourCode ? 'Labour ID: ' + paymentData.labourCode : (paymentData.username ? 'User: ' + paymentData.username : '')} ${paymentData.designation ? '• ' + paymentData.designation : ''}</div>
          <img src="${qrImageSrc}" alt="UPI QR Code" />
          <div class="upi-box">UPI: ${activeUpiId}</div>
          ${formData.bankAccountNumber ? `
            <div class="bank-info">
              <strong>Bank Account:</strong> ${formData.bankAccountNumber}<br/>
              <strong>IFSC:</strong> ${formData.bankIfscCode || 'N/A'}<br/>
              <strong>Bank:</strong> ${formData.bankName || 'Direct Account'}
            </div>
          ` : ''}
          <p style="font-size: 11px; color: #64748B; margin-top: 14px;">Scan with PhonePe, Google Pay, Paytm, or BHIM</p>
        </div>
        <script>
          window.onload = function() { window.print(); };
        </script>
      </body>
      </html>
    `);
    printWindow.document.close();
  };

  return (
    <div
      className="gov-modal-backdrop"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.65)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 9999,
        padding: '10px'
      }}
    >
      <div
        style={{
          backgroundColor: '#FFFFFF',
          width: '100%',
          maxWidth: '520px',
          maxHeight: '94vh',
          borderRadius: '6px',
          boxShadow: '0 12px 36px rgba(0,0,0,0.3)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden'
        }}
      >
        {/* Header */}
        <div
          style={{
            backgroundColor: 'var(--gov-navy)',
            color: '#FFFFFF',
            padding: '12px 16px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between'
          }}
        >
          <div>
            <div style={{ fontWeight: 700, fontSize: '15px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <QrCode size={18} color="#FBBF24" />
              <span>{title}</span>
            </div>
            {subtitle && <div style={{ fontSize: '12px', color: '#CBD5E1', marginTop: '2px' }}>{subtitle}</div>}
          </div>
          <button
            type="button"
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: '#CBD5E1', cursor: 'pointer', padding: '4px' }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Tab switch for editable mode */}
        {isEditable && (
          <div style={{ display: 'flex', borderBottom: '1px solid var(--gov-border)', backgroundColor: '#F8FAFC' }}>
            <button
              type="button"
              onClick={() => setActiveTab('view')}
              style={{
                flex: 1,
                padding: '10px 14px',
                border: 'none',
                background: 'none',
                fontWeight: activeTab === 'view' ? 700 : 500,
                color: activeTab === 'view' ? 'var(--gov-navy)' : '#64748B',
                borderBottom: activeTab === 'view' ? '3px solid var(--gov-navy)' : 'none',
                cursor: 'pointer',
                fontSize: '13px'
              }}
            >
              Scan & Pay (Specific QR)
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('edit')}
              style={{
                flex: 1,
                padding: '10px 14px',
                border: 'none',
                background: 'none',
                fontWeight: activeTab === 'edit' ? 700 : 500,
                color: activeTab === 'edit' ? 'var(--gov-navy)' : '#64748B',
                borderBottom: activeTab === 'edit' ? '3px solid var(--gov-navy)' : 'none',
                cursor: 'pointer',
                fontSize: '13px'
              }}
            >
              Edit Details & Custom QR
            </button>
          </div>
        )}

        {/* Modal Body */}
        <div style={{ padding: '16px', overflowY: 'auto', flex: 1 }}>
          {error && (
            <div className="gov-alert gov-alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '12px', fontSize: '13px' }}>
              <AlertCircle size={15} /> {error}
            </div>
          )}
          {success && (
            <div className="gov-alert gov-alert-success" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '12px', fontSize: '13px' }}>
              <Check size={15} /> {success}
            </div>
          )}

          {/* VIEW TAB: SPECIFIC QR SCAN & PAY */}
          {activeTab === 'view' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {/* Payee Profile Strip */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                backgroundColor: '#F1F5F9',
                borderRadius: '6px',
                border: '1px solid #CBD5E1'
              }}>
                <div>
                  <div style={{ fontWeight: 700, fontSize: '14px', color: '#0F172A' }}>
                    {activeBeneficiary}
                  </div>
                  <div style={{ fontSize: '11px', color: '#64748B' }}>
                    {paymentData.labourCode ? `Labour Code: ${paymentData.labourCode}` : (paymentData.username ? `Staff Username: ${paymentData.username}` : '')}
                    {paymentData.designation ? ` • ${paymentData.designation}` : (paymentData.roles ? ` • ${paymentData.roles.join(', ').replace(/ROLE_/g, '')}` : '')}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span className="gov-badge gov-badge-success" style={{ fontSize: '11px', display: 'inline-flex', alignItems: 'center', gap: '3px' }}>
                    <Check size={11} /> Specific QR Active
                  </span>
                </div>
              </div>

              {/* Specific Scannable QR Card */}
              <div style={{
                border: '2px solid #2563EB',
                borderRadius: '8px',
                padding: '16px 12px',
                backgroundColor: '#EFF6FF',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '12px', fontWeight: 700, color: '#1E3A8A', textTransform: 'uppercase', marginBottom: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}>
                  <QrCode size={16} color="#2563EB" /> Payee Specific UPI QR Code
                </div>

                <div style={{
                  display: 'inline-block',
                  padding: '10px',
                  backgroundColor: '#FFFFFF',
                  borderRadius: '6px',
                  border: '1px solid #BFDBFE',
                  boxShadow: '0 4px 12px rgba(37,99,235,0.12)'
                }}>
                  <img
                    src={qrImageSrc}
                    alt="Specific UPI QR Code"
                    style={{ width: '190px', height: '190px', objectFit: 'contain', display: 'block' }}
                  />
                </div>

                <div style={{ fontSize: '11px', color: '#3B82F6', marginTop: '8px', fontWeight: 500 }}>
                  Scan with PhonePe, Google Pay, Paytm, BHIM, or any Banking App
                </div>

                {/* Direct App Launch Button (One-Touch on Mobile) */}
                <div style={{ marginTop: '12px', display: 'flex', justifyContent: 'center', gap: '8px', flexWrap: 'wrap' }}>
                  <a
                    href={upiPayUri}
                    className="gov-btn gov-btn-primary"
                    style={{ fontSize: '12px', padding: '6px 14px', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Smartphone size={14} /> Pay in UPI App (Mobile)
                  </a>
                  <button
                    type="button"
                    onClick={handlePrint}
                    className="gov-btn gov-btn-secondary"
                    style={{ fontSize: '12px', padding: '6px 12px', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                    title="Print or save physical payment QR card"
                  >
                    <Printer size={13} /> Print QR Card
                  </button>
                </div>
              </div>

              {/* UPI ID Card */}
              <div style={{
                border: '1px solid #CBD5E1',
                borderRadius: '6px',
                padding: '10px 14px',
                backgroundColor: '#FFFFFF',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '10px'
              }}>
                <div>
                  <div style={{ fontSize: '11px', fontWeight: 700, color: '#64748B', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Smartphone size={13} color="#2563EB" /> Payee Specific UPI ID / Mobile
                  </div>
                  <div style={{ fontSize: '14px', fontWeight: 700, color: '#1E293B', marginTop: '2px', fontFamily: 'monospace' }}>
                    {activeUpiId}
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => copyToClipboard(activeUpiId, 'upi')}
                  className="gov-btn gov-btn-secondary gov-btn-sm"
                  style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '6px 10px', fontSize: '11px' }}
                >
                  {copiedField === 'upi' ? <Check size={13} color="#16A34A" /> : <Copy size={13} />}
                  {copiedField === 'upi' ? 'Copied!' : 'Copy UPI'}
                </button>
              </div>

              {/* Bank Account Details Card */}
              <div style={{
                border: '1px solid #CBD5E1',
                borderRadius: '6px',
                padding: '12px 14px',
                backgroundColor: '#FFFFFF'
              }}>
                <div style={{ fontSize: '11px', fontWeight: 700, color: '#64748B', textTransform: 'uppercase', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Building size={13} color="#16A34A" /> Direct Bank Account Details
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px', fontSize: '13px' }}>
                  <div>
                    <span style={{ fontSize: '11px', color: '#64748B' }}>Account Number:</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, fontFamily: 'monospace' }}>
                      <span>{formData.bankAccountNumber || '—'}</span>
                      {formData.bankAccountNumber && (
                        <button
                          type="button"
                          onClick={() => copyToClipboard(formData.bankAccountNumber, 'acc')}
                          title="Copy account number"
                          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '2px', color: '#64748B' }}
                        >
                          {copiedField === 'acc' ? <Check size={12} color="#16A34A" /> : <Copy size={12} />}
                        </button>
                      )}
                    </div>
                  </div>

                  <div>
                    <span style={{ fontSize: '11px', color: '#64748B' }}>IFSC Code:</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 700, fontFamily: 'monospace' }}>
                      <span>{formData.bankIfscCode || '—'}</span>
                      {formData.bankIfscCode && (
                        <button
                          type="button"
                          onClick={() => copyToClipboard(formData.bankIfscCode, 'ifsc')}
                          title="Copy IFSC code"
                          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '2px', color: '#64748B' }}
                        >
                          {copiedField === 'ifsc' ? <Check size={12} color="#16A34A" /> : <Copy size={12} />}
                        </button>
                      )}
                    </div>
                  </div>

                  {formData.bankName && (
                    <div>
                      <span style={{ fontSize: '11px', color: '#64748B' }}>Bank Name:</span>
                      <div style={{ fontWeight: 600 }}>{formData.bankName}</div>
                    </div>
                  )}

                  {formData.bankBeneficiaryName && (
                    <div>
                      <span style={{ fontSize: '11px', color: '#64748B' }}>Beneficiary Name:</span>
                      <div style={{ fontWeight: 600 }}>{formData.bankBeneficiaryName}</div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* EDIT TAB */}
          {activeTab === 'edit' && (
            <form onSubmit={handleSubmit}>
              <div style={{ fontSize: '12px', color: '#4B5563', backgroundColor: '#F1F5F9', padding: '8px 12px', border: '1px solid #CBD5E1', marginBottom: '14px', borderRadius: '4px' }}>
                <ShieldCheck size={13} style={{ display: 'inline', verticalAlign: 'text-bottom', marginRight: '4px' }} />
                Update specific payment credentials or upload custom merchant QR pictures.
              </div>

              {/* UPI ID Field */}
              <div style={{ border: '1px solid var(--gov-border)', padding: '12px', marginBottom: '14px', borderRadius: '4px' }}>
                <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--gov-navy)', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Smartphone size={14} color="#2563EB" /> 1. Payee Specific UPI Handle
                </div>

                <div className="gov-form-group">
                  <label className="gov-form-label">UPI ID or Mobile Number</label>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <input
                      type="text"
                      className="gov-form-input"
                      value={formData.upiId}
                      onChange={(e) => setFormData({ ...formData, upiId: e.target.value })}
                      placeholder={activeUpiId}
                    />
                    <button
                      type="button"
                      onClick={generateQrFromUpi}
                      className="gov-btn gov-btn-secondary gov-btn-sm"
                      title="Auto-generate QR code from this UPI ID"
                      style={{ whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '4px' }}
                    >
                      <Sparkles size={13} color="#D97706" /> Auto-QR
                    </button>
                  </div>
                  <div className="gov-form-help">Current specific handle: <strong>{activeUpiId}</strong></div>
                </div>

                <div className="gov-form-group" style={{ marginBottom: 0 }}>
                  <label className="gov-form-label">Custom QR Picture (Upload Image or Paste URL)</label>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    <div style={{ display: 'flex', gap: '6px' }}>
                      <input
                        type="text"
                        className="gov-form-input"
                        value={formData.qrCodeUrl}
                        onChange={(e) => setFormData({ ...formData, qrCodeUrl: e.target.value })}
                        placeholder="Image URL or upload custom QR picture below..."
                      />
                      {formData.qrCodeUrl && (
                        <button
                          type="button"
                          onClick={() => setFormData({ ...formData, qrCodeUrl: '' })}
                          className="gov-btn gov-btn-secondary gov-btn-sm"
                        >
                          Clear
                        </button>
                      )}
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <label className="gov-btn gov-btn-secondary gov-btn-sm" style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                        <Upload size={13} /> Select QR Image File
                        <input
                          type="file"
                          accept="image/*"
                          onChange={handleFileUpload}
                          style={{ display: 'none' }}
                        />
                      </label>
                      <span style={{ fontSize: '11px', color: '#64748B' }}>Accepts PNG, JPG, or screenshot</span>
                    </div>

                    {formData.qrCodeUrl && (
                      <div style={{ marginTop: '4px', textAlign: 'center', padding: '6px', backgroundColor: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: '4px' }}>
                        <img
                          src={formData.qrCodeUrl}
                          alt="QR Preview"
                          style={{ width: '100px', height: '100px', objectFit: 'contain', margin: '0 auto', display: 'block' }}
                        />
                        <span style={{ fontSize: '10px', color: '#16A34A', fontWeight: 600 }}>✓ Custom QR Image Active</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Bank Account Section */}
              <div style={{ border: '1px solid var(--gov-border)', padding: '12px', marginBottom: '14px', borderRadius: '4px' }}>
                <div style={{ fontWeight: 700, fontSize: '13px', color: 'var(--gov-navy)', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Building size={14} color="#16A34A" /> 2. Bank Account Details (NEFT / IMPS / RTGS)
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px' }}>
                  <div className="gov-form-group">
                    <label className="gov-form-label">Bank Account Number</label>
                    <input
                      type="text"
                      className="gov-form-input font-mono"
                      value={formData.bankAccountNumber}
                      onChange={(e) => setFormData({ ...formData, bankAccountNumber: e.target.value })}
                      placeholder="e.g. 10293847561"
                    />
                  </div>

                  <div className="gov-form-group">
                    <label className="gov-form-label">IFSC Code</label>
                    <input
                      type="text"
                      className="gov-form-input font-mono"
                      value={formData.bankIfscCode}
                      onChange={(e) => setFormData({ ...formData, bankIfscCode: e.target.value.toUpperCase() })}
                      placeholder="e.g. SBIN0001234"
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '10px' }}>
                  <div className="gov-form-group" style={{ marginBottom: 0 }}>
                    <label className="gov-form-label">Bank Name</label>
                    <input
                      type="text"
                      className="gov-form-input"
                      value={formData.bankName}
                      onChange={(e) => setFormData({ ...formData, bankName: e.target.value })}
                      placeholder="e.g. State Bank of India"
                    />
                  </div>

                  <div className="gov-form-group" style={{ marginBottom: 0 }}>
                    <label className="gov-form-label">Beneficiary Account Name</label>
                    <input
                      type="text"
                      className="gov-form-input"
                      value={formData.bankBeneficiaryName}
                      onChange={(e) => setFormData({ ...formData, bankBeneficiaryName: e.target.value })}
                      placeholder={activeBeneficiary}
                    />
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '16px' }}>
                <button
                  type="button"
                  onClick={() => setActiveTab('view')}
                  className="gov-btn gov-btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="gov-btn gov-btn-primary"
                >
                  {saving ? 'Saving Details...' : 'Save Payment Credentials'}
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Footer */}
        <div style={{ padding: '10px 16px', backgroundColor: '#F1F5F9', borderTop: '1px solid var(--gov-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '11px', color: '#64748B' }}>
            Secured for Admin/Owner & Accountant access
          </span>
          <button
            type="button"
            onClick={onClose}
            className="gov-btn gov-btn-secondary gov-btn-sm"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
