import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LoadBalanceTest {
	private final Random random = new Random();

	private int getWeight(String candidate) {
		return 100;
	}

	// com.alipay.sofa.rpc.client.lb.RandomLoadBalancer
	@Test
	public void random() {
		List<String> providerInfos = Arrays.asList(
				"a", "b", "c", "d", "e"
		);

		String providerInfo = null;
		int size = providerInfos.size(); // 总个数
		int totalWeight = 0;             // 总权重
		boolean isWeightSame = true;     // 权重是否都一样

		for (int i = 0; i < size; i++) {
			int weight = getWeight(providerInfos.get(i));
			totalWeight += weight;       // 累计总权重
			if (isWeightSame && i > 0 && weight != getWeight(providerInfos.get(i - 1))) {
				isWeightSame = false;    // 计算所有权重是否一样
			}
		}

		if (totalWeight > 0 && !isWeightSame) {
			// 如果权重不相同且权重大于0则按总权重数随机
			int offset = random.nextInt(totalWeight);
			// 并确定随机值落在哪个片断上
			for (int i = 0; i < size; i++) {
				offset -= getWeight(providerInfos.get(i));
				if (offset < 0) {
					providerInfo = providerInfos.get(i);
					break;
				}
			}
		} else {
			// 如果权重相同或权重为0则均等随机
			providerInfo = providerInfos.get(random.nextInt(size));
		}

		System.out.println(providerInfo);
	}
}
