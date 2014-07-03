/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.stream;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.disruptor.util.SiddhiEventFactory;
import org.wso2.siddhi.core.event.disruptor.util.SiddhiEventPublishTranslator;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class StreamJunction {
    private List<StreamReceiver> streamReceivers = new CopyOnWriteArrayList<StreamReceiver>();
    private ThreadPoolExecutor threadPoolExecutor;
    private SiddhiEventFactory factory = new SiddhiEventFactory();
    private int bufferSize = 1024;
    private boolean distruptorEnabled= false;

    Executor executor =     Executors.newCachedThreadPool();
    private String streamId;

    public StreamJunction(String streamId, ThreadPoolExecutor threadPoolExecutor) {
        this.streamId = streamId;
        this.threadPoolExecutor = threadPoolExecutor;
    }


    public void send(StreamEvent allEvents) {
       Event event = (Event) allEvents;
       for (StreamReceiver streamReceiver : streamReceivers) {
           if (distruptorEnabled) {
           streamReceiver.getDisruptor().publishEvent(new SiddhiEventPublishTranslator(event));
           } else {
               streamReceiver.receive(allEvents);
           }

       }
    }

    public synchronized void addEventFlow(StreamReceiver streamReceiver) {
        if(distruptorEnabled) {
        Disruptor disruptor = new Disruptor<Event>(factory, bufferSize, executor, ProducerType.SINGLE,new SleepingWaitStrategy());
        streamReceiver.setDisruptor(disruptor);
        disruptor.handleEventsWith(new StreamHandler(streamReceiver));
        disruptor.start();
        }
        //in reverse order to execute the later states first to overcome to dependencies of count states
        streamReceivers.add(0, streamReceiver);
    }

    public synchronized void removeEventFlow(HandlerProcessor queryStreamProcessor) {
        streamReceivers.remove(queryStreamProcessor);
        if(distruptorEnabled){
        queryStreamProcessor.getDisruptor().shutdown();
        }
    }

    public String getStreamId() {
        return streamId;
    }
}
