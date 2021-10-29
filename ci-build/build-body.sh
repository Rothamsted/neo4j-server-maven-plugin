set -e 

echo -e "\n\n\Running Maven Goal: $MAVEN_GOAL\n"
mvn --update-snapshots --batch-mode --show-version -Dgpg.skip -Dmaven.javadoc.skip=true \
    -Prres-deploy --settings ci-build/maven-settings.xml $MAVEN_ARGS $MAVEN_GOAL


return

# TODO: remove, this is now auto-executed as module.

echo -e "\n\n\tIntegration Tests\n"
cd integration-tests
mvn verify
cd ..

if [[ "$MAVEN_GOAL" == "deploy" ]]; then
	echo -e "\n\n\tDeployment\n"
	mvn --update-snapshots --batch-mode -Prres-deploy \
	    --settings ci-build/maven-settings.xml $MAVEN_ARGS deploy
fi
