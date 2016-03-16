# NettyRpc
An RPC framework based on Netty, ZooKeeper and Spring  
中文详情：[Chinese Details](http://www.cnblogs.com/luxiaoxun/p/5272384.html)
### NettyRpc-1.0
1) RPC Client send request with short connection by netty.  
2) RPC Client will wait until it gets response.
#### How to use
1. Define an interface:
    	public interface HelloService { 
			String hello(String name); 
			String hello(Person person);
		}
2. Implement the interface with annotation @RpcService:
		@RpcService(HelloService.class)
		public class HelloServiceImpl implements HelloService {
			@Override
			public String hello(String name) {
				return "Hello! " + name;
			}

			@Override
			public String hello(Person person) {
				return "Hello! " + person.getFirstName() + " " + person.getLastName();
			}
		}
3. Run the server with zookeeper
		RpcBootstrap
4. Run the client:
		ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
		final RpcClient rpcClient = new RpcClient(serviceDiscovery);
		HelloService helloService = rpcClient.create(HelloService.class);
		String result = helloService.hello("World");
