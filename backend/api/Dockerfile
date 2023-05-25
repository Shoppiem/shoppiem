FROM azul/zulu-openjdk:16
ENV SPRING_APPLICATION_NAME=shoppiem
ENV SPRING_PROFILES_ACTIVE=test
ENV MR_GH_USERNAME=bmelesse
ENV MR_GH_TOKEN=xyz
EXPOSE 8080
COPY build/libs/app.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
