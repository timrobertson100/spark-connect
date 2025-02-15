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
package org.gbif.spark.pipelines.udf;

import org.gbif.ws.client.Taxonomy;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;

public class TaxonomyUDF implements UDF1<String, Row> {
  private static Taxonomy TAXONOMY = Taxonomy.getInstance();

  public static void register(SparkSession spark, String name) {
    spark
        .udf()
        .register(
            name,
            new TaxonomyUDF(),
            DataTypes.createStructType(
                new StructField[] {
                  DataTypes.createStructField("taxonId", DataTypes.IntegerType, true),
                  DataTypes.createStructField("scientificName", DataTypes.StringType, true)
                }));
  }

  @Override
  public Row call(String verbatimName) throws Exception {
    String scientificName = TAXONOMY.match(verbatimName);
    return RowFactory.create(123, scientificName);
  }
}
