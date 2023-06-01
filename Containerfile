FROM registry.access.redhat.com/ubi8/openjdk-11:latest
ENV TZ="Europe/Vienna"

USER jboss

COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

ARG BRANCH
ARG COMMIT
ARG VERSION
# ARG TIMESTAMP
ENV BRANCH=$BRANCH
# ENV TIMESTAMP=$TIMESTAMP
ENV COMMIT=$COMMIT
ENV VERSION=$VERSION

EXPOSE 8080
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"