package xunshan.foo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource({ "classpath*:sofa-rpc-config.xml" })
@SpringBootApplication
public class ModuleFooApp {
    public static void main(String[] args) {
        SpringApplication.run(ModuleFooApp.class, args);
    }
}
