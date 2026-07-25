
---

## PAYIN SERVICE (`PayinServiceClient`)

**Base URL:** `${acko.payin-service.url}`

### 1. Create Order / Create Payment Order

**Endpoint:** `POST /payments/order-details-ekey`

**Purpose:** Submit a payin order and generate an encrypted key (ekey) for payment processing

**Request DTO: `CreateOrderCentralRequest`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `amount` | Float | ✅ | Payment amount in decimal format (e.g., 1000.50) |
| `client_reference_id` | String | ✅ | Unique reference ID from the client system (used for idempotency and tracking) |
| `customer_id` | Long | ✅ | Unique identifier of the customer/user making the payment |
| `customer_phone` | String | ✅ | Phone number of the customer (e.g., +91XXXXXXXXXX) |
| `customer_email` | String | ✅ | Email address of the customer (fallback: "ecare@acko.tech") |
| `lob` | String | ✅ | Line of Business (e.g., "motor", "health", "electronics") |
| `app` | String | ✅ | Application identifier (e.g., "Acko.Desktop", "Acko.Mobile") |
| `timestamp` | Long | ✅ | Unix timestamp in milliseconds when the order is created |
| `return_url_client` | String | ✅ | Callback URL where the customer is redirected after payment completion |
| `recurring` | Boolean | ❌ | Flag indicating if this is a recurring payment (defaults to false) |
| `context` | Map<String, String> | ❌ | Additional metadata or context data (key-value pairs) |

**Response DTO: `CreateOrderCentralResponse`**

| Field | Type | Description |
|-------|------|-------------|
| `ekey` | String | Encrypted order key - unique identifier for this payment order (use this for further operations) |

**HTTP Response:** `ResponseEntity<CreateOrderCentralResponse>`

**Status Codes:**
- `200 OK` - Order created successfully
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Authentication failed
- `500 Internal Server Error` - Server error

**Error Handling:** Throws `OrderCreationFailedException` on failure. Implement retry logic with exponential backoff (recommended: 3 retries with 2s, 4s, 8s delays).

---

### 2. Verify Payment (V1)

**Endpoint:** `GET /payments/{orderId}/verify`

**Purpose:** Sync and verify the status of a payin order (legacy version)

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `orderId` | String | The order ID or ekey returned from createOrder |

**Response DTO: `PayinVerificationCentralResponse`**

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Internal payment transaction ID |
| `app` | String | Application identifier |
| `lob` | String | Line of Business |
| `client_id` | String | Client/Partner identifier |
| `client_reference_id` | String | Original reference ID from create order request |
| `customer_id` | String | Customer identifier |
| `customer_email` | String | Customer email address |
| `customer_phone` | String | Customer phone number |
| `amount` | Float | Payment amount |
| `currency` | String | Currency code (e.g., "INR") |
| `pg` | String | Payment Gateway used (e.g., "razorpay", "juspay") |
| `pg_token` | String | Payment gateway authentication token |
| `pg_payment_id` | String | Payment ID from the gateway |
| `pg_response` | String | Full response from payment gateway (JSON string) |
| `status` | PaymentStatus | Payment status enum (e.g., PENDING, SUCCESS, FAILED, REFUNDED) |
| `webhook_status` | String | Status of webhook delivery |
| `payment_method` | String | Payment method used (e.g., "card", "netbanking", "upi") |
| `webhook_event_id` | String | ID of the webhook event |
| `created_on` | String | ISO 8601 timestamp of order creation |
| `updated_on` | String | ISO 8601 timestamp of last update |
| `return_url_client` | String | Client callback URL |
| `webhook_event_name` | String | Name of the webhook event |

**Status Codes:**
- `200 OK` - Payment verified successfully
- `400 Bad Request` - Invalid order ID
- `404 Not Found` - Order not found
- `500 Internal Server Error` - Server error

---

### 3. Verify Payment (V2)

**Endpoint:** `GET /payments/{orderId}/verify/v2`

**Purpose:** Sync and verify the status of a payin order (enhanced version with refund details)

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `orderId` | String | The order ID or ekey returned from createOrder |

**Response DTO: `PayinVerificationCentralResponseV2`**

| Field | Type | Description |
|-------|------|-------------|
| `event_name` | String | Event name associated with the payment (e.g., "payment.success", "payment.failed") |
| `date_created` | String | ISO 8601 timestamp of event creation |
| `order_object` | OrderObject | Nested object containing detailed order information (see below) |

**OrderObject Structure:**

