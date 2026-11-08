# DailyWorth: Personal Expense Tracker & Daily Budget Management

A production-ready personal finance and daily budget management application built on the philosophy: **"Know your limit. Own your day."** Rather than merely tracking past expenditure, DailyWorth empowers users by calculating an exact, safe-to-spend daily allowance based on actual take-home salary, fixed monthly commitments, and savings targets, resetting every single morning without balance roll-overs.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture & Execution Alignment:**
> 1. **Zero-Rollover Daily Allowance Philosophy**: Unused daily allowance does **NOT** roll over to the next day. Overspending on any single day does not reduce tomorrow's clean allowance. Daily allowances are calculated dynamically from the active calendar month length (28, 29, 30, or 31 days) and eligible daily transaction totals.
> 2. **Financial Precision Guarantee**: All monetary calculations utilize arbitrary-precision decimals (`BigDecimal` / `DECIMAL(12, 2)`). Never float or double.
> 3. **Dual Delivery Alignment**: 
>    - **Backend & Database Specifications**: Complete Spring Boot 3.x modular architecture, MySQL 8.x DDL with indexes and foreign keys, REST API contracts, and Spring Security JWT specs.
>    - **Interactive Android Applet**: Implemented in modern Jetpack Compose with Material 3 (slate/deep charcoal theme with emerald gains and crimson alerts), powered by a local Room database engine matching the exact relational schema, enabling an immediate, responsive, and tactile experience in the emulator with fast sub-5-second expense entry.

---

## 1. Overview & Core Concept

### What DailyWorth Does
Most expense trackers simply show where money went after it was already spent. DailyWorth flips this paradigm forward: it computes how much money the user can safely spend **today** without jeopardizing their fixed obligations (rent, EMI, bills) or monthly savings goals.

### The Math Formula
```
Available Monthly Budget = Monthly Take-home Salary - Fixed Monthly Expenses - Monthly Savings Target
Daily Allowance          = Available Monthly Budget / Number of Days in Current Calendar Month
Remaining Today          = Daily Allowance - Today's Eligible Expenses
Overspent Today          = max(0, Today's Eligible Expenses - Daily Allowance)
```

### Core Tenets
- **Fresh Start Every Morning**: Each day begins with a full, clean allowance. Yesterday's restraint is locked in as savings; yesterday's impulse buy does not penalize today's basic sustenance.
- **Sub-5-Second Expense Entry**: Large numeric keypad, quick category chips, instant balance deduction.
- **Isolated Multi-User Security**: Strict tenant isolation by `user_id` across all queries, DTOs, and tokens.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Surface Vibe**: Deep Navy & Charcoal surfaces (`#0D1117`, `#161B22`, `#21262D`) offering high-contrast clarity and a sleek fintech feel.
- **Positive Accent (Emerald Green)**: `#10B981` / `#059669` for safe allowances, healthy margins, and goal milestones.
- **Caution / Warning (Amber)**: `#F59E0B` when spending reaches 75% of daily allowance.
- **Overspent Accent (Crimson / Coral)**: `#EF4444` when allowance is exhausted and overspending occurs.
- **Typography**: Prominent, tabular currency numbers for instant legibility (`Inter` / M3 Display Medium) paired with crisp geometric labels.

### Key User Flows
1. **Onboarding & Financial Calibration**:
   - Welcome & authentication (Email/Password + optional Quick PIN).
   - Enter Gross Salary vs. Net Take-Home Salary.
   - Enter Recurring Fixed Monthly Expenses (Rent, utilities, EMIs).
   - Set Monthly Savings Target (e.g. ₹8,000 / month).
   - Immediate live preview of calculated Daily Allowance (e.g., ₹800/day).
2. **Dashboard ("Today's Pulse")**:
   - Big radial / linear visual allowance gauge showing Remaining vs. Spent today.
   - Status badge: *"On Track"*, *"Tight"*, or *"Overspent by ₹X"*.
   - Quick-add Floating Action Button (`+`).
   - Recent 3 transactions and Month-to-date savings accumulation badge.
3. **Quick-Add Expense Modal**:
   - Huge numeric keypad input.
   - One-tap category grid (Food, Travel, Shopping, Entertainment, Household, Health, Bills, Others).
   - Payment method toggle (Cash, UPI, Debit Card, Credit Card).
   - Instant "Save" button triggering immediate visual feedback and allowance gauge animation.
4. **Calendar & Transaction History**:
   - Interactive calendar heat-map where days are color-coded (Green = Under budget, Red = Overspent, Gray = Rest day).
   - Tapping any date reveals that exact historical date's allowance, expenses, and delta.
