/**
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
package org.factcast.spring.store.inmem;

import org.factcast.store.inmem.InMemFactStore;
import org.springframework.beans.factory.DisposableBean;

@SuppressWarnings("deprecation")
public class DisposableInMemFactStore extends InMemFactStore implements DisposableBean {

    public void destroy() throws Exception {
        shutdown();
    }
}