| Field | Type | Description |
|-------|------|-------------|
| `customer_phone` | String | Customer phone number |
| `customer_id` | String | Customer identifier |
| `user_ekey` | String | Encrypted user key |
| `customer_email` | String | Customer email address |
| `order_id` | String | Order/payment ID |
| `journey` | String | Payment journey type (e.g., "new_customer", "renewal") |
| `app` | String | Application identifier |
| `pg_payment_id` | String | Payment gateway transaction ID |
| `status` | String | Payment status (e.g., "captured", "failed", "pending") |
| `amount` | String | Payment amount as string |
| `refunds` | List<Refund> | Array of refund objects (see below) |
| `payouts_for_refunds` | List<Object> | Array of payout objects for refunds |
| `payment_method_type` | String | Type of payment method (e.g., "card", "upi") |
| `payment_method` | String | Specific payment method details |
| `payment_created` | String | ISO 8601 timestamp of payment creation |

**Refund Object Structure:**

| Field | Type | Description |
|-------|------|-------------|
| `unique_request_id` | String | Unique refund request identifier |
| `ref` | String | Refund reference number |
| `amount` | Double | Refund amount |
| `created` | String | ISO 8601 timestamp of refund creation |
| `status` | String | Refund status (e.g., "pending", "success", "failed") |
| `error_message` | String | Error message if refund failed |
| `initiated_by` | String | User/system that initiated the refund |

**Status Codes:**
- `200 OK` - Payment verified successfully
- `400 Bad Request` - Invalid order ID
- `404 Not Found` - Order not found
- `500 Internal Server Error` - Server error

---

### 4. Create Refund

**Endpoint:** `POST /refund/create`

**Purpose:** Create a refund intent for an existing payment order

**Request DTO: `CreateRefundRequest`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `order_id` | String | ✅ | The order ID or ekey from createOrder response |
| `amount` | Float | ✅ | Refund amount (should be ≤ original payment amount) |

**Response DTO: `CreateRefundResponse`**

| Field | Type | Description |
|-------|------|-------------|
| `refund_id` | String | Unique identifier for the created refund (use for initiateRefund) |

**HTTP Response:** `ResponseEntity<CreateRefundResponse>`

**Status Codes:**
- `200 OK` - Refund created successfully
- `400 Bad Request` - Invalid order ID or amount
- `404 Not Found` - Order not found
- `422 Unprocessable Entity` - Refund amount exceeds available balance
- `500 Internal Server Error` - Server error

---

### 5. Initiate Refund

**Endpoint:** `POST /refund/initiate`

**Purpose:** Submit/process the refund that was created via createRefund

**Request DTO: `InitiateRefundRequest`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `client_id` | String | ❌ | Client identifier (optional, for audit trail) |
| `payment_id` | String | ❌ | Original payment ID (optional) |
| `pg` | String | ❌ | Payment gateway identifier (optional, e.g., "razorpay") |
| `amount` | Float | ❌ | Refund amount (optional, defaults to created amount) |
| `refund_id` | String | ✅ | Refund ID from createRefund response |

**Response DTO: `InitiateRefundResponse`**

| Field | Type | Description |
|-------|------|-------------|
| `refund_id` | String | Refund identifier |
| `payment_id` | String | Original payment ID |
| `amount` | Float | Refund amount processed |
| `pg_token` | String | Payment gateway authentication token |
| `pg_payment_id` | String | Payment gateway transaction ID |
| `status` | String | Refund status (e.g., "initiated", "processed", "failed") |

**HTTP Response:** `ResponseEntity<InitiateRefundResponse>`

**Status Codes:**
- `200 OK` - Refund initiated successfully
- `400 Bad Request` - Invalid refund ID or parameters
- `404 Not Found` - Refund not found
- `409 Conflict` - Refund already processed
- `500 Internal Server Error` - Server error

**Error Handling:** Throws `RefundInitiationFailedException` on failure. Implement retry logic with exponential backoff.

---

## PAYOUT SERVICE (`PayoutServiceClient`)

**Base URL:** `${acko.payout-service.url}`

### 1. Generate Payout Request ID

**Endpoint:** `POST /api/generate_payout_request_id`

**Purpose:** Allocate and generate a unique platform payout request ID

**Request:** Empty body

**Response DTO: `PayOutIdResponseDTO`**

| Field | Type | Description |
|-------|------|-------------|
| `payout_request_id` | String | Unique payout request identifier (use for subsequent payout operations) |

**HTTP Response:** `ResponseEntity<PayOutIdResponseDTO>`

**Status Codes:**
- `200 OK` - Payout request ID generated successfully
- `500 Internal Server Error` - Server error

---

### 2. Validate Account Details (Pre-flight)

**Endpoint:** `POST /api/validate/account_details`

**Purpose:** Pre-flight validation of beneficiary bank account details before initiating payout

