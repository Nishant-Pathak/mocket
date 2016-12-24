package com.network.mocket.helper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AckManagerImpl implements AckManager {
  private static final Logger LOGGER = Logger.getLogger(AckManagerImpl.class.getSimpleName());
  private final TreeSet<Integer> acks;
  volatile boolean seenZero;


  public AckManagerImpl() {
    this.acks = new TreeSet<>();
    this.seenZero = false;
    acks.add(-1);
  }

  private int condense() {
    Iterator<Integer> iterator = acks.iterator();
    int start = iterator.next();
    while (iterator.hasNext()) {
      int next = iterator.next();
      if(start+1 != next) break;
      start++;
    }
    return start;
  }

  @Override
  synchronized public int getLastSeenAck() {
    return condense();
  }

  @Override
  synchronized public void ackSeen(int i) {
    LOGGER.log(Level.FINEST, "ack seen for: {0}", i);
    if (i == 0) seenZero = true;
    acks.add(i);
  }

  @Override
  synchronized public Pair<Integer, Integer> recentMissingAckRange() {
    int startAck = condense();
    Integer lastAck = acks.higher(startAck);
    if (null == lastAck) {
      lastAck = startAck;
    }
    LOGGER.log(Level.FINEST, "start is {0}, and end is {1}", new Object[]{startAck, lastAck});

    return Pair.create(startAck, lastAck);
  }

  @Override
  public String dump() {
    return "Acks: " + acks;
  }

  @Override
  synchronized public List<Integer> getVoids(int maxVoids) {
    int start = condense();

    List<Integer> voids = new LinkedList<>();

    for (int i = start + 1; i < acks.last() && voids.size() < maxVoids; i++) {
      if (!acks.contains(i)) {
        voids.add(i);
      }
    }
    LOGGER.log(Level.FINEST, "acks are {0}, voids are {1}", new Object[]{dump(), voids});
    return voids;
  }

  /**
   * prefer nack vs ack if there is more distributed void.
   * @return
   */
  @Override
  synchronized public boolean preferNackVsAck() {
    Pair<Integer, Integer> recentMissSize = recentMissingAckRange();
    int diff = recentMissSize.getSecond() - recentMissSize.getFirst();
    LOGGER.log(Level.FINEST, "ack\'s are: {0}, diff is {1}", new Object[]{dump(), diff});
    return acks.size() > diff && diff != 0;
  }
}
