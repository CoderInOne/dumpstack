package xunshan.foo;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class RpcBoltServer {
    public static void main(String[] args) {
//        RegistryConfig registryConfig = new RegistryConfig()
//                .setProtocol("zookeeper")
//                .setAddress("127.0.0.1:2181");

        ServerConfig serverConfig = new ServerConfig()
                .setProtocol("bolt")
                .setPort(12201)
                .setDaemon(false);

        ProviderConfig<HelloService> helloServiceProviderConfig = new ProviderConfig<HelloService>()
                .setInterfaceId(HelloService.class.getName())
                .setRef(new HelloServiceImpl())
				.setFilterRef(Arrays.asList((Filter) new HelloFilter()))
//                .setRegistry(registryConfig)
                .setServer(serverConfig);

        helloServiceProviderConfig.export();
    }
}
