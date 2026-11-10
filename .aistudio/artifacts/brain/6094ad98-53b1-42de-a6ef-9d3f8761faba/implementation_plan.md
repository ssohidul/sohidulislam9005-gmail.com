# POS Billing Interface with Product Search, Cart Management & Tax Calculation

A modern, high-speed Point of Sale (POS) billing system for Bangladeshi retail and wholesale businesses in **NSB দোকান সেবা**, featuring instant product search, reactive cart management, configurable VAT/tax calculation, tender change computation, and digital cash memo generation backed by Firestore.

## User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were dismissed, so recommended defaults tailored for Bangladeshi retail businesses have been selected:
> - **VAT / Tax Calculation**: Configurable tax percentage with standard Bangladeshi presets (0%, 5% retail VAT, 7.5%, 15% standard) and custom rate input, with transparent subtotal, tax amount, and net grand total calculation.
> - **Payment Tender**: Cash, Mobile Financial Services (bKash, Nagad, Rocket), and Due Khata (বাকি খাতা) with automatic change return (ফেরত টাকা) calculation.
> - **Receipt / Cash Memo**: Printable and shareable digital Bengali cash memo with shop branding, itemized VAT breakdown, and customer due balance.

- **Confirmed Decision 1**: Store tax rates (`taxRate` and `taxAmount`) directly on the `Sale` entity in Firestore to maintain accurate historical records for audits and financial summaries.
- **Confirmed Decision 2**: Interactive Cart Drawer / Bottom Sheet with real-time quantity adjustments, custom pricing, quick-clear cart, and live subtotal calculation.

---

## 1. Overview & Core Concept

