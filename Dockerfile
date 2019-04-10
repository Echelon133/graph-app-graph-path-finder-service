FROM adoptopenjdk/openjdk11:latest
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
EXPOSE 8097
ENTRYPOINT ["java","-cp","app:app/lib/*","ml.echelon133.services.graphpathfinder.GraphPathFinderApp"]