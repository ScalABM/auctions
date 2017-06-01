#!/usr/bin/env bash
set -e  # Needed in order for Travis CI to report failure if publish.sh fails!

# specify location of encrypted Sonatype credentials
SET_CREDENTIALS=\''set credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))'\'

# snapshot releases should be published whenever new content is merged to develop
if [ $TRAVIS_BRANCH == "develop" ] && [ $TRAVIS_EVENT_TYPE == "push" ]; then
  sbt $SET_CREDENTIALS publish
fi

# official releases should be published whenever new content is merged to master
if [ $TRAVIS_BRANCH == "master" ] && [ $TRAVIS_EVENT_TYPE == "push" ]; then
  sbt $SET_CREDENTIALS publishSigned sonatypeRelease
fi