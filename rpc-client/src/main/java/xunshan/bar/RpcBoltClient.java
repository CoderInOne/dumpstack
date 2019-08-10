package xunshan.bar;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import xunshan.foo.HelloService;

@SpringBootApplication
public class RpcBoltClient {
    public static void main(String[] args) {
//        RegistryConfig registryConfig = new RegistryConfig()
//                .setProtocol("zookeeper")
//                .setAddress("127.0.0.1:2181");

        ConsumerConfig<HelloService> consumerConfig = new ConsumerConfig<HelloService>()
                .setInterfaceId(HelloService.class.getName())
//                .setRegistry(registryConfig)
                .setProtocol("bolt")
				.setDirectUrl("bolt://127.0.0.1:12201");

        HelloService refer = consumerConfig.refer();
        String resp = refer.sayHello("andy");
        System.out.println("resp:" + resp);
    }
}
