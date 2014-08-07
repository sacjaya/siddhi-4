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

import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class PartitionInstanceRuntime {
    private String key;
    private List<QueryRuntime> queryRuntimeList;

    public PartitionInstanceRuntime(String key,List<QueryRuntime> queryRuntimeList){
        this.key =key;
        this.queryRuntimeList = queryRuntimeList;
    }


    public void remove(ConcurrentMap<String, StreamJunction> streamJunctionMap, ConcurrentMap<String, AbstractDefinition> streamDefinitionMap) {
        for(QueryRuntime queryRuntime: queryRuntimeList){
            queryRuntime.removeQuery(streamJunctionMap,streamDefinitionMap);
        }
    }
}
