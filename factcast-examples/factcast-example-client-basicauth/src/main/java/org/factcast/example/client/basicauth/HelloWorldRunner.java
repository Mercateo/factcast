/*
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 *
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
package org.factcast.example.client.basicauth;

import java.util.*;

import org.factcast.core.*;
import org.factcast.core.spec.*;
import org.factcast.core.subscription.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;

import lombok.*;

@SuppressWarnings("ALL")
@RequiredArgsConstructor
@Component
public class HelloWorldRunner implements CommandLineRunner {

    @NonNull
    private final FactCast fc;

    @Override
    public void run(String... args) throws Exception {

        Fact fact = Fact.builder().ns("smoke").type("foo").build("{\"bla\":\"fasel\"}");
        fc.publish(fact);
        System.out.println("published " + fact);

        Optional<Fact> fetchById = fc.fetchById(fact.id());
        System.out.println("fetch by id returns payload:" + fetchById.get().jsonPayload());

        Subscription sub = fc.subscribeToIds(SubscriptionRequest.catchup(FactSpec.ns("smoke"))
                .fromScratch(),
                System.out::println).awaitCatchup(5000);

        sub.close();

    }

}
