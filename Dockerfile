FROM eclipse-temurin:17.0.7_7-jre-alpine

USER root

COPY entry-point.sh /scripts/

RUN apk --update add git openssh

COPY build/libs/changelog-updater-*-all.jar /scripts/changelog-updater.jar

CMD ["/bin/sh", "/scripts/entry-point.sh"]