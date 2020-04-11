# Krossbow

[![Bintray Download](https://img.shields.io/bintray/v/joffrey-bion/maven/krossbow-stomp-core)](https://bintray.com/joffrey-bion/maven/krossbow-stomp-core/_latestVersion)
[![Github Build](https://img.shields.io/github/workflow/status/joffrey-bion/krossbow/CI%20Build?label=build&logo=github)](https://github.com/joffrey-bion/krossbow/actions?query=workflow%3A%22CI+Build%22)
[![Travis Build](https://img.shields.io/travis/joffrey-bion/krossbow/master.svg?label=build&logo=travis)](https://travis-ci.org/joffrey-bion/krossbow)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/krossbow/blob/master/LICENSE)

A coroutine-based Kotlin multi-platform WebSocket client and [STOMP 1.2](https://stomp.github.io/index.html) client
 over web sockets.
 
## Supported targets

This project only supports the JVM 8/11 and JavaScript targets as of now, as it was primarily built for applications
 combining Kotlin/React frontend with Kotlin Spring Boot backend.

This project cannot be used on Android as of now because of its dependency on either Spring (JDK8) or JDK11's API, but
 this could change if the need arises (please open an issue if you'd like to see it happen).
Android support could be somewhat easily added by writing an adapter for web sockets on top of OkHttp's web sockets.

Adding support for the Native target may require a bit more effort. Contributions are welcome in this respect, though.

## Experimental status

***This project is experimental, meaning that there is no guarantee of backwards compatibility.*** 
Any part of the public API may change until version 1.0.0 is released.

This is mainly due to the fact that the project is young, but also because it has multiple dependencies on
 experimental libraries like [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) and 
 [Kotlinx IO](https://github.com/Kotlin/kotlinx-io).

## STOMP Usage

### Raw STOMP usage (without conversions)

This is how to create a client and interact with it:

```kotlin
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText

val client = StompClient() // custom WebSocketClient and other config can be passed in here
val session: StompSession = client.connect(url) // optional login/passcode can be provided here

session.use { // this: StompSession
    sendText("/some/destination", "Basic text message") 

    val subscription = subscribeText("/some/topic/destination")

    val firstMessage: String? = subscription.messages.receive()
    println("Received: $firstMessage")

    subscription.unsubscribe()
}
```

The `StompSession.use()` method here is similar to `Closeable.use()` and allows to disconnect automatically even in
 case of error.

If the STOMP session needs to be passed around and cannot be used in one place like this, it is possible to be explicit
using `try`/`finally`, and `disconnect()` manually:

```kotlin
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText

val session: StompSession = StompClient().connect(url)

try {
    session.sendText("/some/destination", "Basic text message") 

    val subscription = session.subscribeText("/some/topic/destination")

    val firstMessage: String? = subscription.messages.receive()
    println("Received: $firstMessage")

    subscription.unsubscribe()
} finally {
    session.disconnect()
}
```


### Using body conversions

Usually STOMP is used in conjunction with JSON bodies that are converted back and forth between objects.
Krossbow comes with built-in support for Kotlinx Serialization in order to support multiplatform conversions.

You will need to use the `krossbow-stomp-kxserialization` module to add these capabilities (you don't need the core
 module anymore as it is transitively brought by this one).

Call `withJsonConversions` to add conversions capabilities to your `StompSession`.
Then, use `convertAndSend` and `subscribe` overloads with serializers to use these conversions:

```kotlin
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.conversions.kxserialization.convertAndSend
import org.hildan.krossbow.stomp.conversions.kxserialization.subscribe
import org.hildan.krossbow.stomp.conversions.kxserialization.withJsonConversions

val session = StompClient().connect(url)
val jsonStompSession = session.withJsonConversions() // adds convenience methods for kotlinx.serialization's conversions

jsonStompSession.use {
    convertAndSend("/some/destination", MyPojo("Custom", 42), MyPojo.serializer()) 

    val subscription = subscribe("/some/topic/destination", MyMessage.serializer())
    val firstMessage: MyMessage = subscription.messages.receive()

    println("Received: $firstMessage")
    subscription.unsubscribe()
}
```

Note that `withJsonConversions()` takes an optional `Json` argument to customize the serialization configuration.

#### Using Jackson conversions (JVM only)

If you're only targeting the JVM, you can use Jackson instead of Kotlinx Serialization to use reflection instead of
 manually provided serializers.
 
You will need to use the `krossbow-stomp-jackson` module to add these capabilities (you don't need the core
 module anymore as it is transitively brought by this one).

```kotlin
StompClient().connect(url).withJacksonConversions().use {
    convertAndSend("/some/destination", MyPojo("Custom", 42)) 

    val subscription = subscribe<MyMessage>("/some/topic/destination")
    val firstMessage: MyMessage = subscription.messages.receive()

    println("Received: $firstMessage")
    subscription.unsubscribe()
}
```

You can use it with your own `ObjectMapper` this way:

```kotlin
val objectMapper = jacksonObjectMapper().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
val session = StompClient().connect(url).withJacksonConversions(objectMapper)
```

## Picking a web socket implementation

The `krossbow-websocket-api` artifact defines a general web socket API, and provides a basic JS implementation using the
 Browser's native web socket and a basic JVM implementation using Java 11's async WebSocket API.
Other artifacts provide more implementations supporting more platforms by depending on third party libraries:

| Artifact                    |           Browser          |           NodeJS           |                JVM8+ (blocking)               |   JVM11+ (async)   | Dependencies                                                                                                                                                                                   |
|-----------------------------|:--------------------------:|:--------------------------:|:---------------------------------------------:|:------------------:|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `krossbow-websocket-api`    |     :white_check_mark:     |                            |                                               | :white_check_mark: |                                                                                                                                                                                                |
| `krossbow-websocket-sockjs` | :eight_pointed_black_star: | :eight_pointed_black_star: |           :eight_pointed_black_star:          |                    | [sockjs-client](https://github.com/sockjs/sockjs-client), [Spring websocket](https://docs.spring.io/spring-framework/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/websocket.html) |
| `krossbow-websocket-spring` |                            |                            | :white_check_mark: :eight_pointed_black_star: |                    | [Spring websocket](https://docs.spring.io/spring-framework/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/websocket.html)                                                           |

:white_check_mark: supported with native web socket transport

:eight_pointed_black_star: supported using [SockJS](https://github.com/sockjs/sockjs-client) protocol (requires a SockJS server)

## Adding the dependency

All the dependencies are currently published to Bintray JCenter.
They are not yet available on npm yet.

If you are using STOMP and have no special requirement for the web socket implementation, `krossbow-websocket-api` 
doesn't need to be explicitly declared as dependency because it is transitively pulled by all `krossbow-stomp-xxx` 
artifacts.

### Common library

```kotlin
// common source set
implementation("org.hildan.krossbow:krossbow-stomp-core-metadata:$krossbowVersion")

// jvm source set
implementation("org.hildan.krossbow:krossbow-stomp-core-jvm:$krossbowVersion")

// js source set
implementation("org.hildan.krossbow:krossbow-stomp-core-js:$krossbowVersion")
```

## Project structure
 
This project contains the following modules:
- `krossbow-stomp-core`: the multiplatform STOMP client to use as a STOMP library in common, JVM or JS projects. It
 implements the STOMP 1.2 protocol on top of a websocket API defined by the `krossbow-websocket-api` module.
- `krossbow-stomp-jackson`: a superset of `krossbow-stomp-core` adding conversion features using Jackson
- `krossbow-stomp-kxserialization`: a superset of `krossbow-stomp-core` adding conversion features using Kotlinx
 Serialization library
- `krossbow-websocket-api`: a common WebSocket API that the STOMP client relies on, to enable the use of custom
 WebSocket clients. This also provides a default JS client implementations using the Browser's native WebSocket, and
  a JVM 11+ implementation using the async WebSocket API.
- `krossbow-websocket-sockjs`: a multiplatform `WebSocketClient` implementation for use with SockJS servers. It uses
 Spring's SockJSClient on JVM, and npm `sockjs-client` for JavaScript (NodeJS and browser).
- `krossbow-websocket-spring`: a JVM 8+ implementation of the web socket API using Spring's WebSocketClient. Provides
 both a normal WebSocket client and a SockJS one.
