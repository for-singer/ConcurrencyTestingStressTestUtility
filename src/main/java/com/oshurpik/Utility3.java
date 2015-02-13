package com.oshurpik;

import com.google.common.base.Stopwatch;
import com.oshurpik.component.FileReaderHelper;
import com.oshurpik.component.HttpRequestHelper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;

public class Utility3 {
    
    public static void main(String[] ags) {
        final int THREAD_NUMBER = 110;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        AtomicInteger counter = new AtomicInteger();
        
        AtomicReference<ExecutorService> executorReference = new AtomicReference<>();
        executorReference.set(executor);
        
        Producer3 producer = new Producer3(executorReference, counter, THREAD_NUMBER);
        producer.run();
        
        executorReference.get().shutdown();
        while(!executorReference.get().isTerminated()) {}
        System.out.println("All work is done");
    }
}

class Producer3 {
    private static final int JOB_NUMBER_THRESHOLD_TOP = 170;
    private static final int JOB_NUMBER_THRESHOLD_BOTTOM = 125;
    
    private static final int THREAD_NUMBER_DELTA = 25;
    
    private static final int TESTS_PER_MINUTE = 6000;
    private static final int QUANTS_PER_SECOND = 1;
    
    private static final int QUANTS_PER_MINUTE = DateTimeConstants.SECONDS_PER_MINUTE * QUANTS_PER_SECOND;
    final static Logger logger = Logger.getLogger(Utility3.class.getName());
 
    AtomicReference<ExecutorService> executorReference;
    AtomicInteger counter;
    Integer threadNumber;

    Producer3(AtomicReference<ExecutorService> executorReference, AtomicInteger counter, Integer threadNumber) {
        this.executorReference = executorReference;
        this.counter = counter;
        this.threadNumber = threadNumber;
    }

    public void run() {
        final int NUMBER_OF_TESTS_PER_QUANT = Math.round(TESTS_PER_MINUTE / QUANTS_PER_MINUTE);

        boolean flag = false;        
        for (int j = 0; j < QUANTS_PER_MINUTE; j++) {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                System.out.println("flag:" + flag);
                if (counter.get() > threadNumber) {
                    flag = true;
                }
                if (counter.get() > JOB_NUMBER_THRESHOLD_TOP) {
                    threadNumber += THREAD_NUMBER_DELTA;
                    executorReference.get().shutdown();
                    executorReference.set(Executors.newFixedThreadPool(threadNumber));
                }
                else if (flag && threadNumber > JOB_NUMBER_THRESHOLD_BOTTOM) {
                    threadNumber -= THREAD_NUMBER_DELTA;
                    executorReference.get().shutdown();
                    executorReference.set(Executors.newFixedThreadPool(threadNumber));
                }
                System.out.println("threadNumber:" + threadNumber);
                
                for (int i = 0; i < NUMBER_OF_TESTS_PER_QUANT; i++) {                    
                    executorReference.get().execute(new Consumer3(FileReaderHelper.read(), counter));
                    System.out.println("Added.");                    
                    System.out.println("counter from prod:" + counter.incrementAndGet());                                        
                }                
            } catch (Exception ex) {
               ex.printStackTrace();
            }
            stopwatch.stop();
            long timeToSleep = DateTimeConstants.MILLIS_PER_SECOND / QUANTS_PER_SECOND - (stopwatch.elapsed(TimeUnit.MILLISECONDS));
            try {                
                Thread.sleep(timeToSleep);
                System.out.println("timeToSleep:" + timeToSleep);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
    }
}

class Consumer3 implements Runnable {
    final static Logger logger = Logger.getLogger(Utility3.class.getName());
 
    String url;
    AtomicInteger counter;

    Consumer3(String url, AtomicInteger counter) {
        this.url = url;
        this.counter = counter;
    }
   
    public void run() {
        try {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            HttpResponse response = HttpRequestHelper.sendRequest(url);
            stopwatch.stop();
                        
            System.out.println("counter from consumer:" + counter.decrementAndGet());

            Integer size = null;
            try {
                size = EntityUtils.toString(response.getEntity()).length();
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            logger.info("url : " + url + ", status code : " + response.getStatusLine().getStatusCode() + ", latency : " + String.valueOf(stopwatch.elapsed(TimeUnit.MILLISECONDS)) + ", size : " + String.valueOf(size));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
