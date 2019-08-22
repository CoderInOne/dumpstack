package xunshan;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class ZookeeperTest {
    private CuratorFramework client;
    private final static String ROOT = "/zk-test/hello";

    @Before
    public void setUp() throws Exception {
        client = createClient();

        client.start();

        // clean workspace
        if (client.checkExists().forPath(ROOT) != null) {
            List<String> children = client.getChildren().forPath(ROOT);

            System.out.println("children:" + children);
            for (String c : children) {
                client.delete().forPath(ROOT + "/" + c);
            }

            client.delete().forPath(ROOT);
        }
    }

    private CuratorFramework createClient() {
        return CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .connectionTimeoutMs(1000)
                .sessionTimeoutMs(1000)
                .canBeReadOnly(false)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .defaultData(null)
                .build();
    }

    @Test
    public void createNode() throws Exception {
        String path = "/zk-test/hello/a";
        byte[] data = "hi".getBytes();

        String resp = client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path, data);

        assertEquals(path, resp);
        assertArrayEquals(data, client.getData().forPath(path));
    }

    @Test
    public void listenChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // add and delete node
                System.out.println("create node");
                try {
                    String resp = client.create().creatingParentContainersIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(ROOT + "/a", "hi".getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000);

        // listen changes
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, ROOT, false);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println("new path event:" + event.getType() +
                        ", data:" + event.getData() +
                        ", initialData:" + event.getInitialData());

                latch.countDown();
            }
        });
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        latch.await();
        pathChildrenCache.clear();
    }

    /* -*- create mode -*- */

    @Test
    public void ephermeralMode() throws Exception {
        String path = ROOT + "/c";
        String resp = client.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);

        System.out.println("created node " + resp);

        assertNotNull(client.checkExists().forPath(path));

        client.close();

        System.out.println("close current client");

        client = createClient();
        client.start();
        assertNull(client.checkExists().forPath(path));

        System.out.println("ephermeral node deleted after client close");
    }

    @Test
    public void ephermeralSequentialMode() throws Exception {
        String path = ROOT + "/d";
        String resp = client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path);

        System.out.println("resp:" + resp);

        client.close();

        client = createClient();
        client.start();

        resp = client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path);

        System.out.println("resp:" + resp);
    }

    @Test
    public void znodeVersionUpdate() throws Exception {
        final String path = ROOT + "/e";
        client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);

        client.getCuratorListenable().addListener(new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println("path:" + event.getPath() + ", version:" + event.getStat().getVersion());
            }
        });

        changeData(path, 3);
    }

    private void changeData(final String path, final int cnt) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(cnt);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < cnt; i++) {
                    try {
                        System.out.println("updating " + path);

                        client.setData().inBackground().forPath(path, String.valueOf(cnt).getBytes());

                        Thread.sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    latch.countDown();
                }
            }
        }).start();

        latch.await();
    }

    @Test
    public void watchNode() throws Exception {
        String path = ROOT + "/f";

        client.create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path);

        client.getData()
                .usingWatcher(new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        System.out.println("path:" + event.getPath() + ", type:" + event.getType());
                    }
                })
                .inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println("path:" + event.getPath() +
                                ", version:" + event.getStat().getVersion() +
                                ", cxid:" + event.getStat().getCzxid() +
                                ", dataLength:" + event.getStat().getDataLength()
                        );
                    }
                })
                .forPath(path);

        changeData(path, 3);
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }
}
