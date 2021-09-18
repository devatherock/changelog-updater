FROM adoptopenjdk/openjdk11-openj9:jre-11.0.8_10_openj9-0.21.0-alpine

COPY entry-point.sh /scripts/
COPY build/libs/changelog-updater-*-all.jar /scripts/changelog-updater.jar

ENTRYPOINT ["/bin/sh", "/scripts/entry-point.sh"]