# acmecorp-billing-sdk Changelog

## [Unreleased]

## [1.4.1] - 2026-08-20

### Changed

- BREAKING: renamed `chargeCustomer()` to `createCharge()` and changed
  its return type from `boolean` to `ChargeResult`. Callers must
  update their integration.

## [1.4.0] - 2026-08-10

### Added

- Added support for partial refunds.

## [1.3.5] - 2026-08-01

### Fixed

- Fixed a rounding error in tax calculation.
