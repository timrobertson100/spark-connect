/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.spark.pipelines;

import org.gbif.spark.pipelines.udf.TaxonomyUDF;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.apache.spark.sql.*;

public class Test implements Serializable {

  public static void main(String[] args) throws IOException, AnalysisException {
    SparkSession spark = SparkSession.builder().remote("sc://localhost").getOrCreate();
    spark.addArtifact(
        "/Users/tsj442/dev/git/timrobertson100/spark-connect/target/connect-demo-1.0.0-3.5.4.jar");
    TaxonomyUDF.register(spark, "match");

    spark.read().parquet("/tmp/svampeatlas/*").createOrReplaceTempView("source");

    // Distinct the names, look them up and then join them back to the records
    Dataset<Row> names =
        spark.sql(
            """
          WITH names AS (
            SELECT scientificname, match(scientificname) AS i
            FROM (SELECT /*+ REPARTITION(12) */ DISTINCT scientificname FROM source)
          )
          SELECT
            /*+ REBALANCE */
            s.*, n.i.taxonId AS taxonId, n.i.scientificName AS interpretedScientificName
          FROM
            source s JOIN names n ON s.scientificname = n.scientificname
        """);

    names.write().csv("/tmp/interpreted-" + UUID.randomUUID());
    spark.close();
  }
}
