# Changelog

## [Unreleased]
### Added
- Dependency check plugin
- Unit tests for `ChangelogUpdaterHelper`
- Unit test for `GithubService`

### Changed
- Fixed `EmptyStackException` when writing a new entry type to the very first release
- Fixed a bug in executing commands

### Removed
- Redundant empty line when inserting a subheader above an existing subheader

## [0.1.0] - 2022-05-19
### Added
- Initial version. Adds PR title as changelog entry