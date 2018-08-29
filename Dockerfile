FROM openjdk:8-jre-alpine
WORKDIR /usr/src/myapp
ENV PASSIVE_ADDRESS=127.0.0.1 PASSIVE_PORTS=40000-40060
COPY target/zy-ftp.jar zy-ftp.jar
CMD ["sh", "-c", "java -jar zy-ftp.jar -c server.config -l zy-ftp.log --passive-address $PASSIVE_ADDRESS --passive-ports $PASSIVE_PORTS"]
EXPOSE 8021
