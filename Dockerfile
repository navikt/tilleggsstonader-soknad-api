FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:4a56270a912390af9d238944557a733ce6fa718bac623941b4f1fa9f18e9ed2c

ENV APPLICATION_NAME=tilleggsstonader-soknad-api

EXPOSE 8080
COPY --chown=1069:1069 build/libs/app.jar /app.jar

CMD ["-jar", "/app.jar"]
