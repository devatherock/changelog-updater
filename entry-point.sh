#!/bin/sh

PR_BRANCH=master
if [ ! -z "$VELA_BUILD_BRANCH" ]; then
  PR_BRANCH=$VELA_BUILD_BRANCH
elif [ ! -z "$DRONE_SOURCE_BRANCH" ]; then
  PR_BRANCH=$DRONE_SOURCE_BRANCH
elif [ ! -z "$CIRCLE_BRANCH" ]; then
  PR_BRANCH=$CIRCLE_BRANCH
fi

if [ ! -z "$VELA_REPO_NAME" ]; then
  REPO_NAME=$VELA_REPO_NAME
elif [ ! -z "$DRONE_REPO_NAME" ]; then
  REPO_NAME=$DRONE_REPO_NAME
elif [ ! -z "$CIRCLE_PROJECT_REPONAME" ]; then
  REPO_NAME=$CIRCLE_PROJECT_REPONAME
fi

if [ ! -z "$VELA_REPO_ORG" ]; then
  REPO_ORG=$VELA_REPO_ORG
elif [ ! -z "$DRONE_REPO_OWNER" ]; then
  REPO_ORG=$DRONE_REPO_OWNER
elif [ ! -z "$CIRCLE_PROJECT_USERNAME" ]; then
  REPO_ORG=$CIRCLE_PROJECT_USERNAME  
fi

if [ ! -z "$VELA_BUILD_AUTHOR" ]; then
  USERNAME=$VELA_BUILD_AUTHOR
elif [ ! -z "$DRONE_COMMIT_AUTHOR" ]; then
  USERNAME=$DRONE_COMMIT_AUTHOR
elif [ ! -z "$CIRCLE_USERNAME" ]; then
  USERNAME=$CIRCLE_USERNAME  
fi

if [ ! -z "$VELA_PULL_REQUEST" ]; then
  PULL_REQUEST=$VELA_PULL_REQUEST
elif [ ! -z "$DRONE_PULL_REQUEST" ]; then
  PULL_REQUEST=$DRONE_PULL_REQUEST
elif [ ! -z "$CIRCLE_PULL_REQUEST" ]; then
  PULL_REQUEST=$(echo "$CIRCLE_PULL_REQUEST" | cut -d'/' -f 7)
fi

if [ ! -z "$VELA_BUILD_AUTHOR_EMAIL" ]; then
  EMAIL=$VELA_BUILD_AUTHOR_EMAIL
elif [ ! -z "$DRONE_COMMIT_AUTHOR_EMAIL" ]; then
  EMAIL=$DRONE_COMMIT_AUTHOR_EMAIL
fi

BUILD_EVENT=pull_request
if [ ! -z "$VELA_BUILD_EVENT" ]; then
  BUILD_EVENT=$VELA_BUILD_EVENT
elif [ ! -z "$DRONE_BUILD_EVENT" ]; then
  BUILD_EVENT=$DRONE_BUILD_EVENT
fi

ENABLE_DEBUG=false
if [ "$PLUGIN_DEBUG" = "true" ] || [ "$PARAMETER_DEBUG" = "true" ]; then
  ENABLE_DEBUG=true
fi

ALL_OPTS="-e $BUILD_EVENT -t $CHANGELOG_UPDATER_GIT_TOKEN -pb $PR_BRANCH"

if [ "$ENABLE_DEBUG" = "true" ]; then
  ALL_OPTS="$ALL_OPTS -d"
fi

if [ ! -z "$REPO_ORG" ]; then
   ALL_OPTS="$ALL_OPTS -o $REPO_ORG"
fi

if [ ! -z "$REPO_NAME" ]; then
   ALL_OPTS="$ALL_OPTS -r $REPO_NAME"
fi

if [ ! -z "$PULL_REQUEST" ]; then
   ALL_OPTS="$ALL_OPTS -p $PULL_REQUEST"
fi

if [ ! -z "$USERNAME" ]; then
   ALL_OPTS="$ALL_OPTS -un $USERNAME"
fi

if [ ! -z "$EMAIL" ]; then
   ALL_OPTS="$ALL_OPTS -ue $EMAIL"
fi

java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar changelog-updater.jar $ALL_OPTS