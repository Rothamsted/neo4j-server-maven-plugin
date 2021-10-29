set -e 

echo -e "\n\n\tBuild\n"

mvn --batch-mode --show-version -Dgpg.skip -Dmaven.javadoc.skip=true \
		--settings ci-build/maven-settings.xml install

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
