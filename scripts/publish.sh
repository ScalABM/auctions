#!/usr/bin/env bash

#if [ $TRAVIS_BRANCH == "develop" && $TRAVIS_EVENT_TYPE == "cron" ]; then
if [ $TRAVIS_BRANCH == "develop" ]; then

  sbt 'set credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USERNAME"), System.getenv("SONATYPE_USERNAME"))' publish

fi
