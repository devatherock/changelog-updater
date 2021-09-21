FROM adoptopenjdk/openjdk11-openj9:jre-11.0.8_10_openj9-0.21.0-alpine

USER root

COPY entry-point.sh /scripts/

RUN apk --update add git openssh

COPY build/libs/changelog-updater-*-all.jar /scripts/changelog-updater.jar

CMD ["/bin/sh", "/scripts/entry-point.sh"]