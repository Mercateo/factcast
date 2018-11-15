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
package org.factcast.spring.boot.autoconfigure.client.cache;

import org.factcast.client.cache.CachingFactCast;
import org.factcast.client.cache.CachingFactLookup;
import org.factcast.core.FactCast;
import org.factcast.core.store.FactStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.NonNull;

/**
 * Spring configuration class that provides a CachingFactCast by wrapping a
 * FactCast instance.
 *
 * @author <uwe.schaefer@mercateo.com>
 */
@Configuration
// to exclude from coverage analysis
@ConditionalOnClass(CachingFactCast.class)
public class CachingFactCastAutoConfiguration {

    @Bean
    public CachingFactCast cachingFactCast(FactCast fc, CachingFactLookup fl) {
        return new CachingFactCast(fc, fl);
    }

    @Bean
    public CachingFactLookup cachingFactLookup(@NonNull FactStore store) {
        return new CachingFactLookup(store);
    }
}
