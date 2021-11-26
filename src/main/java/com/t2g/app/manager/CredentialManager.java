package com.t2g.app.manager;

import com.t2g.app.facade.StreamingServiceFacade;
import com.t2g.app.structure.DelayCredentialObject;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

@Component
public class CredentialManager {
    private DelayQueue<DelayCredentialObject> credentialDelayQueue = new DelayQueue<>();

    @PostConstruct
    public void postConstruct() {
        new Thread(new CredentialDelayQueueConsumer(credentialDelayQueue)).start();
    }

    public void addToQueue(StreamingServiceFacade streamingServiceFacade, long timeToExpireInMillis) {
        DelayCredentialObject delayCredentialObject = new DelayCredentialObject(streamingServiceFacade, timeToExpireInMillis);
        credentialDelayQueue.offer(delayCredentialObject);
    }

    @AllArgsConstructor
    private static class CredentialDelayQueueConsumer implements Runnable {
        private static final int TIMEOUT_IN_MINUTES = 5;

    private DelayQueue<DelayCredentialObject> delayQueue;

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                // Waits until an object's expirationDate is reached or the timeout is exceeded.
                //  Returns null if an object doesn't reach its expirationDate upon the specified timeout
                DelayCredentialObject delayCredentialObject = delayQueue.poll(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

                if (delayCredentialObject != null) {
                    delayCredentialObject.getStreamingServiceFacade().refreshCredentials();
                }
            }
        }
    }
}
