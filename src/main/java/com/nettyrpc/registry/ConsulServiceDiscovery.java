package com.nettyrpc.registry;

import com.google.common.net.HostAndPort;
import com.nettyrpc.client.ConnectManage;
import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.State;
import com.orbitz.consul.model.health.HealthCheck;
import com.orbitz.consul.model.health.ServiceHealth;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现
 *
 * @author huangyong
 * @author luxiaoxun
 */
public class ConsulServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ConsulServiceDiscovery.class);

    private volatile List<String> dataList = new ArrayList<>();

    private String registryAddress;
    private Consul consul;
    private ServiceHealthCache svHealth ;

    public ConsulServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        consul = connectServer();
        if (consul != null) {
            watchService(consul);
        }
    }

    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                logger.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                logger.debug("using random data: {}", data);
            }
        }
        return data;
    }

    private Consul connectServer() {
        HostAndPort hostAndPort = HostAndPort.fromString(registryAddress);
        return Consul.builder().withHostAndPort(hostAndPort).build();
    }

    private void watchService(final Consul consul) {

        svHealth = ServiceHealthCache.newCache(consul.healthClient(),ConsulServiceRegistry.SERVICEID_NAME);
        svHealth.addListener(new ConsulCache.Listener<ServiceHealthKey, ServiceHealth>() {
            @Override
            public void notify(Map<ServiceHealthKey, ServiceHealth> newValues) {
                System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

                List<ServiceHealth> list = consul.healthClient().getHealthyServiceInstances(ConsulServiceRegistry.SERVICEID_NAME).getResponse();
                //TODO 待完善
                dataList.clear();
                if(null != list && !list.isEmpty()){
                    for (ServiceHealth health : list) {
                        String ip = health.getNode().getAddress();
                        int port = health.getService().getPort();
                        dataList.add(ip+":"+port);
                    }
                }
                UpdateConnectedServer();
            }
        });
        svHealth.start();
    }

    private void UpdateConnectedServer(){
        ConnectManage.getInstance().updateConnectedServer(this.dataList);
    }


}
