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
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsMatchFunctionExecutor extends FunctionExecutor {

    private Pattern pattern;
    private ExpressionExecutor expressionExecutor;

    @Override
    public void init(List<ExpressionExecutor> attributeExpressionExecutors, SiddhiContext siddhiContext) {
        if (attributeSize != 2) {
            throw new OperationNotSupportedException("IsMatch has to have 2 expressions regex and the attribute, " +
                    "currently " + attributeSize + " expressions provided");
        }
        ExpressionExecutor regexExecutor = attributeExpressionExecutors.get(0);
        if (regexExecutor.getReturnType() != Attribute.Type.STRING &&
                regexExecutor instanceof ConstantExpressionExecutor) {
            throw new OperationNotSupportedException("IsMatch expects regex string input expression but found " +
                    regexExecutor.getReturnType());
        }
        expressionExecutor = attributeExpressionExecutors.get(1);

        pattern = Pattern.compile((String) regexExecutor.execute(null));

    }

    /**
     * The main executions method which will be called upon event arrival
     *
     * @param data the runtime values of the attributeExpressionExecutors
     * @return
     */
    @Override
    protected Object execute(Object[] data) {
        throw new IllegalStateException("isMatch cannot execute two data " + Arrays.deepToString(data));
    }

    @Override
    protected Object execute(Object data) {
        Matcher matcher = pattern.matcher(data.toString());
        return matcher.matches();
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.BOOL;
    }

    @Override
    public Object execute(StreamEvent event) {
        return execute(expressionExecutor.execute(event));
    }


}
