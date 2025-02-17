/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.broker.region.policy;

import org.apache.activemq.ActiveMQMessageAudit;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.Message;

/**
 * A default implementation of {@link DeadLetterStrategy} which uses
 * a constant destination.
 * 
 * 
 * @org.apache.xbean.XBean
 * 
 * 
 */
public class SharedDeadLetterStrategy extends AbstractDeadLetterStrategy {

    public static final String DEFAULT_DEAD_LETTER_QUEUE_NAME = "ActiveMQ.DLQ";

    private ActiveMQDestination deadLetterQueue = new ActiveMQQueue(DEFAULT_DEAD_LETTER_QUEUE_NAME);
    private final ActiveMQMessageAudit messageAudit = new ActiveMQMessageAudit();

    public ActiveMQDestination getDeadLetterQueueFor(Message message, Subscription subscription) {
        return deadLetterQueue;
    }

    public ActiveMQDestination getDeadLetterQueue() {
        return deadLetterQueue;
    }

    public void setDeadLetterQueue(ActiveMQDestination deadLetterQueue) {
        this.deadLetterQueue = deadLetterQueue;
    }

    @Override
    public int getMaxProducersToAudit() {
        return messageAudit.getMaximumNumberOfProducersToTrack();
    }

    @Override
    public void setMaxProducersToAudit(int maxProducersToAudit) {
        messageAudit.setMaximumNumberOfProducersToTrack(maxProducersToAudit);
    }

    @Override
    public void setMaxAuditDepth(int maxAuditDepth) {
        messageAudit.setAuditDepth(maxAuditDepth);
    }

    @Override
    public int getMaxAuditDepth() {
        return messageAudit.getAuditDepth();
    }

    @Override
    protected ActiveMQMessageAudit lookupActiveMQMessageAudit(Message message) {
        return messageAudit;
    }

}
