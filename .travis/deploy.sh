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
