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
package org.apache.activemq.transport.amqp.joram;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.objectweb.jtests.jms.admin.Admin;
import org.apache.qpid.amqp_1_0.jms.impl.ConnectionFactoryImpl;
import org.apache.qpid.amqp_1_0.jms.impl.QueueImpl;
import org.apache.qpid.amqp_1_0.jms.impl.TopicImpl;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.net.URI;
import java.util.Hashtable;

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class ActiveMQAdmin implements Admin {

    Context context;
    {
        try {
            // Use the jetty JNDI context since it's mutable.
            final Hashtable<String, String> env = new Hashtable<String, String>();
            env.put("java.naming.factory.initial", "org.eclipse.jetty.jndi.InitialContextFactory");
            env.put("java.naming.factory.url.pkgs", "org.eclipse.jetty.jndi");;
            context = new InitialContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected BrokerService createBroker() throws Exception {
        return BrokerFactory.createBroker(new URI("broker://()/localhost?persistent=false"));
    }

    public String getName() {
        return getClass().getName();
    }

    BrokerService broker;
    int port;

    public void startServer() throws Exception {
        if (System.getProperty("basedir") == null) {
            File file = new File(".");
            System.setProperty("basedir", file.getAbsolutePath());
        }
        broker = createBroker();
        broker.start();
        final TransportConnector connector = broker.addConnector("amqp://localhost:0");
        port = connector.getConnectUri().getPort();
    }

    public void stopServer() throws Exception {
        broker.stop();
    }

    public void start() throws Exception {
    }

    public void stop() throws Exception {
    }

    public Context createContext() throws NamingException {
        return context;
    }

    public void createQueue(String name) {
        try {
            context.bind(name, new QueueImpl("queue://"+name));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTopic(String name) {
        try {
            context.bind(name, new TopicImpl("topic://"+name));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteQueue(String name) {
        // BrokerTestSupport.delete_queue((Broker)base.broker, name);
        try {
            context.unbind(name);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTopic(String name) {
        try {
            context.unbind(name);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void createConnectionFactory(String name) {
        try {
            final ConnectionFactory factory = new ConnectionFactoryImpl("localhost", port, null, null);
            context.bind(name, factory);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteConnectionFactory(String name) {
        try {
            context.unbind(name);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public void createQueueConnectionFactory(String name) {
        createConnectionFactory(name);
    }
    public void createTopicConnectionFactory(String name) {
        createConnectionFactory(name);
    }
    public void deleteQueueConnectionFactory(String name) {
        deleteConnectionFactory(name);
    }
    public void deleteTopicConnectionFactory(String name) {
        deleteConnectionFactory(name);
    }

}
