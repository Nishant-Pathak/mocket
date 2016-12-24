package com.network.mocket.helper;

import java.util.List;

public interface AckManager {
  int getLastSeenAck();
  void ackSeen(int i);
  Pair<Integer, Integer> recentMissingAckRange();

  String dump();

  List<Integer> getVoids(int maxNackSize);

  boolean preferNackVsAck();
}
