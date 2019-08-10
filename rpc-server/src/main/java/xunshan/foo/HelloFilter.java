package xunshan.foo;

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.filter.Filter;
import com.alipay.sofa.rpc.filter.FilterInvoker;

@Extension("customer")
@AutoActive(providerSide = true, consumerSide = true)
public class HelloFilter extends Filter {
	@Override
	public SofaResponse invoke(FilterInvoker invoker, SofaRequest request) throws SofaRpcException {
		System.out.println("HelloFilter filtering " + request.toString());
		return invoker.invoke(request);
	}
}
