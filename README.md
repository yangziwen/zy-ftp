## zy-ftp
基于netty实现的ftp服务器。

#### 特性
* 实现了ftp服务端常用的大部分命令，详情[请见代码](https://github.com/yangziwen/zy-ftp/tree/master/src/main/java/io/github/yangziwen/zyftp/command/impl)
* 支持配置用户访问文件权限、文件传输限速等，详情[请见配置](https://github.com/yangziwen/zy-ftp/blob/master/conf/server.config)
* 不加密的传输方式下支持主动、被动模式
* 加密的传输方式下(FTP over TLS)，仅支持被动模式
* 运行于docker环境时，仅支持被动模式

#### 打包 & 运行
* 内嵌在程序中运行
    1. 引入依赖
    ```xml
    <dependency>
        <groupId>io.github.yangziwen</groupId>
        <artifactId>zy-ftp</artifactId>
        <version>0.0.2</version>
    </dependency>
    ```
    2. 启动服务
    ```java
    FtpRunner runner = FtpRunner.builder()
        .localIp("127.0.0.1")
        .localPort(8121)
        .configFile(new File("conf/server.config"))
        .logFile(new File("log/zy-ftp.log"))
        .build();

    runner.run();
    ```
* 基于jar包运行
    1. 打包代码：`mvn package -Pstandalone` or `sh gradlew build`
    2. 启动服务：`java -jar zy-ftp.jar -c ${config_file_path}`
* 基于docker运行
    1. 制作镜像：`mvn package dockerfile:build -Pstandalone` or `sh gradlew dockerBuild`
    2. 启动容器：
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
* 查看启动参数：`java -jar zy-ftp.jar -h`
* 可通过设置系统变量进行连接泄露检测，如`-Dio.netty.leakDetectionLevel=ADVANCED`
