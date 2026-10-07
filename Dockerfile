FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:71c1c716c9ed8a8e71937c9a0f8ea246ab604e2fd2f8de187b56de4a3023427f

ENV APPLICATION_NAME=tilleggsstonader-soknad-api

EXPOSE 8080
COPY --chown=1069:1069 build/libs/app.jar /app.jar

CMD ["-jar", "/app.jar"]