5. **Savings Goals Hub**:
   - Visual progress bars for targets (Emergency Fund, Tech Upgrade, Travel).
   - Dedicated allocation logs that never double-deduct from daily spending.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale | Alternatives Considered |
|---|---|---|---|
| **Daily Reset Logic** | Dynamic Date-Partitioned Querying | No scheduled midnight cron jobs modifying balances; allowances and balances are calculated on-the-fly from transaction timestamps relative to local date boundaries. Eliminates race conditions and simplifies backdated edits. | Midnight cron reset (Fragile, timezone-dependent, hard to recover from failures). |
| **Numeric Types** | `BigDecimal` (Java/Kotlin) & `DECIMAL(12, 2)` (SQL) | Absolute IEEE 754 precision; zero floating-point rounding errors on currency calculations. | Double / Float (Unacceptable precision drift for financial applications). |
| **Dual-Mode Data Sync** | Local-First Room DB with Idempotent Sync Tokens | Allows sub-second instant expense capture offline; records sync seamlessly via `idempotency_key` to avoid duplicate transaction billing. | Online-only REST (Fails when user is in a basement store or spotty mobile network). |
| **Savings Goal Deductions** | Separate Capital Allocations | Savings contributions are funded from the planned monthly savings bucket or explicit lump sums; they do NOT deduct from today's groceries/travel allowance. | Deducting savings from daily allowance (Distorts daily living allowance). |

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ANDROID MOBILE CLIENT                           │
│                                                                        │
│   ┌───────────────────┐    ┌──────────────────────────────────────┐    │
│   │ Jetpack Compose   │    │  DailyWorth State & ViewModel        │    │
│   │ Material 3 UI     │◄──►│  (StateFlow, Insets, Navigation)     │    │
│   └───────────────────┘    └──────────────────┬───────────────────┘    │
│                                               │                        │
│                            ┌──────────────────┴───────────────────┐    │
│                            │    DailyBudgetEngine / Repository    │    │
│                            └──────────┬───────────────────┬───────┘    │
│                                       │                   │            │
│                     ┌─────────────────┴────┐     ┌────────┴─────────┐  │
│                     │ Room Database (Local)│     │ Retrofit Sync    │  │
│                     │ SQLite DECIMAL via TC│     │ (Idempotent API) │  │
│                     └──────────────────────┘     └────────┬─────────┘  │
└───────────────────────────────────────────────────────────┼────────────┘
                                                            │ HTTPS / JWT
┌───────────────────────────────────────────────────────────┼────────────┐
│                    SPRING BOOT BACKEND REST API           ▼            │
│                                                                        │
│   ┌───────────────────────────────────────────────────────────────┐    │
│   │ Spring Security & JWT Filter (Stateless, User Context)        │    │
│   └───────────────────────────────┬───────────────────────────────┘    │
│                                   │                                    │
│   ┌───────────────┬───────────────┴───┬───────────────┬───────────┐    │
│   │ AuthController│BudgetController   │TransactionCtrl│GoalCtrl   │    │
│   └───────┬───────┴───────┬───────────┴───────┬───────┴─────┬─────┘    │
│           │               │                   │             │          │
│   ┌───────┴───────┬───────┴───────────┬───────┴───────┬─────┴─────┐    │
│   │ AuthService   │BudgetCalculator   │ExpenseService │GoalService│    │
│   └───────┬───────┴───────┬───────────┴───────┬───────┴─────┬─────┘    │
│           │               │                   │             │          │
│   ┌───────┴───────────────┴───────────────────┴─────────────┴─────┐    │
│   │ Spring Data JPA Repositories (Isolated by tenant user_id)     │    │
│   └───────────────────────────────┬───────────────────────────────┘    │
└───────────────────────────────────┼────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        MYSQL 8.0 DATABASE                              │
│   (users, salary_profiles, budget_periods, transactions, goals...)     │
└────────────────────────────────────────────────────────────────────────┘
```

---

### Detailed Entity-Relationship (ER) Model

```
       ┌───────────────────────┐
       │         users         │
       │───────────────────────│
       │ PK  id                │
       │     email (UQ)        │
       │     password_hash     │
       │     full_name         │
       │     currency          │
       │     timezone          │
       │     created_at        │
       └───────────┬───────────┘
                   │ 1
                   │
         ┌─────────┼───────────────────────────┬─────────────────────────┐
         │ 1..N    │ 1..N                      │ 1..N                    │ 1..N
         ▼         ▼                           ▼                         ▼
