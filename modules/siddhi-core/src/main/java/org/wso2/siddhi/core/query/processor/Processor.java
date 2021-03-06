/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.query.processor;

import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.converter.EventConverter;
import org.wso2.siddhi.core.query.QueryProcessingElement;

public abstract class Processor  implements QueryProcessingElement{

    /**
     * To construct the Processor chain specifying the
     * next processor which will handle event
     * @param processor
     */
    public  abstract void addToLast(QueryProcessingElement processor);

   /**
     * To process incoming event
     *
     * @param streamEvent
     * @return
     */
    public  abstract void process(StreamEvent streamEvent);


    public abstract QueryProcessingElement getNext();
}
