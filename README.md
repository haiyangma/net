# net
the project is based on netty framework from spark
what i have done is to add http framework int the project

You can use it like this :

    @Test
    public void createNewHttpServer() throws Exception{
        Map map = new HashMap();
        map.put("springXml","spring.xml");
        map.put("actionPath","com.mhy.netty.action");
        TransportConf conf = new TransportConf("test",new MapConfigProvider(map));
        TransportContext context = new TransportContext(conf,true);
        TransportServer server =  context.createNewHttpServer(56666);
        server.sync();
    }

    @Test
    public void createRpcServer() throws Exception{
        TransportConf conf = new TransportConf("test",new MapConfigProvider(new HashMap()));
        TransportContext context = new TransportContext(conf,new MyRpcHandler(),true);
        TransportServer server =  context.createRpcServer(56666);
        server.sync();
    }


    @Test
    public void createRpcClient() throws Exception{
        TransportConf conf = new TransportConf("test",new MapConfigProvider(new HashMap()));
        TransportContext context = new TransportContext(conf,new MyRpcHandler(),true);
        TransportClientFactory clientFactory = context.createClientFactory();
        TransportClient client = clientFactory.createClient("10.13.89.31",56666);
        client.sendRpc(ByteBuffer.wrap("nihao".getBytes()), new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
                System.out.println(new String(response.array()));
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println(e.getMessage());
            }
        });
    }