┌─────────────────┐ ┌───────────────────┐ ┌─────────────────────┐ ┌──────────────────┐
│ salary_profiles │ │  budget_periods   │ │    transactions     │ │  savings_goals   │
│─────────────────│ │───────────────────│ │─────────────────────│ │──────────────────│
│ PK  id          │ │ PK  id            │ │ PK  id              │ │ PK  id           │
│ FK  user_id     │ │ FK  user_id       │ │ FK  user_id         │ │ FK  user_id      │
│     annual_ctc  │ │     start_date    │ │ FK  category_id     │ │     name         │
│     take_home   │ │     end_date      │ │     amount (DEC)    │ │     target_amount│
│     fixed_costs │ │     monthly_budget│ │     payment_method  │ │     saved_amount │
│     savings_targ│ │     daily_allow   │ │     tx_date (DATE)  │ │     target_date  │
│     credit_day  │ │     days_in_month │ │     idempotency_key │ └────────┬─────────┘
│     effective_dt│ └───────────────────┘ │     created_at      │          │ 1
└─────────────────┘                       └─────────────────────┘          │ 1..N
                                                                           ▼
                                                                  ┌──────────────────┐
                                                                  │savings_contribs  │
                                                                  │──────────────────│
                                                                  │ PK  id           │
                                                                  │ FK  goal_id      │
                                                                  │     amount (DEC) │
                                                                  │     created_at   │
                                                                  └──────────────────┘