- **What It Does**: Enables shopkeepers, cashiers, and managers to search products rapidly by name or barcode, adjust cart quantities, apply itemized discounts and statutory VAT/tax, assign customers or walk-in buyers, collect payments through multiple channels, and complete sales transactions with automated stock deduction and invoice generation.
- **Target Audience**: Bangladeshi grocery, electronics, pharmacy, clothing, and retail shop owners who need frictionless checkout on mobile and tablet devices.
- **Key Value**: Speeds up customer checkout time, prevents manual calculation mistakes, automatically keeps inventory accurate, and transparently records sales tax/VAT.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ 1. Search & Pick│ ───►  │ 2. Cart Manage  │ ───►  │ 3. Tax & Tender │ ───►  │ 4. Memo & Sync  │
│ Type/scan items │       │ Adjust qty, see │       │ Select 5% VAT,  │       │ Auto stock drop,│
│ 1-tap add cart  │       │ items & subtotal│       │ Cash/bKash/Due  │       │ Print/Share memo│
└─────────────────┘       └─────────────────┘       └─────────────────┘       └─────────────────┘
```

1. **Product Discovery**:
   - Header with active search bar (name, category, or barcode) and clear button.
   - Category chips ("সব", "মুদি", "স্টেশনারি", etc.) with instant live filtering.
   - Product cards display real-time stock levels, retail price, low-stock warning chips, and reactive `+` / `-` quantity steppers when added to cart.
2. **Persistent Cart Banner & Management**:
   - Bottom floating cart summary showing total items and running total in Bangladeshi Taka (`৳`).
   - Tapping expands the comprehensive Cart Management sheet:
     - Itemized list with thumbnail icon, product title, unit price, quantity steppers, item total, and swipe/button to delete.
     - Quick "কার্ট খালি করুন (Clear Cart)" button with safety confirmation.
3. **Tax & Payment Settlement Modal**:
   - **Customer Selector**: Choose walk-in customer or select existing contact from the Dues Book (showing past credit balance).
   - **Financial Breakdown**:
     - Subtotal (পণ্যের মোট মূল্য)
     - Discount (ছাড়) with quick presets (`৳10`, `৳20`, `৳50`)
     - Tax / VAT (ভ্যাট ও কর): Preset chips (`0%`, `5% ভ্যাট`, `7.5% ভ্যাট`, `15% ভ্যাট`) + custom percentage field showing the exact calculated tax amount in Taka.
     - Grand Total (সর্বমোট প্রদেয় বিল): Highlighted in bold Dokan emerald green.
   - **Payment Tender**:
     - Payment modes: নগদ (Cash), বিকাশ (bKash), নগদ (Nagad), বাকি (Due Khata).
     - Paid amount field with quick cash buttons (Exact, ৳500, ৳1000).
     - Dynamic change return (ফেরত টাকা) or remaining due (অবশিষ্ট বাকি) chip.
4. **Transaction Completion & Receipt**:
   - 1-tap "ক্যাশমেমো ও সম্পন্ন করুন" button.
   - Displays printable digital Cash Memo dialog with WhatsApp share and PDF/print action.
   - Resets cart and triggers stock deduction across Firestore in real-time.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Tax / VAT Calculation Formula**:
  - *Chosen Approach*: `Tax Amount = (Subtotal - Discount) * (Tax Rate / 100)`. Grand total equals `(Subtotal - Discount) + Tax Amount`.
  - *Why*: In retail commerce, tax is levied on the discounted taxable amount, avoiding overtaxing customers when discounts are granted.
  - *Alternatives Considered*: Tax on raw subtotal before discount was rejected as it violates standard retail tax compliance.
- **Decision 2: Stock Validation during Cart Addition**:
  - *Chosen Approach*: Prevent incrementing cart quantity beyond current available stock, with visual warning badges when stock is exhausted or critically low.
  - *Why*: Prevents accidental overselling and negative inventory in the cloud database.
- **Decision 3: Change Return vs. Due Detection**:
  - *Chosen Approach*: If paid amount exceeds grand total, show green "ফেরত দিন (Change to Return)"; if paid amount is less, automatically register remaining balance as Due if a customer is selected, or prompt user if walk-in.

---

## 4. Technical Architecture & Data Strategy

### System Component Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├───────────────────────────────┬────────────────────────────────────────┤
│ PosBillingScreen              │ CheckoutSheetContent                   │
│ ├─ Product Search & Chips     │ ├─ Customer Selection Dropdown         │
│ ├─ Product Grid / List Cards  │ ├─ Cart Items Review List              │
│ └─ Floating Cart Bottom Bar   │ ├─ Tax / VAT Selector (0/5/7.5/15%)    │
│                               │ ├─ Discount & Tender Payment Mode      │
│                               │ └─ Change Return / Due Calculation     │
└───────────────┬───────────────┴───────────────────┬────────────────────┘
                │                                   │
                ▼                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    DokanViewModel (State Management)                   │
│ ├─ cart: StateFlow<List<CartItem>>                                     │
│ ├─ taxRate: StateFlow<Double> & taxAmount: StateFlow<Double>           │
│ ├─ discount: StateFlow<Double> & paidAmount: StateFlow<Double?>        │
│ ├─ completeSale(onSuccess, onError)                                    │
└───────────────────────────────┬────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   DokanRepository (Firestore Engine)                   │
│ ├─ records Sale document with taxRate, taxAmount, grandTotal           │
│ ├─ decrements stockQuantity on each purchased Product                  │
│ └─ records CustomerTransaction if due balance remains                  │
└────────────────────────────────────────────────────────────────────────┘
```

### Data Model Updates

1. **`Sale` Entity**:
   - `val taxRate: Double = 0.0` (tax percentage applied)
   - `val taxAmount: Double = 0.0` (calculated tax in currency units)
2. **`CartItem` Entity**:
   - Retains `product`, `quantity`, `customPrice`, with computed `total` and `profit`.
3. **`DokanViewModel` State Extensions**:
   - `taxRate: StateFlow<Double>` with setter `setTaxRate(rate: Double)`
   - Computed properties: `subtotal`, `taxAmount`, `grandTotal`, `changeReturnAmount`, `dueAmount`.

---

## 5. Verification & Testing Plan

1. **Robolectric & Unit Tests**:
   - Test sale recording with 5% and 15% VAT calculation.
   - Verify stock reduction matches cart item quantities.
   - Verify proper credit/due balance assignment when paid amount is less than total.
2. **Build & Compilation**:
   - Run `compile_applet` to ensure clean Kotlin compilation without errors.
   - Run `gradle :app:testDebugUnitTest` to verify test suite health.
