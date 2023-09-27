set -e 

echo -e "\n\n\tRunning Maven Goal: $MAVEN_GOAL\n"
mvn --update-snapshots --batch-mode --show-version -Dgpg.skip -Dmaven.javadoc.skip=true \
    -Prres-deploy --settings ci-build/maven-settings.xml $MAVEN_BUILD_ARGS $MAVEN_GOAL
