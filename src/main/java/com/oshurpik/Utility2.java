package com.oshurpik;

import com.google.common.base.Stopwatch;
import com.oshurpik.component.FileReaderHelper;
import com.oshurpik.component.HttpRequestHelper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;

public class Utility2 {
    
    public static void main(String[] ags) {
        final int THREAD_NUMBER = 100;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        Producer producer = new Producer(executor);
        producer.run();

        executor.shutdown();
        while(!executor.isTerminated()) {}
        System.out.println("All work is done");
    }
}

class Producer {
    private static final int TESTS_PER_MINUTE = 6000;
    private static final int QUANTS_PER_SECOND = 10;
    
    private static final int QUANTS_PER_MINUTE = DateTimeConstants.SECONDS_PER_MINUTE * QUANTS_PER_SECOND;
    final static Logger logger = Logger.getLogger(Utility2.class.getName());
 
    ExecutorService executor;

    Producer(ExecutorService executor) {
        this.executor = executor;
    }

    public void run() {
        final int NUMBER_OF_TESTS_PER_QUANT = Math.round(TESTS_PER_MINUTE / QUANTS_PER_MINUTE);
        
        for (int j = 0; j < QUANTS_PER_MINUTE; j++) {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                for (int i = 0; i < NUMBER_OF_TESTS_PER_QUANT; i++) {
                    System.out.println("Added.");
                    executor.execute(new Consumer(FileReaderHelper.read()));
                }
            } catch (Exception ex) {
               ex.printStackTrace();
            }
            stopwatch.stop();
            long timeToSleep = DateTimeConstants.MILLIS_PER_SECOND / QUANTS_PER_SECOND - (stopwatch.elapsed(TimeUnit.MILLISECONDS));
            try {                
                Thread.sleep(timeToSleep);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
 
class Consumer implements Runnable {
    final static Logger logger = Logger.getLogger(Utility2.class.getName());
 
    String url;

    Consumer(String url) {
        this.url = url;
    }
   
    public void run() {
        try {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            HttpResponse response = HttpRequestHelper.sendRequest(url);
            stopwatch.stop();

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
