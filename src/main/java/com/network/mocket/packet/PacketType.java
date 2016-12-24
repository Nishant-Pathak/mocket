package com.network.mocket.packet;

public enum PacketType {
  REQUEST((short)0),
  DATA((short)1),
  ACK((short)2),
  NACK((short)3);

  public short type;
  PacketType(short i) {
    type = i;
  }

  public boolean isValidType() {
    return 0 <= type && type <= PacketType.values().length;
  }

  public boolean isTimeAccounted() {
    return this != ACK && this != NACK;
  }
}
