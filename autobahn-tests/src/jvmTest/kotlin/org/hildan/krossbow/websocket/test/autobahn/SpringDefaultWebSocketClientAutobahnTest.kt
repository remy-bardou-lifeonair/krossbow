package org.hildan.krossbow.websocket.test.autobahn

import org.hildan.krossbow.websocket.WebSocketClient
import org.hildan.krossbow.websocket.spring.SpringDefaultWebSocketClient

// FIXME: investigate RejectedExecutionException on plain echo tests (thread pool is shutting down but the websocket
//  should not be closed at that time). Only happens on the CI
class SpringDefaultWebSocketClientAutobahnTest : AutobahnClientTestSuite("krossbow-spring-default-client") {

    override fun provideClient(): WebSocketClient = SpringDefaultWebSocketClient
}
