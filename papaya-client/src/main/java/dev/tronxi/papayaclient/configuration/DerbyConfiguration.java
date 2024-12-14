package dev.tronxi.papayaclient.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;

@Configuration
public class DerbyConfiguration {

    @Bean
    public DataSource configure() {
        String jarDir = UpnpConfiguration.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();

        if (jarDir.startsWith("nested:")) {
            jarDir = jarDir.substring(7);
        }

        int jarEndIndex = jarDir.indexOf(".jar");
        if (jarEndIndex != -1) {
            jarDir = jarDir.substring(0, jarEndIndex + 4);
        }

        File jarFile = new File(jarDir);
        File parentDir = jarFile.getParentFile();

        System.out.println(parentDir.getAbsolutePath());
        String databaseUrl = "jdbc:derby:" + parentDir.getAbsolutePath() + "/db;create=true";

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        dataSource.setUrl(databaseUrl);

        return dataSource;
    }
}
