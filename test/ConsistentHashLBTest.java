import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistentHashLBTest {
	private long hash(byte[] digest, int index) {
		long f =  ((long) (digest[3 + index * 4] & 0xFF) << 24)
				| ((long) (digest[2 + index * 4] & 0xFF) << 16)
				| ((long) (digest[1 + index * 4] & 0xFF) << 8)
				|         (digest[index * 4]     & 0xFF);
		return f & 0xFFFFFFFFL;
	}

	private byte[] messageDigest(String value) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			// md5.reset();
			md5.update(value.getBytes("UTF-8"));
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No such algorithm named md5", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported encoding of" + value, e);
		}
	}

	@Test
	public void cstHashTest() {
		for (int i = 0; i < 4; i++) {
			System.out.println(hash(messageDigest("a"), i));
		}
	}

	public class ConsistentHashLoadBalancer {

		/**
		 * {interface#method : selector}
		 */
		private ConcurrentHashMap<String, Selector> selectorCache = new ConcurrentHashMap<String, Selector>();
		
		public String doSelect() {
			List<String> Strings = Arrays.asList("a", "b");
			
			String interfaceId = "a";
			String method = "m";
			String key = interfaceId + "#" + method;
			int hashcode = Strings.hashCode(); // 判断是否同样的服务列表
			Selector selector = selectorCache.get(key);
			if (selector == null // 原来没有
					||
					selector.getHashCode() != hashcode) { // 或者服务列表已经变化
				selector = new Selector(interfaceId, method, Strings, hashcode);
				selectorCache.put(key, selector);
			}
			return selector.select(new String[] {"a"});
		}

		/**
		 * 选择器
		 */
		private class Selector {

			/**
			 * The Hashcode.
			 */
			private final int                         hashcode;

			/**
			 * The Interface id.
			 */
			private final String                      interfaceId;

			/**
			 * The Method name.
			 */
			private final String                      method;

			/**
			 * 虚拟节点
			 */
			private final TreeMap<Long, String> virtualNodes;

			/**
			 * Instantiates a new Selector.
			 *
			 * @param interfaceId the interface id
			 * @param method      the method
			 * @param actualNodes the actual nodes
			 */
			public Selector(String interfaceId, String method, List<String> actualNodes) {
				this(interfaceId, method, actualNodes, actualNodes.hashCode());
			}

			/**
			 * Instantiates a new Selector.
			 *
			 * @param interfaceId the interface id
			 * @param method      the method
			 * @param actualNodes the actual nodes
			 * @param hashcode    the hashcode
			 */
			public Selector(String interfaceId, String method, List<String> actualNodes, int hashcode) {
				this.interfaceId = interfaceId;
				this.method = method;
				this.hashcode = hashcode;
				// 创建虚拟节点环 （默认一个provider共创建128个虚拟节点，较多比较均匀）
				this.virtualNodes = new TreeMap<>();
				int num = 128;
				for (String s : actualNodes) {
					for (int i = 0; i < num / 4; i++) {
						byte[] digest = messageDigest(s + i);
						for (int h = 0; h < 4; h++) {
							long m = hash(digest, h);
							virtualNodes.put(m, s);
						}
					}
				}
			}

			public String select(Object[] args) {
				String key = buildKeyOfHash(args);
				byte[] digest = messageDigest(key);
				return selectForKey(hash(digest, 0));
			}

			/**
			 * 获取第一参数作为hash的key
			 *
			 * @param args the args
			 * @return the string
			 */
			private String buildKeyOfHash(Object[] args) {
				if (args == null || args.length == 0) {
					return "";
				}
				else {
					return args[0].toString();
				}
			}

			/**
			 * Select for key.
			 *
			 * @param hash the hash
			 * @return the provider
			 */
			private String selectForKey(long hash) {
				Map.Entry<Long, String> entry = virtualNodes.ceilingEntry(hash);
				if (entry == null) {
					entry = virtualNodes.firstEntry();
				}
				return entry.getValue();
			}

			/**
			 * 换算法？ MD5  SHA-1 MurMurHash???
			 *
			 * @param value the value
			 * @return the byte [ ]
			 */
			private byte[] messageDigest(String value) {
				MessageDigest md5;
				try {
					md5 = MessageDigest.getInstance("MD5");
					// md5.reset();
					md5.update(value.getBytes("UTF-8"));
					return md5.digest();
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException("No such algorithm named md5", e);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unsupported encoding of" + value, e);
				}
			}

			/**
			 * Hash long.
			 *
			 * @param digest the digest
			 * @param index  the number
			 * @return the long
			 */
			private long hash(byte[] digest, int index) {
				long f = ((long) (digest[3 + index * 4] & 0xFF) << 24)
						| ((long) (digest[2 + index * 4] & 0xFF) << 16)
						| ((long) (digest[1 + index * 4] & 0xFF) << 8)
						| (digest[index * 4] & 0xFF);
				return f & 0xFFFFFFFFL;
			}

			/**
			 * Gets hash code.
			 *
			 * @return the hash code
			 */
			public int getHashCode() {
				return hashcode;
			}
		}
	}
}
