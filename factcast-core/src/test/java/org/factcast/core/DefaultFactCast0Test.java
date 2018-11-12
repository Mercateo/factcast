package org.factcast.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import org.factcast.core.spec.FactSpec;
import org.factcast.core.store.FactStore;
import org.factcast.core.subscription.Subscription;
import org.factcast.core.subscription.SubscriptionRequest;
import org.factcast.core.subscription.SubscriptionRequestTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class DefaultFactCast0Test {
    @Mock
    private FactStore store;

    @InjectMocks
    private DefaultFactCast uut;

    @Captor
    private ArgumentCaptor<UUID> cuuid;

    @Captor
    private ArgumentCaptor<SubscriptionRequestTO> csr;

    @Captor
    private ArgumentCaptor<List<Fact>> cfacts;

    @Test
    public void testSubscribeToFacts() {
        when(store.subscribe(csr.capture(), any())).thenReturn(mock(Subscription.class));

        final UUID since = UUID.randomUUID();
        SubscriptionRequest r = SubscriptionRequest.follow(FactSpec.forMark())
                .or(FactSpec.ns(
                        "some").type("type"))
                .from(since);

        uut.subscribeToFacts(r, f -> {
        });

        verify(store).subscribe(any(), any());

        final SubscriptionRequestTO req = csr.getValue();
        assertTrue(req.continuous());
        assertFalse(req.idOnly());
        assertEquals(since, req.startingAfter().get());
        assertFalse(req.ephemeral());
    }

    @Test
    public void testSubscribeToIds() {
        when(store.subscribe(csr.capture(), any())).thenReturn(mock(Subscription.class));

        SubscriptionRequest r = SubscriptionRequest.follow(FactSpec.forMark())
                .or(FactSpec.ns(
                        "some").type("type"))
                .fromScratch();

        uut.subscribeToIds(r, f -> {
        });

        verify(store).subscribe(any(), any());

        final SubscriptionRequestTO req = csr.getValue();
        assertTrue(req.continuous());
        assertTrue(req.idOnly());
        assertFalse(req.ephemeral());
    }

    @Test
    public void testFetchById() {
        when(store.fetchById(cuuid.capture())).thenReturn(Optional.empty());

        final UUID id = UUID.randomUUID();
        uut.fetchById(id);

        assertSame(id, cuuid.getValue());
    }

    @Test
    public void testPublish() {
        doNothing().when(store).publish(cfacts.capture());

        final Test0Fact f = new Test0Fact();
        uut.publish(f);

        final List<Fact> l = cfacts.getValue();
        assertEquals(1, l.size());
        assertTrue(l.contains(f));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoId() {
        uut.publish(new Test0Fact().id(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNamespace() {
        uut.publish(new Test0Fact().ns(null));
    }

    @Test(expected = NullPointerException.class)
    public void testPublishOneNull() {
        uut.publish((Fact) null);
    }

    @Test(expected = NullPointerException.class)
    public void testPublishManyNull() {
        uut.publish((List<Fact>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testFetchByIdNull() {
        uut.fetchById(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeIdsNull() {
        uut.subscribeToIds(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFactsNull() {
        uut.subscribeToFacts(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeIds1stArgNull() {
        uut.subscribeToIds(null, f -> {
        });
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFacts1stArgNull() {
        uut.subscribeToFacts(null, f -> {
        });
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeIds2ndArgNull() {
        uut.subscribeToIds(SubscriptionRequest.follow(FactSpec.forMark()).fromScratch(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testSubscribeFacts2ndArgNull() {
        uut.subscribeToFacts(SubscriptionRequest.follow(FactSpec.forMark()).fromScratch(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testDefaultFactCast() {
        new DefaultFactCast(null);
    }

    @Test(expected = NullPointerException.class)
    public void testpublishWithMarkOneNull() {
        uut.publishWithMark((Fact) null);
    }

    @Test(expected = NullPointerException.class)
    public void testpublishWithMarkManyNull() {
        uut.publishWithMark((List<Fact>) null);
    }

    @Test
    public void testSerialOf() {
        when(store.serialOf(any(UUID.class))).thenReturn(OptionalLong.empty());
        UUID id = UUID.randomUUID();
        uut.serialOf(id);
        verify(store).serialOf(same(id));
    }

    @Test(expected = NullPointerException.class)
    public void testSerialOfNull() {
        uut.serialOf(null);
    }
}
