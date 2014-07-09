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
package org.wso2.siddhi.core.executor.expression;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import org.wso2.siddhi.query.api.query.input.BasicSingleInputStream;
import org.wso2.siddhi.query.api.query.input.InputStream;

public class VariableExpressionExecutor implements ExpressionExecutor {
    Attribute.Type type;
    int streamPosition = -1;
    int attributePosition = -1;
    int innerStreamPosition = -1;  //Simple Event (Default)
    String attributeName;
    String streamReference;


    public VariableExpressionExecutor(String streamIdOfVariable, String attributeName, int position,
                                      String currentStreamReference, boolean processInDefinition, InputStream inputStream) {
        this.attributeName = attributeName;
        if (streamIdOfVariable != null) {
            streamReference = streamIdOfVariable;
        } else {
            streamReference = currentStreamReference;
        }
        AbstractDefinition definition = null;
        if(inputStream instanceof BasicSingleInputStream) {
            definition = getDefinition(inputStream);
        } //TODO : else

        if(definition!=null){
       type = definition.getAttributeType(attributeName);
       attributePosition = definition.getAttributePosition(attributeName);
        }


        //TODO: below lines of code for testing purpose
//        if (attributeName.equals("symbol")) {
//            type = Attribute.Type.STRING;
//            attributePosition = 0;
//        } else if (attributeName.equals("price")) {
//            type = Attribute.Type.FLOAT;
//            attributePosition = 1;
//        } else if (attributeName.equals("volume")) {
//            type = Attribute.Type.INT;
//            attributePosition = 2;
//        } else if (attributeName.equals("tweet")) {
//            type = Attribute.Type.STRING;
//            attributePosition = 1;
//        }

    }


    private AbstractDefinition getDefinition(InputStream inputStream){
        AbstractDefinition definition = null;
        if(inputStream instanceof BasicSingleInputStream) {
            String referenceSourceId = ((BasicSingleInputStream)inputStream).getStreamReferenceId();
            if (referenceSourceId != null && referenceSourceId.equals(streamReference)) {
                definition = ((BasicSingleInputStream)inputStream).getDefinition();
            }
        } //TODO: else
        return definition;
    }

    @Override
    public Object execute(StreamEvent event) {

            if (event instanceof Event) {
                return ((Event) event).getData()[attributePosition];
            }
            else {
                //TODO: else
                return null;
            }

    }

    public Attribute.Type getReturnType() {
        return type;
    }


    public String constructFilterQuery(StreamEvent newEvent, int level) {
        Object obj = execute(newEvent);
        if (obj == null) {
            StringBuilder sb = new StringBuilder();
            if (streamPosition >= 0 && level == 0) {
                sb.append("event").append(streamPosition).append(".");
            }
            if (innerStreamPosition >= 0) {
                sb.append("event").append(innerStreamPosition).append(".");
            }
            if (attributePosition >= 0) {
                sb.append("data").append(attributePosition);
            }
            return sb.toString();
        } else if (obj instanceof String) {
            return "'" + obj.toString() + "'";
        } else {
            return obj.toString();
        }
    }
}
