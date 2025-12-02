package it.polimi.spark.streaming.event_time;

import it.polimi.spark.common.Consts;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class WindowedCount {
    public static void main(String[] args) throws Exception {
        final String master = args.length > 0 ? args[0] : Consts.MASTER_ADDR_DEFAULT;

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("WindowedCount")
                .getOrCreate();

        // Create DataFrame from a rate source.
        // A rate source generates records with a timestamp and a value at a fixed rate.
        // It is used for testing and benchmarking.
        final Dataset<Row> inputRecords = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 1)
                .load();
        spark.sparkContext().setLogLevel("ERROR");

        inputRecords.withWatermark("timestamp", "1 hour");

        // Q1: group by window (size 30 seconds, slide 10 seconds)

        // TODO

        final StreamingQuery query1 = inputRecords
                .withColumn("value", col("value").mod(100))
                .withColumn("timestamp", date_format(col("timestamp"), "HH:mm:ss"))
                .groupBy(
                        window(col("timestamp"), "30 seconds", "10 seconds")
                )
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            query1.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        // Q2: group by window (size 30 seconds, slide 10 seconds) and value

        // TODO

        final StreamingQuery query2 = inputRecords
                .withColumn("value", col("value").mod(100))
                .withColumn("timestamp", date_format(col("timestamp"), "HH:mm:ss"))
                .groupBy(
                        window(col("timestamp"), "30 seconds", "10 seconds"),
                        col("value")
                )
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            query1.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        // Q3: group only by value

        // TODO

        final StreamingQuery query3 = inputRecords
                .withColumn("value", col("value").mod(100))
                .withColumn("timestamp", date_format(col("timestamp"), "HH:mm:ss"))
                .groupBy(
                        col("value")
                )
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            query1.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }

}