package it.polimi.nsds.kafka.eval;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

// Group number: 47
// Group members: Alessandro Boglioli, Riccardo Colombi, Pietro Limoni

// Is it possible to have more than one partition for topics "sensors1" and "sensors2"? yes, it's possible, but if we have more than a merger, the number of topics' partition must be the same

// Is there any relation between the number of partitions in "sensors1" and "sensors2"? yes, if we have more than a merger

// Is it possible to have more than one instance of Merger? yes, it's possible but only in some cases (see next answer)
// If so, what is the relation between their group id?
/*
    - different group id, different number of topics' partitions: it's possible to have different instances of Merger
    - different group id, same number of topics' partitions: it's possible to have different instances of Merger
    - same group id, different number of topics' partitions: it's not possible to have different instances of Merger,
        because we cannot ensure that the same key for different topics will be read by the same instance of Merger
    - same group id, same number of topics' partitions: it's possible to have different instances of Merger
 */

// Is it possible to have more than one partition for topic "merged"? yes, it's possible

// Is it possible to have more than one instance of Validator? yes, it's possible
// If so, what is the relation between their group id?
/*
    - if they have the same group id is guaranteed that we don't have copies in the output topic
    - if they don't have the same group id, the output topic will contain duplicates
 */

public class Consumers47 {
    public static void main(String[] args) {
        String serverAddr = "localhost:9092";
        int stage = Integer.parseInt(args[0]);
        String groupId = args[1];

        switch (stage) {
            case 0:
                new Merger(serverAddr, groupId).execute();
                break;
            case 1:
                new Validator(serverAddr, groupId).execute();
                break;
            case 2:
                System.err.println("Wrong stage");
        }
    }

    private static class Merger {
        private final String serverAddr;
        private final String consumerGroupId;

        private static final String input1 = "sensors1";
        private static final String input2 = "sensors2";
        private static final String output = "merged";
        private static final int commitEvery = 40;

        public Merger(String serverAddr, String consumerGroupId) {
            this.serverAddr = serverAddr;
            this.consumerGroupId = consumerGroupId;
        }

        public void execute() {
            // Consumer
            final Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

            KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Arrays.asList(input1, input2));

            // Producer
            final Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

            final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);

            HashMap<String, Integer> keyValuePairs1 = new HashMap<>();
            HashMap<String, Integer> keyValuePairs2 = new HashMap<>();
            int numConsumed = 0;

            while (true) {
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

                for (final ConsumerRecord<String, Integer> record : records) {
                    System.out.println("Partition: " + record.partition() +
                            "\tOffset: " + record.offset() +
                            "\tKey: " + record.key() +
                            "\tValue: " + record.value() +
                            "\tTopic: " + record.topic()
                    );

                    if (record.topic().equals(input1)) {
                        keyValuePairs1.put(record.key(), record.value());
                    }
                    else {
                        keyValuePairs2.put(record.key(), record.value());
                    }

                    final int sum = keyValuePairs1.getOrDefault(record.key(), 0)
                            + keyValuePairs2.getOrDefault(record.key(), 0);

                    producer.send(new ProducerRecord<>(output, record.key(), sum));

                    numConsumed++;

                    if (numConsumed == commitEvery) {
                        numConsumed = 0;
                        consumer.commitSync();
                    }
                }
            }
        }
    }

    private static class Validator {
        private final String serverAddr;
        private final String consumerGroupId;
        private static final String producerTransactionalId = "forwarderTransactionalId";
        private static final String inputTopic = "merged";
        private static final String outputTopic1 = "output1";
        private static final String outputTopic2 = "output2";

        public Validator(String serverAddr, String consumerGroupId) {
            this.serverAddr = serverAddr;
            this.consumerGroupId = consumerGroupId;
        }

        public void execute() {
            // Consumer
            final Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

            KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(inputTopic));

            // Producer
            final Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId);
            producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

            final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);

            producer.initTransactions();

            while (true) {
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

                for (final ConsumerRecord<String, Integer> record : records) {
                    producer.beginTransaction();

                    System.out.println("Partition: " + record.partition() +
                            "\tOffset: " + record.offset() +
                            "\tKey: " + record.key() +
                            "\tValue: " + record.value()
                    );

                    producer.send(new ProducerRecord<>(outputTopic1, record.key(), record.value()));
                    producer.send(new ProducerRecord<>(outputTopic2, record.key(), record.value()));

                    producer.commitTransaction();
                }
            }
        }
    }
}