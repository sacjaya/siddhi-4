/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.query.input.pattern;

import org.wso2.siddhi.query.api.query.input.pattern.element.CountElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.LogicalElement;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;
import org.wso2.siddhi.query.api.query.input.BasicSingleInputStream;
import org.wso2.siddhi.query.api.query.input.InputStream;
import org.wso2.siddhi.query.api.query.input.pattern.element.FollowedByElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.PatternElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PatternInputStream implements InputStream, PatternElement {

    private PatternElement patternElement;
    private List<String> streamIdList;
    private TimeConstant within;

    public PatternInputStream(PatternElement patternElement, TimeConstant within) {
        this.patternElement = patternElement;
        this.streamIdList = new ArrayList<String>(collectStreamIds(patternElement, new HashSet<String>()));
        this.within = within;
    }

    public PatternInputStream(PatternElement patternElement) {
        this(patternElement, null);
    }

    @Override
    public List<String> getStreamIds() {
        return streamIdList;
    }
//
//    @Override
//    public List<QueryEventSource> constructQueryEventSourceList(
//            ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
//            List<QueryEventSource> queryEventSources) {
//        return constructEventStreamList(patternElement, streamTableDefinitionMap, queryEventSources);
//    }

    public TimeConstant getWithin() {
        return within;
    }

    public PatternElement getPatternElement() {
        return patternElement;
    }

    private HashSet<String> collectStreamIds(PatternElement patternElement,
                                             HashSet<String> streamIds) {
        if (patternElement instanceof PatternInputStream) {
            streamIds.addAll(((PatternInputStream) patternElement).getStreamIds());
        } else if (patternElement instanceof BasicSingleInputStream) {
            streamIds.addAll(((BasicSingleInputStream) patternElement).getStreamIds());
        } else if (patternElement instanceof LogicalElement) {
            collectStreamIds(((LogicalElement) patternElement).getStreamElement1(), streamIds);
            collectStreamIds(((LogicalElement) patternElement).getStreamElement1(), streamIds);
        } else if (patternElement instanceof CountElement) {
            collectStreamIds(((CountElement) patternElement).getStreamElement(), streamIds);
        } else if (patternElement instanceof FollowedByElement) {
            collectStreamIds(((FollowedByElement) patternElement).getPatternElement(), streamIds);
            collectStreamIds(((FollowedByElement) patternElement).getFollowedByPatternElement(), streamIds);
        }
        return streamIds;
    }

//    public List<QueryEventSource> constructEventStreamList(PatternElement patternElement,
//                                                           ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
//                                                           List<QueryEventSource> queryEventSources) {
//
//
//        if (patternElement instanceof BasicSingleInputStream) {
//            // todo fix
//            // ((BasicSingleInputStream) patternElement).constructQueryEventSourceList(streamTableDefinitionMap, queryEventSources);
//        } else if (patternElement instanceof LogicalElement) {
//            constructEventStreamList(((LogicalElement) patternElement).getStreamElement1(), streamTableDefinitionMap, queryEventSources);
//            constructEventStreamList(((LogicalElement) patternElement).getStandardStream2(), streamTableDefinitionMap, queryEventSources);
//        } else if (patternElement instanceof CountElement) {
//            constructEventStreamList(((CountElement) patternElement).getStreamElement(), streamTableDefinitionMap, queryEventSources);
//        } else if (patternElement instanceof FollowedByElement) {
//            constructEventStreamList(((FollowedByElement) patternElement).getPatternElement(), streamTableDefinitionMap, queryEventSources);
//            constructEventStreamList(((FollowedByElement) patternElement).getFollowedByPatternElement(), streamTableDefinitionMap, queryEventSources);
//        } else if (patternElement instanceof PatternInputStream) {
//            ((PatternInputStream) patternElement).constructQueryEventSourceList(streamTableDefinitionMap, queryEventSources);
//        }
//
//        return queryEventSources;
//    }
}
