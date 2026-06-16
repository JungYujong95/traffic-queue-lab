# Global Exception Handling Architecture

## Overview

`GlobalExceptionHandler` centralizes API exception responses for controllers.

It handles:

- business exceptions
- request validation failures
- unreadable JSON request bodies
- missing request parameters
- request parameter type mismatches
- missing static or API resources
- unexpected exceptions

## Flow

```text
Controller
  -> throws BusinessException or framework exception
  -> GlobalExceptionHandler
  -> ErrorResponse
```

## Trade-off

The initial error code set is intentionally small and common-focused. Domain-specific error codes should be added when domain rules are implemented, not before.
