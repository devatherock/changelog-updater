[![CircleCI](https://circleci.com/gh/devatherock/changelog-updater.svg?style=svg)](https://circleci.com/gh/devatherock/changelog-updater)
[![Version](https://img.shields.io/docker/v/devatherock/changelog-updater?sort=semver)](https://hub.docker.com/r/devatherock/changelog-updater/)
[![Coverage Status](https://coveralls.io/repos/github/devatherock/changelog-updater/badge.svg?branch=master)](https://coveralls.io/github/devatherock/changelog-updater?branch=master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=changelog-updater&metric=alert_status)](https://sonarcloud.io/component_measures?id=changelog-updater&metric=alert_status&view=list)
[![Docker Pulls](https://img.shields.io/docker/pulls/devatherock/changelog-updater.svg)](https://hub.docker.com/r/devatherock/changelog-updater/)
[![Docker Image Size](https://img.shields.io/docker/image-size/devatherock/changelog-updater.svg?sort=date)](https://hub.docker.com/r/devatherock/changelog-updater/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
# changelog-updater
CI plugin to add a changelog entry, if it is missing, from pull request title

## Usage
### Docker

```shell script
docker run --rm \
  -e CHANGELOG_UPDATER_GIT_TOKEN=sometoken \
  -e PARAMETER_DEBUG=true \
  -e CI=true \
  -e VELA_BUILD_BRANCH=somebranch \
  -e VELA_REPO_NAME=repo \
  -e VELA_REPO_ORG=org \
  -e VELA_BUILD_AUTHOR=devatherock \
  -e VELA_BUILD_AUTHOR_EMAIL=devatherock@gmail.com \
  -e CIRCLE_PULL_REQUEST=https://github.com/org/repo/pull/1 \
  -v /path/to/.ssh:/root/.ssh \
  -v /path/to/changelog:/work \
  -w=/work \
  devatherock/changelog-updater:latest
```