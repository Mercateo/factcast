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
package org.factcast.client.grpc.cli.cmd;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.factcast.client.grpc.cli.util.ConsoleFactObserver;
import org.factcast.client.grpc.cli.util.Parser.Options;
import org.factcast.core.FactCast;
import org.factcast.core.spec.FactSpec;
import org.factcast.core.store.FactStore;
import org.factcast.core.subscription.SubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CatchupTest {
    @Mock
    FactStore fs;

    FactCast fc;

    @Test
    void testCatchup() throws Exception {
        Catchup cmd = new Catchup();
        String ns = "foo";
        UUID startId = new UUID(0, 1);

        cmd.from = startId;
        cmd.ns = ns;

        fc = spy(FactCast.from(fs));
        Options opt = new Options();

        cmd.runWith(fc, opt);

        SubscriptionRequest r = SubscriptionRequest.catchup(FactSpec.ns(cmd.ns)).from(startId);
        verify(fc).subscribeToFacts(eq(r), any(ConsoleFactObserver.class));
    }
}
