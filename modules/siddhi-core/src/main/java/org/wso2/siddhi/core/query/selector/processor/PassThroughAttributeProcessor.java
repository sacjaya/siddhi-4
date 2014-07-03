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
package org.wso2.siddhi.core.query.selector.processor;


import org.wso2.siddhi.core.event.StreamEvent;

import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

public class PassThroughAttributeProcessor{

    private Attribute.Type outputType;

    public PassThroughAttributeProcessor(Expression expression, AbstractDefinition inputStreamDefinition) {
        if(expression instanceof Variable){
               outputType =   inputStreamDefinition.getAttributeType( ((Variable)expression).getAttributeName());

         }
    }

//    public PassThroughAttributeProcessor(Expression expressionExecutor) {
//
//    }


    public Attribute.Type getOutputType() {
        return outputType;
    }
}
