#!/usr/bin/env bash

SLUG="jaredsburrows/gradle-license-plugin"
BRANCH="master"

set -e

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping snapshot deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping snapshot deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  echo "Skipping snapshot deployment: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "Deploying snapshot..."
  ./gradlew artifactoryPublish publishPlugins -Dgradle.publish.key=$GRADLE_KEY -Dgradle.publish.secret=$GRADLE_SECRET
  echo "Snapshot deployed!"
fi
