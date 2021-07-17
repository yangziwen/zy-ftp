package io.github.yangziwen.zyftp.main;

import java.io.File;

import io.github.yangziwen.zyftp.run.FtpRunner;

public class Main {

    private Main() {}

    public static void main(String[] args) throws Exception {
        FtpRunner runner = FtpRunner.builder()
            .localIp("127.0.0.1")
            .localPort(8121)
            .configFile(new File("conf/server.config"))
            .logFile(new File("log/zy-ftp.log"))
            .build();

        runner.run();
    }

}
