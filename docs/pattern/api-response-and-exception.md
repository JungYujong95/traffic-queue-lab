# API Response and Exception Pattern

## Decision

Common API responses and exception handling live under the `global` package.

```text
global
├── exception
│   ├── BusinessException.java
│   ├── ErrorCode.java
│   ├── ErrorResponse.java
│   ├── FieldErrorDetail.java
│   └── GlobalExceptionHandler.java
└── response
    └── ApiResponse.java
```

## Response Format

Successful responses use `ApiResponse<T>`.

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

Error responses use `ErrorResponse`.

```json
{
  "success": false,
  "code": "COMMON_400_001",
  "message": "요청 값이 올바르지 않습니다.",
  "errors": []
}
```

## Exception Rule

Business code should throw `BusinessException` with an `ErrorCode` instead of throwing raw `RuntimeException`.

`ErrorCode` owns the HTTP status, error code, and user-facing message. This keeps exception responses consistent and avoids scattering hardcoded messages across controllers or services.

DB connection acquisition timeouts are returned as `DB_503_001`.
The handler checks Spring JDBC, transaction creation, and Hibernate JDBC connection exception chains so Hikari connection timeout failures do not fall back to the generic `COMMON_500_001` response.
