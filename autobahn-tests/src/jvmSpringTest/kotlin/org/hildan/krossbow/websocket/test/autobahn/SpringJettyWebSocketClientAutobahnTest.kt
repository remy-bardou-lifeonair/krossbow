package org.hildan.krossbow.websocket.test.autobahn

import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.spring.SpringJettyWebSocketClient

class SpringJettyWebSocketClientAutobahnTest : AutobahnClientTestSuite("krossbow-spring-jetty-client") {

    override fun provideClient(): WebSocketClient = SpringJettyWebSocketClient
}
