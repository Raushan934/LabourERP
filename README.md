# Labour Management ERP System (Government Administrative Standard)

> **Centralized Multi-Business Workforce, Site Muster, Wages & Financial Operations Management Platform**  
> **Aesthetic Standard:** Indian Government Administrative Portal (`gov.in` standard — high information density, sharp rectangular styling, `#1B3557` palette, strictly zero gradients, zero AI dashboard gimmickry).

---

## 1. Quick Access & Live Services

Both the Backend API and Frontend Administrative Portal are currently compiled, seeded, and running:

- **Frontend Administrative Portal:** [http://localhost:5173](http://localhost:5173)
- **Backend REST API:** [http://localhost:8080](http://localhost:8080)
- **API Base Context:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)

### Pre-Configured Administrative Credentials

| Role | Username | Password | Authority Scope |
|---|---|---|---|
| **ADMIN / OWNER** | `admin` | `Admin@123` | Full enterprise control across all businesses, custom fields, master data, and settings |
| **OPERATIONS MANAGER** | `manager` | `Manager@123` | Operational management over assigned businesses, sites, and workforce directory |
| **SITE SUPERVISOR** | `supervisor` | `Supervisor@123` | Daily site muster logging, overtime entry, and attendance correction requests |
| **ACCOUNTANT** | `accountant` | `Accountant@123` | Advance disbursements, wage payroll settlement, and official financial statements |

*(All login credentials can be filled with a single click from the login portal's quick-fill buttons).*

---

## 2. Technology Stack

- **Frontend:**
  - React 18.3.1 (Single Page Administrative Application)
  - Vite 5.2.11
  - React Router DOM 6.23.1
  - Axios 1.7.2 with JWT Bearer & Multi-Tenant `X-Business-ID` Context Interceptors
  - Government Administrative CSS (`gov.in` design standard: `#1B3557`, `#152A45`, `#245A85`, `#EAF2F8`, `#F4F6F8`, `#D5DCE3`)
- **Backend:**
  - Java 22 / JDK 22
  - Spring Boot 3.3.5
  - Spring Security with Stateless JWT Authentication & BCrypt Password Hashing
  - Spring Data JPA & Hibernate 6
  - Flyway Migration Engine (V1 Relational Schema & V2 Master Seed Data)
  - Maven 3.9.11
- **Database & Containerization:**
  - MySQL 8.0 (Production / Docker) & In-Memory Mode with identical Flyway seed
  - Docker & Docker Compose (`docker-compose.yml`, multi-stage Dockerfiles for backend and frontend)
  - Persistent volume storage for database transactions

---

## 3. Core Enterprise Modules

1. **Dashboard:** Real-time business KPIs, today's muster roll breakdown (Present, Half Day, Absent, Paid Leave), site muster overview table, and period financial accrual summaries.
2. **Multi-Business Management:** Centralized registry supporting multiple isolated businesses (*Bharat Electrical Contracting Corp.*, *National Fuel & Petrol Retail Hub*, etc.). Strict data filtering prevents cross-tenant data leakage.
3. **Site & Location Management:** Construction site register with site codes, client details, assigned supervisors, work locations, start/end dates, and workforce headcount.
4. **Labour Workforce Directory:** Worker KYC, category (Electrician, Helper, Technician, Supervisor), designation, daily base wage, hourly overtime rate, current site, emergency contacts, and soft deactivation (preserving historical records).
5. **Daily Attendance & Muster Roll:** Site and date muster grid with one-click bulk marking (All Present, All Half-Day, All Absent), overtime logging, and an **Attendance Correction Audit Modal** requiring mandatory justification reasons.
6. **Wage & Payroll Ledger:** Dynamic calculation engine that processes attendance, half-day wage rules, paid leaves, weekly offs, overtime, and advances recovery (capped by policy). Generates itemized periodic wage ledgers.
7. **Advance Management:** Loan and advance disbursement tracking with recovery balances, payment channels, reference numbers, and settlement statuses.
8. **Payment Disbursements:** Wage disbursements supporting partial and staged payments, payment channels (Cash, NEFT, UPI), and voucher reference numbers.
9. **Labour Transfers:** Inter-site workforce reallocation tracking origin and destination sites, transfer dates, and operational reasons without overwriting historical attendance records.
10. **Reports & Statutory Statements:**
    - Form VII Individual Labour Statement with full daily muster breakdown, advance ledger, disbursement history, and printable/CSV download formats.
    - Site-Wise Monthly Cost & Muster Matrix.
11. **Users & Permissions:** User directory and RBAC security enforcement.
12. **Audit History:** Tamper-evident forensic audit log capturing User, Action, Entity, Before/After JSON states, IP addresses, and timestamps.
13. **Dynamic Master Data & Custom Field Builder:**
    - Dynamic management of Business Types, Labour Categories, Designations, Attendance Statuses, Payment Methods, and Advance Types.
    - No-code Custom Field Builder allowing administrators to add new fields to Labourers, Sites, Attendance, Payments, and Advances directly through the UI.

---

## 4. How to Run via Docker Compose

To run the entire multi-container production stack with MySQL 8, Backend, and Frontend:

```bash
# 1. Ensure Docker Desktop is running
# 2. Build and run containers
docker compose up --build -d

# 3. View logs
docker compose logs -f

# 4. Access portal
# Frontend: http://localhost
# Backend API: http://localhost:8080/api/v1
```

---

## 5. Development & Verification Summary

- Backend Maven Build: `BUILD SUCCESS` (Compiled with Java 22, 110 source classes).
- Frontend Vite Build: `✓ built in 2.85s` (Compiled with 0 errors).
- Architecture & Design Specification artifact available at `C:\Users\Amit Kumar\.gemini\antigravity\brain\d9ac1611-8fbe-4a6e-a9d8-e84fb8d21663\ARCHITECTURE_AND_DESIGN_SPEC.md`.
