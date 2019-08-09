package xunshan.foo;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcBoltServer {
    public static void main(String[] args) {
        RegistryConfig registryConfig = new RegistryConfig()
                .setProtocol("zookeeper")
                .setAddress("127.0.0.1:2181");

        ServerConfig serverConfig = new ServerConfig()
                .setProtocol("bolt")
                .setPort(12201)
                .setDaemon(false);

        ProviderConfig<HelloService> helloServiceProviderConfig = new ProviderConfig<HelloService>()
                .setInterfaceId(HelloService.class.getName())
                .setRef(new HelloServiceImpl())
                .setRegistry(registryConfig)
                .setServer(serverConfig);

        helloServiceProviderConfig.export();
    }
}
