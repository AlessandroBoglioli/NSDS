package lab.eval2024.eval;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Future;

// Group number:
// 47

// Group members:
// Boglioli Alessandro, Colombi Riccardo, Limoni Pietro

// Number of partitions for inputTopic (min, max):
// Number of partitions for outputTopic1 (min, max):
// Number of partitions for outputTopic2 (min, max):

// Number of instances of Consumer1 (and groupId of each instance) (min, max):
// Number of instances of Consumer2 (and groupId of each instance) (min, max):

// Please, specify below any relation between the number of partitions for the topics
// and the number of instances of each Consumer

public class Consumers47 {
    public static void main(String[] args) {
        String serverAddr = "localhost:9092";
        int consumerId = Integer.valueOf(args[0]);
        String groupId = args[1];
        if (consumerId == 1) {
            Consumer1 consumer = new Consumer1(serverAddr, groupId);
            consumer.execute();
        } else if (consumerId == 2) {
            Consumer2 consumer = new Consumer2(serverAddr, groupId);
            consumer.execute();
        }
    }

    private static class Consumer1 {
        private final String serverAddr;
        private final String consumerGroupId;

        private static final String inputTopic = "inputTopic";
        private static final String outputTopic = "outputTopic1";

        private static final String producerTransactionalId = "forwarderTransactionalId";

        public Consumer1(String serverAddr, String consumerGroupId) {
            this.serverAddr = serverAddr;
            this.consumerGroupId = consumerGroupId;
        }

        public void execute() {
            // Consumer
            final Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

            // TODO: add properties if needed

            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
            // The consumer does not commit automatically, but within the producer transaction
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));

            KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(inputTopic));

            // Producer
            final Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);

            // TODO: add properties if needed

            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId);
            producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

            final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);
            producer.initTransactions();
            producer.beginTransaction();

            // TODO: add code if needed

            List<Integer> values = new ArrayList<>();
            Integer sum = 0;

            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();

            while (true) {
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
                for (final ConsumerRecord<String, Integer> record : records) {
                    // TODO: add code to process records

                    System.out.println("Received record :  Topic -> " + record.topic() +
                            " Partition -> " + record.partition() +
                            " Key -> " + record.key() +
                            " Value -> " + record.value());

                    values.add(record.value());
                    if (values.size() == 10) {

                        // message elaboration and sending
                        for (int j = 0; j < 10; j ++)
                            sum += values.get(j);
                        producer.send(new ProducerRecord<>(outputTopic, "sum", sum));
                        sum = 0;
                        values.clear();

                        // Offset commit
                        final long lastOffset = record.offset();
                        map.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(lastOffset + 1));
                        producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
                        producer.commitTransaction();
                        producer.beginTransaction();
                    }
                }

            }
        }
    }

    private static class Consumer2 {
        private final String serverAddr;
        private final String consumerGroupId;

        private static final String inputTopic = "inputTopic";
        private static final String outputTopic = "outputTopic2";

        public Consumer2(String serverAddr, String consumerGroupId) {
            this.serverAddr = serverAddr;
            this.consumerGroupId = consumerGroupId;
        }

        public void execute() {
            // Consumer
            final Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

            // TODO: add properties if needed

            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(true));
            consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(15000));
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());

            KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(inputTopic));

            // Producer
            final Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);

            // TODO: add properties if needed

            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            final KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProps);

            // TODO: add code if needed

            Map<String, Integer> occurrences = new HashMap<>();
            Map<String, Integer> sums = new HashMap<>();

            while (true) {
                final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
                for (final ConsumerRecord<String, Integer> record : records) {
                    // TODO: add code to process records

                    System.out.println("Received record :  Topic -> " + record.topic() +
                            " Partition -> " + record.partition() +
                            " Key -> " + record.key() +
                            " Value -> " + record.value());

                    occurrences.put(record.key(), occurrences.getOrDefault(record.key(), 0) + 1);
                    sums.put(record.key(), sums.getOrDefault(record.key(), 0) + record.value());

                    if (occurrences.get(record.key()) == 10) {
                        producer.send(new ProducerRecord<>(outputTopic, record.key(), sums.get(record.key())));
                        occurrences.put(record.key(), 0);
                        sums.put(record.key(), 0);
                    }
                }
            }
        }
    }
}