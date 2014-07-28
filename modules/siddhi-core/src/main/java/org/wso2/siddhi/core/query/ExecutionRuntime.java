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

package org.wso2.siddhi.core.query;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.core.query.creator.QueryCreator;
import org.wso2.siddhi.core.query.creator.QueryCreatorFactory;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateManager;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.PartitionHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.PartitionedStreamHandlerProcessor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.parser.QueryOutputParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.partition.Partition;
import org.wso2.siddhi.query.api.query.Query;
import org.wso2.siddhi.query.api.query.input.SingleInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class ExecutionRuntime {
    private String queryId;
    private Query query;
    private StreamDefinition outputStreamDefinition;
    private OutputCallback outputCallback = null;
    private OutputRateManager outputRateManager;
    private List<HandlerProcessor> handlerProcessors = new ArrayList<HandlerProcessor>();
    private boolean localStream;


    public ExecutionRuntime(Query query, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap, ConcurrentMap<String, StreamJunction> streamJunctionMap, Partition partition, SiddhiContext siddhiContext,PartitionRuntime partitionRuntime) {
        if (query.getPropertyValue("name") == null) {
           query.property("name", UUID.randomUUID().toString());
        }
        this.queryId = query.getPropertyValue("name");
        this.query = query;
        outputRateManager = QueryOutputParser.constructOutputRateManager(query.getOutputRate());

        ConcurrentMap<String, AbstractDefinition> localStreamDefinitionMap = null;
        ConcurrentMap<String, StreamJunction> localStreamJunctionMap = null;
        if(partitionRuntime != null){
            localStreamDefinitionMap = partitionRuntime.getLocalStreamDefinitionMap();
            localStreamJunctionMap = partitionRuntime.getLocalStreamJunctionMap();
        }
        QueryCreator queryCreator = QueryCreatorFactory.constructQueryCreator(queryId, query, streamDefinitionMap,localStreamDefinitionMap, outputRateManager, siddhiContext);
        outputStreamDefinition = queryCreator.getOutputStreamDefinition();

        if(queryCreator.querySelector.isPartitioned()){
            localStream = true;
            outputCallback = QueryOutputParser.constructOutputCallback(query.getOutputStream(), localStreamJunctionMap, siddhiContext, outputStreamDefinition);
            outputRateManager.setOutputCallback(outputCallback);

        } else {
            outputCallback = QueryOutputParser.constructOutputCallback(query.getOutputStream(), streamJunctionMap, siddhiContext, outputStreamDefinition);
            outputRateManager.setOutputCallback(outputCallback);
        }

        ArrayList<QuerySelector> querySelectorList = new ArrayList<QuerySelector>();

        QueryPartitioner queryPartitioner = new QueryPartitioner(partition,queryCreator, querySelectorList, siddhiContext);

        List<HandlerProcessor> handlerProcessorList = queryPartitioner.constructPartition();

        if(partition == null){
            handlerProcessors = handlerProcessorList;
            for (HandlerProcessor handlerProcessor : handlerProcessors) {
                streamJunctionMap.get(handlerProcessor.getStreamId()).addEventFlow(handlerProcessor);
            }
        } else if (((SingleInputStream) query.getInputStream()).isPartitioned() ){
                for (int i = 0; i < handlerProcessorList.size(); i++) {
                    HandlerProcessor queryStreamProcessor = handlerProcessorList.get(i);
                    handlerProcessors.add(new PartitionedStreamHandlerProcessor(queryStreamProcessor.getStreamId(), queryPartitioner, i));

                }
            for (HandlerProcessor handlerProcessor : handlerProcessors) {
                localStreamJunctionMap.get(handlerProcessor.getStreamId()).addEventFlow(handlerProcessor);
            }
        } else {
            List<List<PartitionExecutor>> partitionExecutors = queryPartitioner.getPartitionExecutors();
            for (int i = 0; i < handlerProcessorList.size(); i++) {
                HandlerProcessor queryStreamProcessor = handlerProcessorList.get(i);
                handlerProcessors.add(new PartitionHandlerProcessor(queryCreator.querySelector.isPartitioned(),queryStreamProcessor.getStreamId(), queryPartitioner, i, partitionExecutors.get(i)));
            }
            for (HandlerProcessor handlerProcessor : handlerProcessors) {
                streamJunctionMap.get(handlerProcessor.getStreamId()).addEventFlow(handlerProcessor);
            }
        }


    }


    public OutputCallback getOutputCallback() {
        return outputCallback;
    }

    public String getQueryId() {
        return queryId;
    }

    public void addCallback(QueryCallback callback) {
        outputRateManager.addQueryCallback(callback);
    }

    public void removeQuery(ConcurrentMap<String, StreamJunction> streamJunctionMap, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap) {
        for (HandlerProcessor queryStreamProcessor : handlerProcessors) {
            StreamJunction junction = streamJunctionMap.get(queryStreamProcessor.getStreamId());
            if (junction != null) {
                junction.removeEventFlow(queryStreamProcessor);
            }
        }
        streamDefinitionMap.remove(query.getOutputStream().getStreamId());
    }

    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

    public boolean isLocalStream(){
        return localStream;
    }
}