**Request DTO: `AccountValidationRequestDTO`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account_number` | String | ✅ | Bank account number of the beneficiary |
| `ifsc_code` | String | ✅ | IFSC code of the bank branch |
| `account_holder_name` | String | ✅ | Name of the account holder |
| `account_type` | String | ❌ | Type of account (e.g., "savings", "current") |
| `entity_id` | String | ❌ | Beneficiary entity identifier |

**Response DTO: `AccountValidationResponseDTO`**

| Field | Type | Description |
|-------|------|-------------|
| `validation_status` | String | Validation result (e.g., "valid", "invalid", "retry") |
| `message` | String | Detailed validation message |
| `account_holder_name` | String | Validated account holder name (if available) |
| `validation_source` | String | Source of validation (e.g., "nbin", "manual") |

**HTTP Response:** `ResponseEntity<AccountValidationResponseDTO>`

**Status Codes:**
- `200 OK` - Validation completed (may be valid or invalid)
- `400 Bad Request` - Missing required fields
- `422 Unprocessable Entity` - Invalid account details format
- `500 Internal Server Error` - Server error

**Error Handling:** Implement retry logic for 5xx errors. On validation failure, log details for manual review.

---

### 3. Verify IFSC Code

**Endpoint:** `GET /api/ifsc-verify?ifsc={ifsc}`

**Purpose:** Validate IFSC (Indian Financial System Code) code

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `ifsc` | String | ✅ | 11-character IFSC code (format: FIRST0000001) |

**Response DTO: `IfscVerificationResponseDTO`**

| Field | Type | Description |
|-------|------|-------------|
| `ifsc_code` | String | The validated IFSC code |
| `bank_name` | String | Name of the bank |
| `branch_name` | String | Name of the branch |
| `is_valid` | Boolean | Whether the IFSC is valid |
| `error_message` | String | Error message if invalid |

**HTTP Response:** `ResponseEntity<IfscVerificationResponseDTO>`

**Status Codes:**
- `200 OK` - IFSC verification completed
- `400 Bad Request` - Invalid IFSC format
- `404 Not Found` - IFSC not found in database
- `500 Internal Server Error` - Server error

---

### 4. Initiate Payout

**Endpoint:** `POST /api/v2/initiate_payout`

**Purpose:** Submit a payout request to transfer funds to beneficiary

**Request DTO: `InitiatePayoutRequestDTO`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `okind` | String | ✅ | Origin kind/source identifier (e.g., "jarvis", "artemis", "claim-management") |
| `oid` | Long/String | ✅ | Origin ID (e.g., claim ID, policy ID, order ID from source system) |
| `payment_type` | String | ✅ | Type of payment (e.g., "claim", "refund", "settlement") |
| `amount` | BigDecimal | ✅ | Payout amount in decimal format |
| `requested_by_id` | String | ✅ | User/system ID requesting the payout |
| `entity_type` | String | ✅ | Type of beneficiary entity (e.g., "customer", "vendor", "advisor") |
| `entity_id` | String | ✅ | Unique identifier of the beneficiary entity |
| `entity_subtype` | String | ❌ | Sub-type of entity (e.g., "individual", "business") |
| `callback_url` | String | ✅ | Webhook URL for payout status notifications |
| `payment_mode` | String | ✅ | Payment mode (e.g., "neft", "rtgs", "imps", "bank_transfer") |
| `payment_instrument` | PaymentInstrument | ✅ | Beneficiary bank account details (see below) |
| `parent_payment_id` | Long | ❌ | Parent payment ID for tracking refund chains |
| `payout_request_id` | String | ❌ | Previously generated payout request ID (if updating) |
| `unique_id` | String | ❌ | Unique identifier for idempotency |

**PaymentInstrument Structure:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account_number` | String | ✅ | Beneficiary bank account number |
| `ifsc_code` | String | ✅ | IFSC code of the bank branch |
| `account_holder_name` | String | ✅ | Name of the account holder |
| `account_type` | String | ❌ | Account type (e.g., "savings", "current") |
| `beneficiary_id` | String | ❌ | Unique ID of the beneficiary |

**Response DTO: `InitiatePayoutResponseDTO`**

| Field | Type | Description |
|-------|------|-------------|
| `payout_request_id` | String | Payout request identifier |
| `status` | String | Payout status (e.g., "payout_initiated", "created", "initiated") |
| `amount` | BigDecimal | Payout amount |
| `request_id` | String | Request identifier |
| `verification` | VerificationDetails | Verification results (see below) |
| `validation` | ValidationDetails | Validation results (see below) |
| `lob` | String | Line of Business |
| `journey` | String | Journey type |
| `reference_id` | String | Reference identifier |
| `redirection_url` | String | URL for redirect (if applicable) |

