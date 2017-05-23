#!/usr/bin/env bash
set -e  # Needed in order for Travis CI to report failure if publish.sh fails!

if [ $TRAVIS_BRANCH == "develop" ] && [ $TRAVIS_EVENT_TYPE == "push" ]; then

  sbt 'set credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))' publish

fi