```

---

### Complete MySQL DDL Scripts

```sql
-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(191) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Salary Profiles (Historical tracking for increments)
CREATE TABLE salary_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    annual_ctc DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    monthly_take_home DECIMAL(12, 2) NOT NULL,
    monthly_fixed_expenses DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    monthly_savings_target DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    salary_credit_day INT NOT NULL DEFAULT 1,
    effective_from DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_salary_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_salary_user_date (user_id, effective_from DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Budget Periods (Monthly snapshots calculated from calendar lengths)
CREATE TABLE budget_periods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_take_home DECIMAL(12, 2) NOT NULL,
    total_fixed_expenses DECIMAL(12, 2) NOT NULL,
    total_savings_target DECIMAL(12, 2) NOT NULL,
    available_spending_budget DECIMAL(12, 2) NOT NULL,
    days_in_period INT NOT NULL,
    calculated_daily_allowance DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_period (user_id, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Expense Categories
CREATE TABLE expense_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL, -- NULL indicates system global default
    name VARCHAR(50) NOT NULL,
    icon_name VARCHAR(50) NOT NULL DEFAULT 'category',
    color_hex VARCHAR(10) NOT NULL DEFAULT '#10B981',
    is_system_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Transactions (Idempotent, Audited Expense Log)
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255) NULL,
    payment_method ENUM('CASH', 'UPI', 'DEBIT_CARD', 'CREDIT_CARD', 'OTHER') NOT NULL DEFAULT 'UPI',
    transaction_date DATE NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES expense_categories(id),
    UNIQUE KEY uq_tx_idempotency (user_id, idempotency_key),
    INDEX idx_tx_user_date (user_id, transaction_date DESC, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Savings Goals & Contributions
CREATE TABLE savings_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    target_amount DECIMAL(12, 2) NOT NULL,
    current_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    target_date DATE NULL,
    description VARCHAR(255) NULL,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'PAUSED') DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE savings_contributions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    notes VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contrib_goal FOREIGN KEY (goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
    CONSTRAINT fk_contrib_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### Spring Boot Project Structure

```
backend/
├── src/main/java/com/dailyworth/
│   ├── DailyWorthApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── WebMvcConfig.java
│   ├── common/
│   │   ├── ApiResponse.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   └── dto/{AuthRequest.java, AuthResponse.java, RegisterRequest.java}
│   ├── salary/
│   │   ├── SalaryController.java
│   │   ├── SalaryService.java
│   │   ├── entity/SalaryProfile.java
│   │   └── repository/SalaryProfileRepository.java
│   ├── budget/
│   │   ├── BudgetController.java
│   │   ├── BudgetCalculator.java
│   │   ├── entity/BudgetPeriod.java
│   │   └── dto/{BudgetOverviewDto.java, DailyAllowanceResponse.java}
│   ├── expense/
│   │   ├── TransactionController.java
│   │   ├── TransactionService.java
│   │   ├── entity/Transaction.java
│   │   └── repository/TransactionRepository.java
│   ├── savings/
│   │   ├── SavingsController.java
│   │   ├── SavingsService.java
│   │   └── entity/{SavingsGoal.java, SavingsContribution.java}
│   └── analytics/
│       ├── AnalyticsController.java
│       └── AnalyticsService.java
└── src/main/resources/
    ├── application.yml
    └── db/migration/V1__init_schema.sql
```

---

### Android Project Structure (Modern Jetpack Compose)

```
app/src/main/java/com/example/
├── MainActivity.kt
├── DailyWorthApp.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── Converters.kt (BigDecimal, LocalDate, PaymentMethod)
│   │   ├── dao/
│   │   │   ├── TransactionDao.kt
│   │   │   ├── SalaryProfileDao.kt
│   │   │   └── SavingsGoalDao.kt
│   │   └── entity/
│   │       ├── TransactionEntity.kt
│   │       ├── SalaryProfileEntity.kt
│   │       └── SavingsGoalEntity.kt
│   ├── model/
│   │   ├── BudgetDailySummary.kt
│   │   ├── Category.kt
│   │   └── PaymentMethod.kt
│   └── repository/
│       ├── DailyBudgetRepository.kt
│       └── ExpenseRepository.kt
├── domain/
│   └── BudgetEngine.kt (Pure BigDecimal math logic)
├── ui/
│   ├── theme/
│   │   ├── Color.kt (Deep Navy, Emerald, Crimson tokens)
│   │   ├── Theme.kt (M3 Dark/Light Scheme)
│   │   └── Type.kt
│   ├── components/
│   │   ├── AllowanceGauge.kt (Smooth radial progress)
│   │   ├── QuickKeypad.kt (Tactile numeric input)
│   │   ├── CategoryPills.kt
│   │   └── TransactionRow.kt
│   ├── screens/
│   │   ├── DashboardScreen.kt (Allowance, spent, quick actions)
│   │   ├── AddExpenseSheet.kt (Sub-5s entry sheet)
│   │   ├── TransactionsScreen.kt (Calendar + date filter)
│   │   ├── AnalyticsScreen.kt (Category breakdown & trends)
│   │   ├── SavingsGoalsScreen.kt (Goal targets & progress)
│   │   └── SalaryConfigScreen.kt (Income, fixed costs, savings target)
│   └── navigation/
│       └── AppNavigation.kt (Bottom navigation bar)
```

---

### REST API Contract Summary

| Endpoint | Method | Request Payload | Response Body | Description |
|---|---|---|---|---|
| `/api/v1/auth/register` | `POST` | `{ email, password, fullName }` | `{ token, user }` | Create tenant user account |
| `/api/v1/auth/login` | `POST` | `{ email, password }` | `{ accessToken, refreshToken }` | Generate JWT credentials |
| `/api/v1/salary/profile` | `POST` | `{ takeHome, fixedExpenses, savingsTarget, creditDay }` | `{ profileId, effectiveFrom }` | Setup or update financial profile |
| `/api/v1/budget/today` | `GET` | *None (Headers: Authorization)* | `{ date, dailyAllowance, spentToday, remaining, overspent, status }` | Core calculation for today |
| `/api/v1/transactions` | `POST` | `{ amount, categoryId, description, paymentMethod, date, idempotencyKey }` | `{ transactionId, newRemainingToday }` | Instant expense logging |
| `/api/v1/transactions` | `GET` | *Query params: `startDate`, `endDate`, `category`* | `List<TransactionDto>` | Filterable ledger |
| `/api/v1/savings/goals` | `GET` / `POST`| `{ name, targetAmount, targetDate }` | `List<SavingsGoalDto>` | Savings progress & target tracker |
| `/api/v1/analytics/monthly` | `GET` | *Query: `month=2026-11`* | `{ totalSpent, avgDaily, topCategory, daysInBudget, daysOverspent }` | Monthly analytics metrics |

---

### Development Milestones

- **Milestone 1**: Foundation & Data Layer — Room schema entities with BigDecimal type converters, pre-seeded default categories, and pure `BudgetEngine` math unit tests.
- **Milestone 2**: Core Dashboard & Real-Time Allowance Engine — Live allowance gauge, remaining vs. overspent indicators, and salary configuration screen.
- **Milestone 3**: Sub-5-Second Expense Entry — Tactile numeric keypad bottom sheet, category selector, and instant reactive deduction.
- **Milestone 4**: Transaction Ledger & Calendar Insights — Date-picker history, category filters, and daily allowance versus spent breakdowns.
- **Milestone 5**: Savings Goals, Analytics, and Visual Polish — M3 Emerald/Crimson fintech theme, progress tracking, and category distribution charts.
