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

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class DownloadDemo implements Serializable {

  public static void main(String[] args) throws IOException, AnalysisException {
    SparkSession spark = SparkSession.builder().remote("sc://localhost").getOrCreate();

    boolean createTable = true;

    if (createTable) {
      // TODO: Fix Arrow serialization dependency issue to test if table exists
      // spark.catalog().listTables("global_temp").collectAsList(); // see if table is already there

      Dataset<Row> s = spark.read().parquet("/tmp/svampeatlas/*");
      s.createOrReplaceGlobalTempView("occurrence_svampe");
      spark.catalog().cacheTable("global_temp.occurrence_svampe");
    }

    spark
        .sql(
            "SELECT /*+ REBALANCE */ * FROM global_temp.occurrence_svampe WHERE scientificname like 'A%'")
        .write()
        .csv("/tmp/download-" + UUID.randomUUID());
    spark.close();
  }
}
