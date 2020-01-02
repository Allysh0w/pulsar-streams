## PULSAR Docs

#### Consumers subscriptions mode:


- Exclusive Mode (default subscription mode): 
In exclusive mode, only a single consumer is allowed to attach to the subscription. 
If more than one consumer attempts to subscribe to a topic using the same subscription, the consumer receives an error.
![Exclusive mode consumer](https://pulsar.apache.org/docs/assets/pulsar-exclusive-subscriptions.png)

##
- Failover Mode: 
In failover mode, multiple consumers can attach to the same subscription. 
The consumers will be lexically sorted by the consumer's name and the first consumer will initially be the only one receiving messages. 
This consumer is called the master consumer.
When the master consumer disconnects, all (non-acked and subsequent) messages will be delivered to the next consumer.
![Failover mode consumer](https://pulsar.apache.org/docs/assets/pulsar-failover-subscriptions.png)

##
- Shared Mode: 
In shared or round robin mode, multiple consumers can attach to the same subscription. 
Messages are delivered in a round robin distribution across consumers, and any given message is delivered to only one consumer. 
When a consumer disconnects, all the messages that were sent to it and not acknowledged will be rescheduled for sending to the remaining consumers.

![Shared mode consumer](https://pulsar.apache.org/docs/assets/pulsar-shared-subscriptions.png)

```
Limitations of shared mode:
There are two important things to be aware of when using shared mode:

Message ordering is not guaranteed.
You cannot use cumulative acknowledgment with shared mode.
```
##

- Key-Shared
In Key_Shared mode, multiple consumers can attach to the same subscription. 
Messages are delivered in a distribution across consumers and message with same key or same ordering key are delivered to only one consumer. 
No matter how many times the message is re-delivered, it is delivered to the same consumer. 
When a consumer connected or disconnected will cause served consumer change for some key of message.

![Key-Shared mode consumer](https://pulsar.apache.org/docs/assets/pulsar-key-shared-subscriptions.png)

