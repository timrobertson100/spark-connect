### POC Spark connect

A quick POC illustrating a lightweight codebase that uses Spark Connect to reuse a Spark cluster.

This reads a set of parquet files, and then with a simple WS client with no caching, 
distincts and looks up the verbatim names, and then joins them back to the full record
to write as a CSV. When calling over the internet, Svampeatlas of 1M records takes around 
22 secs (would be quicker on GBIF network).

Care was taken with dependencies to ensure a small footprint (<1MB). 

```
SET mapreduce.job.reduces=10;
CREATE TABLE tim.svampeatlas STORED AS parquet AS
SELECT gbifID, scientificName, decimalLatitude, decimalLongitude
FROM prod_h.occurrence 
WHERE datasetKey='84d26682-f762-11e1-a439-00145eb45e9a'
GROUP BY gbifID, scientificName, decimalLatitude, decimalLongitude;
```

Download and start a Spark cluster.
Input data can be downloaded from https://download.gbif.org/tim/svampeatlas/

´´´
wget https://dlcdn.apache.org/spark/spark-3.5.4/spark-3.5.4-bin-hadoop3.tgz        
tar -xvf spark-3.5.4-bin-hadoop3.tgz
cd spark-3.5.4-bin-hadoop3
export SPARK_LOCAL_IP="127.0.0.1"
export JAVA_HOME="/usr/libexec/java_home -v 17"
export SPARK_DAEMON_MEMORY="4G"
./sbin/start-connect-server.sh --packages org.apache.spark:spark-connect_2.12:3.5.4
´´´

```
mvn package
```

Afterwards can run the `Test` class in the IDEA using:

1. Enable and add an environment variable with `--add-opens=java.base/java.nio=ALL-UNNAMED` 
2. Adding provided dependencies to the Classpath
3. Changing the absolute location of the Jar file in the code


