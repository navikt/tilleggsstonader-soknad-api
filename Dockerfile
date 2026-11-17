FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:124de5475ac6ecac94e655db5b7cfecf0d5043c36341106ae4c64d8b0c277ad6

ENV APPLICATION_NAME=tilleggsstonader-soknad-api

EXPOSE 8080
COPY --chown=1069:1069 build/libs/app.jar /app.jar

CMD ["-jar", "/app.jar"]
