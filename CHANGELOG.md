# Changelog

## [Unreleased]
### Added
- Dependency check plugin
- [#2](https://github.com/devatherock/changelog-updater/issues/2): Unit tests

### Changed
- Fixed `EmptyStackException` when writing a new entry type to the very first release
- Fixed a bug in executing commands
- Used Java 17 for builds
- Upgraded spotless to `6.19.0`
- Updated dockerhub readme in CI pipeline
- Upgraded gradle to `7.6.2`

### Removed
- Redundant empty line when inserting a subheader above an existing subheader

## [0.1.0] - 2022-05-19
### Added
- Initial version. Adds PR title as changelog entry