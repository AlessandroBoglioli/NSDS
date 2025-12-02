package it.polimi.lab1.eval2022;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Int;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class Cities {
    public static void main(String[] args) throws TimeoutException, IOException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        // TODO: add code here if necessary

        Dataset<Row> q1 = null; // TODO: query Q1

        final Dataset<Row> conjoinedDataset = citiesPopulation
                .join(citiesRegions, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .select(citiesPopulation.col("id"),
                        citiesPopulation.col("city"),
                        citiesRegions.col("region"),
                        citiesPopulation.col("population"));

        conjoinedDataset.cache();

        q1 = conjoinedDataset
                .groupBy("region")
                .sum("population");

        q1.show();

        Dataset<Row> q2 = null; // TODO: query Q2

        q2 = conjoinedDataset
                .groupBy("region")
                .agg(count("city"), max("population"));

        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        // TODO: add code here to produce the output for query Q3

        population.cache();
        long popSum  = population.reduce((a, b) -> a+b);

        int iteration = 0;
        while (popSum < 100000000) {
            iteration++;
            population = population.map(p -> p > 1000 ? p+(int)(p*0.01) : p-(int)(p*0.1));
            population.cache();
            popSum = population.reduce((a, b) -> a+b);
            System.out.println("Year: " + iteration + " total population: " + popSum);
        }


        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        StreamingQuery q4 = null; // TODO query Q4

        q4 = bookings
                .join(conjoinedDataset, bookings.col("value").equalTo(conjoinedDataset.col("id")))
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        conjoinedDataset.col("region")
                )
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}