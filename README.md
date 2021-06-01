# Minimalistic Netty-Packet library

- Create packets with ease
- Bind events to packets

#### Example Packet:
````java
public class TestPacket extends Packet {

    private UUID uuid;

    public TestPacket() {
    }

    @Override
    public void read(PacketBuffer packetBuffer) {
        uuid = packetBuffer.readUUID();
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.writeUUID(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public TestPacket setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}
````

#### Example Server:
````java
public class NettyTestServer extends ChannelInitializer<Channel> {

    private final ServerBootstrap bootstrap;
    private final IPacketRegistry packetRegistry;

    private EventLoopGroup parentGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private Channel connectedChannel;

    public NettyTestServer(IPacketRegistry packetRegistry, Consumer<Future<? super Void>> doneCallback) {
        this.packetRegistry = packetRegistry;
        this.bootstrap = new ServerBootstrap()
                .option(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .group(parentGroup, workerGroup)
                .childHandler(this)
                .channel(NioServerSocketChannel.class);

        try {
            this.bootstrap.bind(new InetSocketAddress("127.0.0.1", 1234))
                    .awaitUninterruptibly().sync().addListener(doneCallback::accept);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast(new PacketDecoder(packetRegistry), new PacketEncoder(packetRegistry));
        this.connectedChannel = channel;
        this.connectedChannel.writeAndFlush(new TestPacket().setUuid(UUID.randomUUID()));
    }

    public void shutdown() {
        try {
            parentGroup.shutdownGracefully().get();
            workerGroup.shutdownGracefully().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
````

#### Example Client:
````java
public class NettyTestClient extends ChannelInitializer<Channel> {

    private final Bootstrap bootstrap;
    private final IPacketRegistry packetRegistry;
    private final EventRegistry eventRegistry;

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public NettyTestClient(IPacketRegistry packetRegistry, Consumer<Future<? super Void>> doneCallback, EventRegistry eventRegistry) {
        this.packetRegistry = packetRegistry;
        this.eventRegistry = eventRegistry;
        this.bootstrap = new Bootstrap()
                .option(ChannelOption.AUTO_READ, true)
                .group(workerGroup)
                .handler(this)
                .channel(NioSocketChannel.class);

        try {
            this.bootstrap.connect(new InetSocketAddress("127.0.0.1", 1234))
                    .awaitUninterruptibly().sync().addListener(doneCallback::accept);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        // Add the PacketChannelInboundHandler if you want to use the event functionality
        channel.pipeline()
                .addLast(new PacketDecoder(packetRegistry), new PacketEncoder(packetRegistry), new PacketChannelInboundHandler(eventRegistry));
    }

    public void shutdown() {
        try {
            workerGroup.shutdownGracefully().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

}
````

#### Example Packet and Event Registration
````java
// Instantiate the required registries
EventRegistry eventRegistry = new EventRegistry();
IPacketRegistry registry = new SimplePacketRegistry();

// Register a packet with the id 1
registry.registerPacket(1, TestPacket.class);

// Register a PacketSubscriber for the registered packet
// Normally this would have been made in an external class to ensure a better readability
eventRegistry.registerEvents(new Object() {
    // The method signature of a PacketSubscriber must contain a valid packet and may contain the ChannelHandlerContext (optional)
    @PacketSubscriber
    public void onPacketReceive(TestPacket packet, ChannelHandlerContext ctx) {
        System.out.println("Received " + packet.getUuid().toString() + " from " + ctx.channel().remoteAddress().toString());
    }
});
````