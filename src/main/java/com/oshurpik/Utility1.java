package com.oshurpik;

import com.google.common.base.Stopwatch;
import com.oshurpik.component.FileReaderHelper;
import com.oshurpik.component.HttpRequestHelper;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class Utility1 {
    private static final int TEST_NUMBER = 10;
    final static Logger logger = Logger.getLogger(Utility1.class.getName());
    
    public static void main(String[] ags) {
        System.out.println("Start:");
        String url;
        for(int i = 0; i < TEST_NUMBER; i++) {
            url = FileReaderHelper.read();
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
    }
}
