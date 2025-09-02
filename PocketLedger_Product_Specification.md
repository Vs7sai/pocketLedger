# PocketLedger - Product Specification Document

## Product Overview
PocketLedger is a fast, secure, and offline-first personal finance management app that prioritizes speed, privacy, and local data control. The app focuses on rapid transaction entry, encrypted local storage, and comprehensive financial tracking without cloud dependencies.

## Core Principles
- **Speed First**: Transaction entry in under 1 second
- **Privacy by Design**: Full local encryption, no cloud by default
- **Offline-First**: Complete functionality without internet connection
- **User Control**: Full data ownership and export capabilities

## Feature Requirements (Prioritized)

### 1. Fast Transaction Entry
- Amount-first input with large numeric keyboard
- Quick category selection with auto-suggestions
- Optional receipt photo capture
- Save completion in <1 second
- Account selector integration

### 2. Encrypted Local Storage
- Full database encryption (AES-256)
- Automatic and transparent to user
- No cloud synchronization by default
- Secure key management

### 3. Categories & Auto-suggestions
- Rule-based categorization system
- On-device machine learning suggestions
- Fully editable category management
- Smart pattern recognition

### 4. Budgets & Alerts
- Per-category monthly budget tracking
- Visual progress indicators
- Local threshold notifications
- Budget vs. actual spending analysis

### 5. Reports & Charts
- Monthly summary dashboard
- Category breakdown visualizations
- Trend analysis over time
- Exportable to PNG/CSV formats

### 6. Export / Import (Encrypted)
- Encrypted backup files (.plbk extension)
- CSV import/export functionality
- Secure data portability
- Multi-format support

### 7. Offline-First Behavior
- Full functionality in airplane mode
- No cloud dependencies
- Local data processing
- Seamless offline experience

### 8. Device-to-Device Transfer
- Encrypted multi-QR code export
- Wi-Fi Direct import/export
- Secure peer-to-peer sharing
- Cross-platform compatibility

### 9. CSV Mapping UI
- Flexible column mapping interface
- Support for varied bank CSV formats
- Preview functionality
- Validation and error handling

### 10. Receipt Capture & Compression
- Local photo storage
- Optional on-device OCR
- Image compression for storage efficiency
- Receipt metadata extraction

### 11. Local Rule Sharing
- Import/export category rules via QR
- Small file format for easy sharing
- Community-driven categorization
- Rule versioning

### 12. Lightweight On-device Categorization
- Tiny TensorFlow Lite model
- Offline machine learning
- Handles complex categorization cases
- Minimal resource usage

### 13. Payment Reminders & Notifications
- Set payment due dates for recurring expenses
- Local notifications sent 3 days before due date
- Customizable reminder timing
- Offline notification system
- Reminder history and management

## User Experience & Interface Specifications

### Home / Today Feed
**Design Prompt**: "Show today's balance and a timeline of recent transactions. Primary CTA: a prominent floating '+' for quick add. Minimal chrome, keyboard-first, fast-readable amounts."

**Key Elements**:
- Current balance prominently displayed
- Transaction timeline with clear amounts
- Floating action button for quick add
- Minimal UI elements for focus
- Large, readable transaction amounts

### Quick Add Modal
**Design Prompt**: "Amount-first input (large numeric keyboard), auto-suggest category below amount, account selector, optional note and photo thumbnail, single Save button. Default focus on amount."

**Key Elements**:
- Large numeric keypad as primary input
- Category suggestions below amount field
- Account selection dropdown
- Optional note and photo fields
- Single save button for speed
- Auto-focus on amount field

### Accounts & Balances Screen
**Design Prompt**: "List accounts with balances, swipe-to-edit, quick transfer button. Show total net at top and account-type icons (cash/card)."

**Key Elements**:
- Account list with current balances
- Swipe gestures for editing
- Quick transfer functionality
- Net worth calculation
- Visual account type indicators

### Categories Picker
**Design Prompt**: "Grid of colored chips with search; frequently used categories shown first and 'Manage categories' entry at bottom."

