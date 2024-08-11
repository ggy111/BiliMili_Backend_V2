package com.bilimili.buaa13.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 程序启动前创建分片目录
 */
@Component
public class StartupRunner implements CommandLineRunner {

    @Value("${directory.chunk}")
    private String Fragment_Directory;

    @Override
    public void run(String... args) throws RuntimeException {
        File fragmentDir = new File(Fragment_Directory);
        if (!fragmentDir.exists()) {
            boolean created = fragmentDir.mkdirs();
            if (!created) {
                throw new RuntimeException("创建分片文件夹失败: " + Fragment_Directory);
            }
        }
    }

    private void RunBegin(String[] arguments) throws RuntimeException {
        File fragmentDir = new File(File.separator);
        for(String argument : arguments) {
            System.out.println(argument);
            File fragmentFile = new File(fragmentDir, argument);
            if (fragmentFile.exists()) {
                System.out.println(fragmentFile.getAbsolutePath());
            }
            else{
                boolean created = fragmentDir.mkdirs();
                if (!created) {
                    throw new RuntimeException("环境初始化失败: " + argument);
                }
            }
        }
    }

    private void RunEnd(String[] arguments) throws RuntimeException {
        File fragmentDir = new File(Fragment_Directory);
        for(String argument : arguments) {
            System.out.println(argument);
            File fragmentFile = new File(fragmentDir, argument);
            if (fragmentFile.exists()) {
                System.out.println(fragmentFile.getAbsolutePath());
                boolean deleted = fragmentFile.delete();
                if (!deleted) {
                    throw new RuntimeException("环境结束失败: " + argument);
                }
            }
        }
    }
}