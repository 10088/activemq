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
package org.apache.activemq.usecases;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;

import javax.jms.*;
import java.io.File;

public class DurableSubscriptionOfflineTest extends org.apache.activemq.TestSupport {

    private BrokerService broker;
    private ActiveMQTopic topic;

    protected ActiveMQConnectionFactory createConnectionFactory() throws Exception {
        return new ActiveMQConnectionFactory("vm://" + getName());
    }

    @Override
    protected Connection createConnection() throws Exception {
        Connection con = super.createConnection();
        con.setClientID("cliName");
        con.start();
        return con;
    }

    protected void setUp() throws Exception {
        topic = (ActiveMQTopic) createDestination();
        createBroker();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        destroyBroker();
    }

    private void createBroker() throws Exception {
        broker = BrokerFactory.createBroker("broker:(vm://localhost)");
        broker.setBrokerName(getName());
        broker.setDeleteAllMessagesOnStartup(true);

        broker.setPersistent(true);
        KahaDBPersistenceAdapter persistenceAdapter = new KahaDBPersistenceAdapter();
        persistenceAdapter.setDirectory(new File("activemq-data-kaha/" + getName()));
        broker.setPersistenceAdapter(persistenceAdapter);

        broker.start();
    }

    private void destroyBroker() throws Exception {
        if (broker != null)
            broker.stop();
    }

    public void testOfflineSubscription() throws Exception {
        // create durable subscription
        Connection con = createConnection();
        Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        session.createDurableSubscriber(topic, "SubsId", "filter = 'true'", true);
        session.close();
        con.close();

        // send messages
        con = createConnection();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(null);

        int sent = 0;
        for (int i = 0; i < 10; i++) {
            boolean filter = i % 2 == 1;
            if (filter)
                sent++;

            Message message = session.createMessage();
            message.setStringProperty("filter", filter ? "true" : "false");
            producer.send(topic, message);
        }

        session.close();
        con.close();

        // consume messages
        con = createConnection();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createDurableSubscriber(topic, "SubsId", "filter = 'true'", true);
        Listener listener = new Listener();
        consumer.setMessageListener(listener);

        Thread.sleep(3 * 1000);

        session.close();
        con.close();

        assertEquals(sent, listener.count);
    }

    public static class Listener implements MessageListener {
        int count = 0;

        public void onMessage(Message message) {
            count++;
        }
    }
}
