FROM openjdk:8-jre-alpine
WORKDIR /zy-ftp
ENV LOCAL_IP=0.0.0.0 LOCAL_PORT=21 PASSIVE_ADDRESS=127.0.0.1 PASSIVE_PORTS=40000-40060
COPY target/zy-ftp.jar zy-ftp.jar
CMD ["sh", "-c", "java -jar zy-ftp.jar -c server.config -l zy-ftp.log --passive-address $PASSIVE_ADDRESS --passive-ports $PASSIVE_PORTS --local-ip $LOCAL_IP --local-port $LOCAL_PORT"]