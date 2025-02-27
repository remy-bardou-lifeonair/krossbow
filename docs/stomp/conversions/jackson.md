# STOMP with Jackson

The `krossbow-stomp-jackson` module is a JVM-only extension of `krossbow-stomp-core` that provides new APIs to
send and receive properly typed classes, and automatically convert them to/from the JSON bodies of STOMP frames
by leveraging [Jackson](https://github.com/FasterXML/jackson) and [jackson-module-kotlin](https://github.com/FasterXML/jackson-module-kotlin).

The main addition is the extension function `StompSession.withJacksonConversions()`, which turns your `StompSession`
into a `StompSessionWithClassConversions`.
This new session type has additional methods that use reflection to convert your objects into JSON and back:

```kotlin
StompClient().connect(url).withJacksonConversions().use { session ->
    session.convertAndSend("/some/destination", Person("Bob", 42)) 

    val messages: Flow<MyMessage> = session.subscribe<MyMessage>("/some/topic/destination")
    val firstMessage: MyMessage = messages.first()

    println("Received: $firstMessage")
}
```

## Using a custom `ObjectMapper`

Jackson is highly configurable, and it's often useful to configure the `ObjectMapper` manually.

The `withJacksonConversions()` method takes an optional `ObjectMapper` parameter, so you can configure it as you please:

```kotlin
val customObjectMapper: ObjectMapper = jacksonObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
        .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)

val client = StompClient().connect(url)
val session = client.withJacksonConversions(customObjectMapper)
```

## Dependency

You will need to declare the following Gradle dependency to add these capabilities
(you don't need the core module anymore as it is transitively brought by this one):

```kotlin
implementation("org.hildan.krossbow:krossbow-stomp-jackson:{{ git.tag }}")
```

This dependency transitively brings Jackson {{ versions.jackson }} with the [Kotlin module](https://github.com/FasterXML/jackson-module-kotlin).