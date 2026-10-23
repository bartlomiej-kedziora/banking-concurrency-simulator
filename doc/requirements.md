# Banking Concurrency Simulator — Requirements

## 1. Overview

### 1.1 Purpose

The purpose of this project is to simulate banking operations performed concurrently by multiple threads and to demonstrate thread safety using Java synchronization mechanisms.

### 1.2 Version

- **Target version:** 1.0.0
- **Synchronization mechanism:** `synchronized`
- **Language:** Java
- **Build tool:** Maven

## 2. Scope

### 2.1 In Scope

<!-- Describe the features included in version 1.0.0. -->

### 2.2 Out of Scope

<!-- Describe features intentionally excluded from version 1.0.0. -->

## 3. Actors

<!-- Identify the external actors interacting with the system. -->

## 4. Functional Requirements

<!-- Describe what the system must do. Use FR-001, FR-002, etc. -->

### FR-001 — Account Creation

**Description:**

The system must support the creation of bank accounts with a unique account number, an initial balance of zero, and no authorized overdraft by default.

**Acceptance Criteria:**
- [ ] Each newly created account has an initial balance of 0.00 PLN.
- [ ] Each newly created account has an authorized overdraft limit of 0.00 PLN.
- [ ] Each account is assigned a unique account number.
- [ ] Multiple accounts can be created without account number collisions.

### FR-002 — Joint Account Ownership

**Description:**

The system must support bank accounts owned by one or more customers. All account owners share access to the same account balance and authorized overdraft limit.

**Acceptance Criteria:**
- [ ] Each bank account has at least one owner.
- [ ] A bank account can have multiple owners.
- [ ] Every account owner can perform authorized banking operations.
- [ ] Operations performed by different owners affect the same account balance.
- [ ] Concurrent operations performed by different owners must preserve account consistency.

### FR-003 — Transaction History

**Description:**

The system must maintain a transaction history for each bank account, including information about the account owner who initiated each operation.

**Acceptance Criteria:**
- [ ] Each transaction record contains a unique transaction identifier.
- [ ] Each transaction record contains the transaction type, amount, date and time, status, and initiating account owner.
- [ ] All account owners can view the transaction history of their shared account.
- [ ] Concurrent transactions do not result in missing or corrupted transaction records.

### FR-004 — Transaction Audit Log

**Description:**

The system must maintain an internal audit log of banking operation attempts, including successful and rejected transactions. Audit records must not be exposed through customer-facing interfaces.

**Acceptance Criteria:**
- [ ] Successful transaction attempts are recorded.
- [ ] Rejected transaction attempts are recorded with a rejection reason.
- [ ] Each audit record identifies the initiating account owner, account, operation type, amount, timestamp, and outcome.
- [ ] Audit records are not included in customer-visible transaction history.
- [ ] Concurrent operations do not cause audit records to be lost or corrupted.


## 5. Business Rules

<!-- Describe domain rules and restrictions. Use BR-001, BR-002, etc. -->

### BR-001 — Withdrawal Validation

**Description:**

A withdrawal must be rejected if the requested amount exceeds the account's available funds, including its authorized overdraft limit.

**Acceptance Criteria:**
- [ ] A withdrawal within the available funds is accepted.
- [ ] A withdrawal exceeding the available funds is rejected.
- [ ] A rejected withdrawal does not modify the account balance.

### BR-002 — Daily Transaction Limits

**Description:**

Each bank account must have configurable daily transaction limits defined separately for each supported transaction type. These limits are shared by all account owners.

**Acceptance Criteria:**
- [ ] Each supported transaction type has an independent daily limit.
- [ ] Transaction limits apply to the bank account, not individual owners.
- [ ] Successful transactions contribute to the daily total for their transaction type.
- [ ] A transaction exceeding the remaining daily limit is rejected.
- [ ] Rejected transactions do not reduce the remaining daily limit.
- [ ] Daily transaction totals reset at the beginning of a new banking day.
- [ ] Concurrent transactions cannot exceed their respective daily limits.

**Additional Acceptance Criteria:**
- [ ] A transaction exceeding the remaining daily limit is rejected in full.
- [ ] The system must never automatically reduce the requested transaction amount.
- [ ] A rejected transaction does not modify the account balance or daily transaction totals.
- [ ] Daily transaction limits are calculated per calendar day using the `Europe/Warsaw` time zone.
- [ ] Daily transaction totals reset at midnight in the bank's time zone.
- [ ] Transactions performed after midnight use the new day's limits.
- [ ] Transactions performed before midnight count toward the previous day's limits.

### BR-003 — Transaction Idempotency

**Description:**

The system must ensure that repeated processing of the same transaction confirmation does not cause duplicate financial effects.

**Acceptance Criteria:**
- [ ] Each transaction has a unique identifier.
- [ ] A reserved transaction can be completed only once.
- [ ] Repeated confirmation of a completed transaction does not modify the account balance.
- [ ] A failed transaction cannot be completed without an explicitly supported recovery process.
- [ ] Concurrent confirmations of the same transaction cannot result in duplicate balance updates.
troch

## 6. Non-Functional Requirements

<!-- Describe quality attributes and technical constraints. Use NFR-001, NFR-002, etc. -->

### NFR-001 — Thread Safety

**Description:**

Concurrent banking operations must preserve account balance consistency and enforce all applicable business rules.

**Acceptance Criteria:**
- [ ] Concurrent operations cannot cause the authorized overdraft limit to be exceeded.
- [ ] Concurrent balance updates do not result in lost updates.

## 7. Assumptions and Constraints

<!-- Document assumptions and implementation constraints. -->

## 8. Open Questions

<!-- Record unresolved decisions that require further analysis. -->

## 9. References

<!-- Add relative links to architecture documents, diagrams, and other relevant files. -->
