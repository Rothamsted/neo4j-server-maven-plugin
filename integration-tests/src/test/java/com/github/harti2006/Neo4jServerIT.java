/*
 * Copyright 2015 André Hartmann (github.com/harti2006)
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

package com.github.harti2006;

import static java.net.URI.create;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class Neo4jServerIT {

    @Test
    public void testNeo4jServerIsRunning() throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        final RequestEntity<Void> request = RequestEntity.get(create(System.getProperty("neo4j-server.url")))
                .accept(APPLICATION_JSON)
                .build();
        final ResponseEntity<String> responseEntity = restTemplate.exchange(request, String.class);

        assertEquals(OK, responseEntity.getStatusCode());
        System.out.println(responseEntity);
    }
}
