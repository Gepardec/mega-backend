FROM openjdk:11.0-jre-slim
ENV TZ="Europe/Vienna"

WORKDIR /work/
RUN chown :root /work \
    && chmod "g+rwX" /work \
    && chown :root /work

COPY target/*-runner.jar /work/application.jar
# COPY target/lib/* /work/lib/

ARG BRANCH
ARG COMMIT
ARG VERSION
# ARG TIMESTAMP
ENV BRANCH=$BRANCH
# ENV TIMESTAMP=$TIMESTAMP
ENV COMMIT=$COMMIT
ENV VERSION=$VERSION

EXPOSE 8080

CMD ["java","-jar","application.jar","-Dquarkus.http.host=0.0.0.0"]

ailed to load config value of type class java.lang.String for: mega.zep.soap-path
Failed to load config value of type java.util.List<java.lang.String> for: mega.mail.reminder.om
Failed to load config value of type boolean for: mega.mail.employees.notification