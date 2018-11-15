/**
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.factcast.store.pgsql.internal.catchup.paged;

import java.util.concurrent.atomic.AtomicLong;

import org.factcast.core.Fact;
import org.factcast.core.subscription.SubscriptionImpl;
import org.factcast.core.subscription.SubscriptionRequestTO;
import org.factcast.store.pgsql.PGConfigurationProperties;
import org.factcast.store.pgsql.internal.PGPostQueryMatcher;
import org.factcast.store.pgsql.internal.catchup.PGCatchupFactory;
import org.factcast.store.pgsql.internal.query.PGFactIdToSerialMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
// not code in here, just generated @nonnull checks
public class PGPagedCatchUpFactory implements PGCatchupFactory {

    @NonNull
    final JdbcTemplate jdbc;

    @NonNull
    final PGConfigurationProperties props;

    @NonNull
    final PGFactIdToSerialMapper serMapper;

    public PGPagedCatchup create(@NonNull SubscriptionRequestTO request,
            @NonNull PGPostQueryMatcher postQueryMatcher,
            @NonNull SubscriptionImpl<Fact> subscription, @NonNull AtomicLong serial) {
        return new PGPagedCatchup(jdbc, props, serMapper, request, postQueryMatcher, subscription,
                serial);
    }
}
