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
package org.gbif.ws.client;

import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Taxonomy {
  interface WebService {
    @GET("/v1/species/match2")
    Call<MatchResponse> speciesMatch(@Query("scientificName") String scientificName);
  }

  private WebService service;

  private static Taxonomy instance;

  public static Taxonomy getInstance() {
    if (instance == null) {
      instance = new Taxonomy("https://api.gbif.org/");
    }
    return instance;
  }

  private Taxonomy(String baseUrl) {
    service =
        new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(
                new OkHttpClient.Builder()
                    .connectionPool(new ConnectionPool(12, 5L, TimeUnit.MINUTES))
                    .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WebService.class);
  }

  @SneakyThrows
  public String match(String scientificName) {
    Call<MatchResponse> call = service.speciesMatch(scientificName);
    retrofit2.Response<MatchResponse> response = call.execute();
    if (response.isSuccessful() && response.body() != null) {
      return response.body().getUsage() == null ? null : response.body().getUsage().getName();
    } else {
      return null;
    }
  }
}
