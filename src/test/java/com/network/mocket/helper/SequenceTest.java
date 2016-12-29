package com.network.mocket.helper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SequenceTest {
  AtomicInteger sequence;

  @Before
  public void setUp() throws Exception {
     sequence = new AtomicInteger();
  }

  @After
  public void tearDown() throws Exception {
    sequence = null;
  }

  @Test
  public void getAndIncrement() throws Exception {
    assertEquals(sequence.getAndIncrement(), 0);
    assertEquals(sequence.getAndIncrement(), 1);
  }

  @Test
  public void test1() throws ExecutionException, InterruptedException {
    getAndIncrementConcurrent(1);
  }

  @Test
  public void test5() throws ExecutionException, InterruptedException {
    getAndIncrementConcurrent(5);
  }

  @Test
  public void test10() throws ExecutionException, InterruptedException {
    getAndIncrementConcurrent(10);
  }

  @Test
  public void test100() throws ExecutionException, InterruptedException {
    getAndIncrementConcurrent(100);
  }

  private void getAndIncrementConcurrent(final int threadCount) throws InterruptedException, ExecutionException {
    Callable<Integer> task = new Callable<Integer>() {
      @Override public Integer call() {
        return sequence.getAndIncrement();
      }
    };
    List<Callable<Integer>> tasks = Collections.nCopies(threadCount, task);
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    List<Future<Integer>> futures = executorService.invokeAll(tasks);
    List<Integer> results = new ArrayList<>(futures.size());
    for (Future<Integer> future: futures){
      results.add(future.get());
    }

    assertEquals(threadCount, futures.size());
    List<Integer> expectedResult = new ArrayList<>(futures.size());
    for(int i = 0; i< threadCount; i++) {
      expectedResult.add(i);
    }
    Collections.sort(results);
    assertEquals(expectedResult, results);
  }

}