**Key Elements**:
- Color-coded category chips
- Search functionality
- Frequency-based ordering
- Category management access
- Visual category identification

### Budgets Screen
**Design Prompt**: "List monthly budgets with progress bars and %; each budget expands to show transactions in that category and an Edit button."

**Key Elements**:
- Monthly budget overview
- Visual progress indicators
- Percentage completion display
- Expandable transaction details
- Quick edit access

### Reports Screen
**Design Prompt**: "Top: monthly donut chart (tap slices to drill into category). Below: timeline graph for income vs expense and a compact export button."

**Key Elements**:
- Interactive donut chart
- Category drill-down capability
- Income vs. expense timeline
- Export functionality
- Visual data representation

### CSV Import Flow
**Design Prompt**: "Step 1: choose file. Step 2: preview first 5 rows. Step 3: map columns with simple dropdowns (Date, Amount, Desc). Step 4: Validate and Import with progress indicator."

**Key Elements**:
- 4-step guided process
- File selection interface
- Data preview functionality
- Column mapping dropdowns
- Validation and progress tracking

### Export / Encrypted Backup
**Design Prompt**: "Single 'Export backup' action with optional passphrase field, show file-size estimate and QR generation toggle for multi-QR export."

**Key Elements**:
- Single export action
- Optional passphrase protection
- File size estimation
- QR code generation toggle
- Multi-QR export support

### Transfer / QR Import
**Design Prompt**: "Camera scanner UI with clear instruction: 'Scan all QR parts in order.' Show progress dots for parts scanned and a reassemble status bar."

**Key Elements**:
- Camera scanner interface
- Clear scanning instructions
- Progress tracking dots
- Reassembly status indicator
- Multi-part QR support

### Receipt Detail
**Design Prompt**: "Full-screen receipt view with zoom; metadata strip on top (merchant/date/amount); link back to transaction."

**Key Elements**:
- Full-screen receipt display
- Zoom functionality
- Metadata information strip
- Transaction linking
- High-resolution viewing

### Settings & Privacy
**Design Prompt**: "Clean page with bold statement: 'No cloud. Data stays on this device.' Controls for Export, Import, CSV, and optional analytics toggle (off by default)."

**Key Elements**:
- Clear privacy statement
- Export/import controls
- CSV settings
- Analytics toggle (default: off)
- Clean, minimal interface

### Payment Reminders
**Design Prompt**: "List upcoming payments with due dates, reminder status, and quick actions. Show 'Add Reminder' button prominently. Each reminder displays days remaining and allows quick edit or mark as paid."

**Key Elements**:
- Upcoming payments timeline
- Days remaining countdown
- Reminder status indicators
- Quick edit and mark-as-paid actions
- Add reminder button
- Reminder history view

## Microcopy & User Guidance

### Empty States
- **Home empty**: "No transactions yet — tap + to add your first expense."
- **CSV import empty**: "No recognizable columns — try a different file."
- **Export success**: "Backup saved locally. You can scan or share it."
- **Reminders empty**: "No payment reminders set — tap + to add your first reminder."

### Error & Progress Indicators
- **Success toasts**: "✔ [message]" (one-line format)
- **Warning toasts**: "! [message]" (one-line format)
- **Error toasts**: "✗ [message]" (one-line format)
- **Long tasks**: Full-width progress bar with cancel option

## Technical Requirements

### Performance Targets
- Transaction entry: <1 second
- App launch: <2 seconds
- Report generation: <3 seconds
- Export operations: <10 seconds

### Security Requirements
- AES-256 encryption for local database
- Secure key derivation
- No plaintext data storage
- Encrypted backup files

### Storage Requirements
- Efficient image compression
- Local database optimization
- Minimal app footprint
- Scalable data structure

## Success Metrics
- User transaction entry speed
- App crash rate
- Data export success rate
- User privacy satisfaction
- Offline functionality reliability

---

*This specification serves as a comprehensive guide for product development, design implementation, and technical architecture decisions for PocketLedger.*
