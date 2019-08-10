package xunshan.bar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import xunshan.foo.HelloService;

@SpringBootApplication
@ImportResource("classpath*:sofa-rpc-client-config.xml")
public class RpcBoltClientSpring {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RpcBoltClientSpring.class);
        ConfigurableApplicationContext ctx = app.run(args);

        HelloService helloServiceReference = (HelloService) ctx.getBean("helloServiceReference");
        String resp = helloServiceReference.sayHello("Andy");
        System.out.println("resp:" + resp);
    }
}
