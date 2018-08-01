package com.nettyrpc.registry;

import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableCheck;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.net.HostAndPort;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册
 *
 */
public class ConsulServiceRegistry {

    public final static String CHECKID_PREFIX = "checkId_"; //checkID前缀
    public final static String CHECKNAME_PREFIX = "checkName_"; //check名称前缀
    public final static String SERVICEID_PREFIX = "serviceId_"; //服务ID称前缀
    public final static String SERVICEID_NAME = "rpcService"; //服务ID称前缀



    private static final Logger logger = LoggerFactory.getLogger(ConsulServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ConsulServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data) {

        if (data != null) {
            Consul consul = connectServer();
            if (consul != null) {
                String[] arr = data.split(":");
                String ip = arr[0];
                int port = Integer.parseInt(arr[1]);
                String serviceId = SERVICEID_PREFIX + ip;
                Integer servicePort =  port;
                String checkId = SERVICEID_PREFIX + ip;
                String checkName = SERVICEID_PREFIX + ip;
                String checkApi =  "http://"+ip+":"+port;

                Registration registration = ImmutableRegistration
                        .builder()
                        .port(servicePort)
                        .name(SERVICEID_NAME)
                        .id(serviceId)
                        .build();
                consul.agentClient().register(registration);

                //再给服务注册check信息
//                ImmutableCheck immutableCheck = ImmutableCheck
//                        .builder()
//                        .serviceId(serviceId)
//                        .id(checkId)
//                        .name(checkName)
//                        .http(checkApi).interval("10s").build();
//                consul.agentClient().registerCheck(immutableCheck);


            }
        }
    }

    private Consul connectServer() {
        HostAndPort hostAndPort = HostAndPort.fromString(registryAddress);
        return Consul.builder().withHostAndPort(hostAndPort).build();
    }

}