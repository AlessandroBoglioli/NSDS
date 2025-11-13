package lab.ex2;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Use this main Class only to set up the topic in the kafka server
 */

public class TopicManagerTwoPartitions {
    private static final String TOPIC_NAME = "topicA";
    private static final int TARGET_PARTITIONS = 2;
    private static final short REPLICATION_FACTOR = 1;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(props)) {
            ensureTopicWithPartitions(adminClient, TOPIC_NAME, TARGET_PARTITIONS, REPLICATION_FACTOR);
        }
    }

    private static void ensureTopicWithPartitions(AdminClient adminClient, String topicName,
                                                  int targetPartitions, short replicationFactor)
            throws ExecutionException, InterruptedException {

        // Ottieni informazioni sui topic esistenti
        Set<String> existingTopics = adminClient.listTopics().names().get();

        if (!existingTopics.contains(topicName)) {
            System.out.println("Il topic non esiste. Creazione di '" + topicName + "' con " + targetPartitions + " partizioni...");
            NewTopic newTopic = new NewTopic(topicName, targetPartitions, replicationFactor);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            System.out.println("Topic creato correttamente.");
            return;
        }

        // Se esiste, controlla quante partizioni ha
        DescribeTopicsResult describeResult = adminClient.describeTopics(Collections.singletonList(topicName));
        Map<String, KafkaFuture<TopicDescription>> topicDescMap = describeResult.topicNameValues();
        TopicDescription description = topicDescMap.get(topicName).get();

        int currentPartitions = description.partitions().size();

        if (currentPartitions == targetPartitions) {
            System.out.println("Il topic '" + topicName + "' ha già " + targetPartitions + " partizione/i. Nessuna modifica necessaria.");
        } else if (currentPartitions < targetPartitions) {
            System.out.println("Aggiungo partizioni: da " + currentPartitions + " a " + targetPartitions);
            adminClient.createPartitions(Collections.singletonMap(topicName, NewPartitions.increaseTo(targetPartitions))).all().get();
            System.out.println("Numero di partizioni aggiornato correttamente.");
        } else {
            System.out.println("Il topic ha più partizioni (" + currentPartitions + ") di quante richieste (" + targetPartitions + "). Kafka non permette di ridurre le partizioni.");
            System.out.println("Nessuna modifica effettuata. Se vuoi davvero ridurle, cancella manualmente il topic e ricrealo.");
        }
    }
}
