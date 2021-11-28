package com.t2g.app.structure;

import com.google.common.primitives.Ints;
import com.t2g.app.facade.StreamingServiceFacade;
import lombok.Getter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayCredentialObject implements Delayed {

    @Getter
    private StreamingServiceFacade streamingServiceFacade;

    private long expirationTimeInMillis;

    public DelayCredentialObject(StreamingServiceFacade streamingServiceFacade, long timeToExpireInMillis) {
        this.streamingServiceFacade = streamingServiceFacade;
        this.expirationTimeInMillis = System.currentTimeMillis() + timeToExpireInMillis;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long timeDiff = expirationTimeInMillis - System.currentTimeMillis();
        return unit.convert(timeDiff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Ints.saturatedCast(
                this.expirationTimeInMillis - ((DelayCredentialObject) o).expirationTimeInMillis
        );
    }
}
