FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:e37747674d269d1e20b062f222ca019ca55b47588e32e22b7ee2eaf0a5910b47

ENV APPLICATION_NAME=tilleggsstonader-soknad-api

EXPOSE 8080
COPY --chown=1069:1069 build/libs/app.jar /app.jar

CMD ["-jar", "/app.jar"]
