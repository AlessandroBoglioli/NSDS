# Apache Kafka: Fundamentals and Exactly-Once Semantics (EOS)

Apache Kafka is a distributed streaming platform designed for building
real-time data pipelines and streaming applications. It allows for
high-throughput, low-latency processing of data feeds.

## 1. Core Concepts

### Component Descriptions

-   **Topic**: A category or feed name to which records are published.
    Topics are logically multi-producer, multi-consumer.
-   **Partition**: Topics are divided into a set of partitions.
    Partitions are ordered, immutable sequences of records, each
    assigned a sequential ID called the *offset*. This is the
    fundamental unit of parallelism.
-   **Broker**: A single Kafka server. A cluster typically consists of
    multiple brokers.
-   **Producer**: Application that publishes (writes) records to a Kafka
    topic.
-   **Consumer**: Application that subscribes to one or more topics and
    processes published records.
-   **Consumer Group**: A set of consumers cooperating to consume data.
    Each partition is consumed by only one consumer within a group.

## 2. Producer Acknowledgement Guarantees

  -----------------------------------------------------------------------
  acks          Description                   Guarantees
  ------------- ----------------------------- ---------------------------
  **0**         Producer does not wait for    *At Most Once* (data loss
                acknowledgement               possible)

  **1**         Producer waits for leader to  *At Least Once*
                acknowledge                   (duplication possible)

  **all / -1**  Leader and all ISR replicas   *At Least Once* (strongest
                acknowledge                   durability)
  -----------------------------------------------------------------------

## 3. Exactly-Once Semantics (EOS)

EOS ensures that messages are delivered and processed *exactly once*,
even in failure scenarios. Kafka achieves this through:

-   **Idempotence**
-   **Transactions**

### A. Idempotent Producer

Idempotence ensures no duplicate writes for a single producer → single
partition workflow.

Mechanisms: - **Producer ID (PID)** - **Sequence Number** for each batch

Configuration:

``` java
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
```

### B. Transactions (Atomic Read-Process-Write)

Transactions ensure atomic operations across multiple partitions.

#### Transactional Producer Example

``` java
props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);

producer.initTransactions();
producer.beginTransaction();
// send messages...
producer.commitTransaction(); // or producer.abortTransaction();
```

#### Transactional Consumer

``` java
props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
```

### C. Atomic Forwarder Pattern

Ensures that consuming input and writing output happen atomically.

``` java
producer.beginTransaction();
// process and send output records...
producer.sendOffsetsToTransaction(offsetMap, consumer.groupMetadata());
producer.commitTransaction();
```

## 4. Topic Manager (AdminClient API)

Used to create, delete, and manage Kafka topics.

Example:

``` java
NewTopic newTopic = new NewTopic(topicName, topicPartitions, replicationFactor);
adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
```

------------------------------------------------------------------------

Generated for GitHub upload.
