#!/usr/bin/env bash

if [ $TRAVIS_BRANCH == "develop" ] && [ $TRAVIS_EVENT_TYPE == "cron" ]; then

  sbt 'set credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_PASSWORD"))' publish

fi
