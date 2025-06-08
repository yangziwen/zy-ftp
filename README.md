## zy-ftp

[![Java CI with Maven](https://github.com/yangziwen/zy-ftp/actions/workflows/maven.yml/badge.svg)](https://github.com/yangziwen/zy-ftp/actions/workflows/maven.yml)

[Chinese Doc](https://github.com/yangziwen/zy-ftp/blob/master/README_CN.md)
### Introduction
A netty based ftp server

### Feature
* Implements most frequently used ftp commands ([see details](https://github.com/yangziwen/zy-ftp/tree/master/src/main/java/io/github/yangziwen/zyftp/command/impl))
* Support configurations of user access priviledge and file transfer rate limit ([see options](https://github.com/yangziwen/zy-ftp/blob/master/conf/server.config))
* Both active mode and passive mode are supported under cleartext transmission
* Only passive mode is supported when transfer data over TLS
* Only passive mode is supported when running inside a docker container

### Usage
* Running an embedded server
    1. Import the dependency
    ```xml
    <dependency>
        <groupId>io.github.yangziwen</groupId>
        <artifactId>zy-ftp</artifactId>
        <version>0.0.2</version>
    </dependency>
    ```
    2. Start the server
    ```java
    FtpRunner runner = FtpRunner.builder()
        .localIp("127.0.0.1")
        .localPort(8121)
        .configFile(new File("conf/server.config"))
        .logFile(new File("log/zy-ftp.log"))
        .build();

    runner.run();
    ```
* Running the JAR file standalone
    1. Build the JAR file ：`mvn package -Pstandalone` or `sh gradlew build`
    2. Start the server：`java -jar zy-ftp.jar -c ${config_file_path}`
* Running inside a docker container
    1. Build the docker image：`mvn package dockerfile:build -Pstandalone` or `sh gradlew dockerBuild`
    2. Start the container：
    ```
    docker run -d \
      -v ${your_config_file}:/zy-ftp/server.config \
      -v ${your_resource_folder}:/zy-ftp/res \
      -e LOCAL_PORT=8121 \
      -p 8121:8121 \
      -e PASSIVE_PORTS=40000-40060 \
      -p 40000-40060:40000-40060 \
      zy-ftp:0.0.2
    ```
* See more options：`java -jar zy-ftp.jar -h`
