package com.network.mocket.channel.manager;

import com.network.mocket.channel.ChannelCommons;
import com.network.mocket.helper.Pair;
import com.network.mocket.packet.AckPacket;
import com.network.mocket.packet.IPacket;
import com.network.mocket.packet.PacketManager;
import com.network.mocket.packet.PacketType;
import com.network.mocket.parser.ByteBufferToPackets;
import com.network.mocket.utils.TestUtils;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@PrepareForTest(ByteBufferToPackets.class)
@RunWith(PowerMockRunner.class)
public class ClientChannelManagerTest {

  private ClientChannelManager clientChannelManager;

  private PacketManager packetManager;
  private ChannelCommons channelCommons;
  private SocketAddress socketAddress;
  private ScheduledExecutorService scheduledExecutorService;


  @Before
  public void setUp() throws Exception {
    packetManager = createNiceMock(PacketManager.class);
    channelCommons = createNiceMock(ChannelCommons.class);
    socketAddress = createNiceMock(SocketAddress.class);
    scheduledExecutorService = createNiceMock(ScheduledExecutorService.class);
    expect(scheduledExecutorService
        .scheduleWithFixedDelay(anyObject(Runnable.class), anyLong(), anyLong(), anyObject
            (TimeUnit.class))
    ).andReturn(null).times(2);
    replay(scheduledExecutorService);

    clientChannelManager = new ClientChannelManager(
        packetManager,
        channelCommons,
        socketAddress,
        scheduledExecutorService,
        true);
  }

  @Test
  public void shouldNotAckIfChannelIsNotIdle() {
    long now = System.nanoTime();
    assertTrue(clientChannelManager.getLastAckTime() < now);
    clientChannelManager.setLastSendTime(now);
    clientChannelManager.localHouseKeeping();
    assertTrue(clientChannelManager.getLastAckTime() < now);

  }

  @Test
  public void registerChannel() throws Exception {
    PowerMockito.mockStatic(ByteBufferToPackets.class);
    Pair<Integer, IPacket> dummyPacket = Pair.create(0, null);
    PowerMockito
        .when(ByteBufferToPackets.wrapBytesToPackets(clientChannelManager, PacketType.REQUEST, null))
        .thenReturn(dummyPacket);

    channelCommons.write(
        clientChannelManager,
        socketAddress,
        dummyPacket.getSecond(),
        dummyPacket.getFirst());
    expectLastCall();
    replay(channelCommons);
    clientChannelManager.registerChannel();

    verify(channelCommons);
  }

  @Test
  public void cleanUpInFlightMessages() throws Exception {
    IPacket packet = new AckPacket(ByteBuffer.allocate(0), 0);
    clientChannelManager.putInFlightPacket(0, packet);
    expect(packetManager.getDeliveredSuccessfully()).andReturn(1);
    //expect(packetManager.getAckManager()).andReturn(new AckManagerImpl(incomingPacketConsumedTill));

    packetManager.releasePacket(packet);
    expectLastCall();

    replay(packetManager);
    clientChannelManager.cleanUpInFlightMessages();
    verify(packetManager);
  }

  @Test
  public void reSendPacket() throws Exception {

  }

  @Test
  public void getIncomingPacketConsumedTill() throws Exception {

  }

  @Test
  public void addIncomingPacket() throws Exception {

  }

  @Test
  public void ackSeen() throws Exception {

  }

  @Test
  public void putInFlightPacket() throws Exception {

  }

  @Test
  public void canIgnore() throws Exception {
    IPacket packet = TestUtils.getRandomPacket(PacketType.ACK);
    assertFalse(clientChannelManager.canIgnore(packet));

    packet = TestUtils.getRandomPacket(PacketType.DATA);
    assertFalse(clientChannelManager.canIgnore(packet));

    clientChannelManager.addIncomingPacket(packet.getHeader().getSequenceNumber(), packet);

    packet = TestUtils.getRandomPacket(PacketType.DATA);
    assertTrue(clientChannelManager.canIgnore(packet));
  }
}