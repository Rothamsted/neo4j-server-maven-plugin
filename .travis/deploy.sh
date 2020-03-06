echo -e "\n--- Preparing the Environment"

if [ ! -z "$GPG_SECRET_KEYS" ]; then echo $GPG_SECRET_KEYS | base64 --decode | $GPG_EXECUTABLE --import; fi
if [ ! -z "$GPG_OWNERTRUST" ]; then echo $GPG_OWNERTRUST | base64 --decode | $GPG_EXECUTABLE --import-ownertrust; fi


echo -e "\n--- Building"

mvn --batch-mode --show-version -Dgpg.skip -Dmaven.javadoc.skip=true \
		--settings .travis/settings.xml install


echo -e "\n--- Integration Tests"

cd integration-tests
mvn verify
cd ..


echo -e "\n--- Deployoment"

if [ ! -z "$TRAVIS_TAG" ]
then
    echo "on a tag -> set pom.xml <version> to $TRAVIS_TAG"
    mvn --settings .travis/settings.xml org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=$TRAVIS_TAG 1>/dev/null 2>/dev/null
else
    echo "not on a tag -> keep snapshot version in pom.xml"
fi

# TODO: restore the M2 Central version later, when we have new credentials
# mvn clean deploy --settings .travis/settings.xml -Prelease -B -U

mvn --update-snapshots --batch-mode -Prres-deploy --settings .travis/settings.xml deploy
