#!/usr/bin/env bash
set -ev  # Needed in order for Travis CI to report failure if publish.sh fails!

if [ $TRAVIS_BRANCH == "develop" ] && [ $TRAVIS_EVENT_TYPE == "push" ]
then
  sbt publish;  # snapshot releases published when new content merged into develop
elif [ $TRAVIS_BRANCH == "master" ] && [ $TRAVIS_EVENT_TYPE == "push" ]
then

  sbt publishSigned sonatypeRelease;  # official releases published when new content merged into master
else
  echo "No snapshot or official releases published for this build!"
fi