package it.polimi.lab1.ex2;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.col;

/**
 * Implement an iterative algorithm that implements the transitive closure of friends
 * (people that are friends of friends of ... of my friends).
 *
 * Set the value of the flag useCache to see the effects of caching.
 */
public class FriendsComputation {
    private static final boolean useCache = true;

    public static void main(String[] args) throws IOException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "FriendsCache" : "FriendsNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("person", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("friend", DataTypes.StringType, false));
        final StructType schema = DataTypes.createStructType(fields);

        Dataset<Row> input1 = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(schema)
                .csv(filePath + "files/friends/friends.csv")
                .distinct();

        long oldCount = 0;
        long newCount = input1.count();

        Dataset<Row> result = input1;

        while (newCount > oldCount) {
            oldCount = newCount;

            Dataset<Row> join = result.alias("t1")
                    .join(result.alias("t2"),
                            col("t1.friend").equalTo(col("t2.person")))
                    .select(
                            col("t1.person").as("person"),
                            col("t2.friend").as("friend")
                    )
                    .distinct();

            result = result.union(join).distinct();

            if (useCache)
                result.cache();

            newCount = result.count();
        }

        result.show();

        spark.close();
    }
}
