#!/usr/bin/env bash

#if [ $TRAVIS_BRANCH == "develop" && $TRAVIS_EVENT_TYPE == "cron" ]; then
if [ "$(TRAVIS_BRANCH)" == "develop" ]; then

  REALM="Sonatype Nexus Repository Manager"
  HOST="oss.sonatype.org"
  sbt 'set credentials += Credentials("$(REALM)", "$(HOST)", "$(SONATYPE_USERNAME)", "$(SONATYPE_PASSWORD)")' ++$(TRAVIS_SCALA_VERSION) publish

fi