**VerificationDetails Structure:**

| Field | Type | Description |
|-------|------|-------------|
| `payee_name` | String | Verified payee/account holder name |
| `verified_name` | String | Name verified through NBIN/validation service |
| `result` | String | Verification result (e.g., "success", "failed", "mismatch") |

**ValidationDetails Structure:**

| Field | Type | Description |
|-------|------|-------------|
| `result` | String | Validation result (e.g., "success", "failed") |
| `message` | String | Validation message |

**HTTP Response:** `ResponseEntity<InitiatePayoutResponseDTO>`

**Status Codes:**
- `200 OK` - Payout initiated successfully
- `400 Bad Request` - Invalid request parameters
- `401 Unauthorized` - Authentication failed
- `422 Unprocessable Entity` - Invalid transition or state
- `500 Internal Server Error` - Server error

**Error Handling:** Throws `InvalidTransitionException` for invalid state transitions. Implement retry logic with exponential backoff (recommended: 3 retries with 2s, 4s, 8s delays).

---

### 5. Update Payout Details

**Endpoint:** `POST /api/v2/update_payout_details`

**Purpose:** Update payout details after creation (e.g., change amount or beneficiary details)

**Request DTO: `UpdatePayoutRequestDTO`**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | Long | ✅ | Payout request ID to update |
| `amount` | BigDecimal | ✅ | Updated payout amount |
| `payment_instrument` | PaymentInstrument | ✅ | Updated beneficiary bank account details |
| `requested_by_id` | String | ✅ | User/system ID requesting the update |
| `payment_mode` | String | ✅ | Updated payment mode |
| `callback_url` | String | ✅ | Updated webhook URL for notifications |

**Response DTO: `UpdatePayoutResponseDTO`**

| Field | Type | Description |
|-------|------|-------------|
| `payout_request_id` | String | Payout request identifier |
| `status` | String | Updated payout status |
| `amount` | BigDecimal | Updated payout amount |
| `message` | String | Update confirmation message |

**HTTP Response:** `ResponseEntity<UpdatePayoutResponseDTO>`

**Status Codes:**
- `200 OK` - Payout details updated successfully
- `400 Bad Request` - Invalid request parameters
- `404 Not Found` - Payout request not found
- `409 Conflict` - Cannot update in current state
- `500 Internal Server Error` - Server error

---

### 6. Verify/Check Payout Status

**Endpoint:** `GET /api/{payout_request_id}/verify`

**Purpose:** Sync payout status and verification details

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `payout_request_id` | String | The payout request ID from initiate response |

**Response DTO: `PayoutStatusDTO`**

| Field | Type | Description |
|-------|------|-------------|
| `payout_request_id` | String | Payout request identifier |
| `status` | String | Current payout status (e.g., "created", "initiated", "success", "failed", "pending") |
| `amount` | BigDecimal | Payout amount |
| `gateway_transaction_id` | String | Transaction ID from payment gateway |
| `gateway_response` | String | Response from payment gateway |
| `error_message` | String | Error message if payout failed |
| `created_at` | String | ISO 8601 timestamp of creation |
| `updated_at` | String | ISO 8601 timestamp of last update |
| `verification_status` | String | Account verification status |
| `verification_message` | String | Verification details message |

**HTTP Response:** `ResponseEntity<PayoutStatusDTO>`

**Status Codes:**
- `200 OK` - Status retrieved successfully
- `400 Bad Request` - Invalid payout request ID
- `404 Not Found` - Payout request not found
- `500 Internal Server Error` - Server error

---

## SDK Implementation Recommendations

### Authentication & Authorization
- Implement **OAuth 2.0** or **API Key** based authentication based on Acko's requirements
- Add bearer token management with automatic refresh
- Include request signing for sensitive operations

### Retry Strategy
```
Max Retries: 3
Backoff Strategy: Exponential (2s, 4s, 8s)
Idempotency: Use unique_id or client_reference_id to prevent duplicate processing
Timeout: 30 seconds per request
```

### Error Handling
- Implement specific exception classes for each error type
- Log detailed error messages with trace IDs for debugging
- Provide user-friendly error messages to clients

### Rate Limiting
- Implement client-side rate limiting (requests/minute)
- Respect server rate limit headers (X-RateLimit-*)
- Queue requests during rate limit windows

### Request/Response Logging
- Log all requests with sanitized sensitive data (mask card numbers, account numbers)
- Include correlation IDs for distributed tracing
- Store logs for compliance and audit purposes

### Webhook Management
- Implement webhook signature verification
- Store webhook payloads for reconciliation
- Implement retry logic for webhook processing failures

---