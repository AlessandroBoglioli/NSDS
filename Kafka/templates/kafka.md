# Apache Kafka: Fundamentals and Exactly-Once Semantics (EOS) 🔒

This document outlines the core components of the Kafka streaming platform and details the critical concepts required to achieve Exactly-Once Semantics (EOS) in distributed applications.

***

## 1. Core Kafka Components

| Component | Description | Relevance to Your Code |
| :--- | :--- | :--- |
| **Broker** | A single Kafka server. A cluster consists of multiple brokers to ensure high availability. | All files connect to a broker via `serverAddr = "localhost:9092"`. |
| **Topic** | A logical feed/category for published records. Topics are divided into partitions. | Managed by the `TopicManager.java` class. |
| **Partition** | The basic unit of parallelism. Partitions are ordered, immutable sequences of records, each identified by a sequential **Offset**. | `TopicManager` defines the number of partitions (`defaultTopicPartitions = 2`). |
| **Consumer Group** | A set of consumers that share a `GROUP_ID`. Kafka ensures that each message is consumed by only **one** consumer within the group. | Used in `BasicConsumer.java` via `ConsumerConfig.GROUP_ID_CONFIG`. |
| **Replication** | Copies of a partition stored on different brokers to prevent data loss. The `replicationFactor` defines data durability. | Configured by the `TopicManager` to ensure data resilience. |

***

## 2. Producer Reliability Guarantees

The producer's configuration is key to setting the delivery guarantee: At Most Once, At Least Once, or Exactly Once.

### A. Acknowledgment (`acks`)

This setting controls the durability tradeoff between latency and safety.

| `acks` Setting | Outcome | Safety Guarantee |
| :--- | :--- | :--- |
| `0` | Producer does not wait for any acknowledgment. | **At Most Once** (Fastest, highest risk of data loss). |
| `1` | Producer waits only for the **leader broker** to acknowledge the write. | **At Least Once** (Faster than `all`, still risks loss if the leader fails). |
| `all` (`-1`) | Producer waits for the leader and **all In-Sync Replicas (ISRs)** to acknowledge. | **At Least Once** (Highest durability, standard for critical data). |

### B. Idempotence

Idempotence is the first step toward EOS. It prevents message duplication caused by **producer retries** of the *same message batch*.

* **How it Works:** The producer is assigned a **Producer ID (PID)** and sends a **Sequence Number** with each batch. The broker tracks the last successfully committed sequence number for that PID and Partition, discarding any repeated sequence numbers.
* **Enabling Idempotence (`IdempotentProducer.java`):**
    ```java
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));
    ```

***

## 3. Exactly-Once Semantics (EOS) via Transactions

EOS guarantees that a **Read-Process-Write** operation (consuming data, processing it, and writing the result) is atomic. This is achieved using Kafka Transactions, which coordinate both the output messages and the input offset commits.

### A. Transactional Producer

This producer can combine multiple sends and an offset commit into a single atomic unit.

* **Requirement:** The producer must have a unique `transactional.id`.
* **Configuration (`TransactionalProducer.java`):**
    ```java
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "myTransactionalId");
    // This method must be called before using any transaction methods
    producer.initTransactions();
    ```
* **API Flow:** The transaction sequence is `producer.beginTransaction()`, followed by one or more `producer.send()`, and concluded with either `producer.commitTransaction()` (Success: makes messages visible) or `producer.abortTransaction()` (Failure: discards all messages).

### B. The Atomic Forwarder Pattern

The `AtomicForwarder.java` class demonstrates the core of EOS: committing the consumer's position (offset) and the producer's output messages together.

1.  Start Transaction: `producer.beginTransaction()`
2.  Process and Send: Loop through input records, apply logic, and call `producer.send()` for the output topic.
3.  Commit Offsets: The crucial step where the producer informs Kafka to commit the consumer offsets:
    ```java
    producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
    ```
4.  Commit Transaction: `producer.commitTransaction()`

### C. Transactional Consumer Isolation

For the EOS guarantee to hold, consumers must be configured to ignore messages from transactions that were aborted.

* **Configuration (`TransactionalConsumer.java`):**
    ```java
    // This is the default setting for EOS applications
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    ```
* **`read_committed`:** The consumer only reads messages that were part of a successfully committed transaction, ensuring data integrity.
* **`read_uncommitted`:** The consumer reads all messages, including those from aborted transactions, risking duplicates or incomplete data.
```eof

You can now download the file named **`kafka_basic.md`** using the link in the document editor.