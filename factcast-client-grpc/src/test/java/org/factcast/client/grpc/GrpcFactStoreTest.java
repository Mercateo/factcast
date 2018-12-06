package org.factcast.client.grpc;

import static org.factcast.core.TestHelper.expectNPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.factcast.core.Fact;
import org.factcast.core.TestFact;
import org.factcast.core.store.RetryableException;
import org.factcast.core.subscription.SubscriptionRequestTO;
import org.factcast.core.subscription.observer.FactObserver;
import org.factcast.grpc.api.conv.ProtoConverter;
import org.factcast.grpc.api.conv.ProtocolVersion;
import org.factcast.grpc.api.conv.ServerConfig;
import org.factcast.grpc.api.gen.FactStoreProto.MSG_Empty;
import org.factcast.grpc.api.gen.FactStoreProto.MSG_Facts;
import org.factcast.grpc.api.gen.FactStoreProto.MSG_Notification;
import org.factcast.grpc.api.gen.FactStoreProto.MSG_SubscriptionRequest;
import org.factcast.grpc.api.gen.RemoteFactStoreGrpc.RemoteFactStoreBlockingStub;
import org.factcast.grpc.api.gen.RemoteFactStoreGrpc.RemoteFactStoreStub;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.springboot.autoconfigure.grpc.client.AddressChannelFactory;

@ExtendWith(MockitoExtension.class)
public class GrpcFactStoreTest {

    @InjectMocks
    private GrpcFactStore uut;

    @Mock
    private RemoteFactStoreBlockingStub blockingStub;

    @Mock
    private RemoteFactStoreStub stub;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SubscriptionRequestTO req;

    private ProtoConverter conv = new ProtoConverter();

    @Captor
    private ArgumentCaptor<MSG_Facts> factsCap;

    @Test
    void testFetchByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(blockingStub.fetchById(eq(conv.toProto(id)))).thenReturn(conv.toProto(Optional
                .empty()));
        Optional<Fact> fetchById = uut.fetchById(id);
        assertFalse(fetchById.isPresent());
    }

    @Test
    void testFetchByIdFound() {
        UUID id = UUID.randomUUID();
        when(blockingStub.fetchById(eq(conv.toProto(id)))).thenReturn(conv.toProto(Optional.of(Fact
                .builder()
                .ns("test")
                .build("{}"))));
        Optional<Fact> fetchById = uut.fetchById(id);
        assertTrue(fetchById.isPresent());
    }

    @Test
    void testPublish() {
        when(blockingStub.publish(factsCap.capture())).thenReturn(MSG_Empty.newBuilder().build());
        final TestFact fact = new TestFact();
        uut.publish(Collections.singletonList(fact));
        verify(blockingStub).publish(any());
        final MSG_Facts pfacts = factsCap.getValue();
        Fact published = conv.fromProto(pfacts.getFact(0));
        assertEquals(fact.id(), published.id());
    }

    static class SomeException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    @Test
    void testPublishPropagatesException() {
        assertThrows(SomeException.class, () -> {
            when(blockingStub.publish(any())).thenThrow(new SomeException());
            uut.publish(Collections.singletonList(Fact.builder().build("{}")));
        });
    }

    @Test
    void testFetchByIdPropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            when(blockingStub.fetchById(any())).thenThrow(new StatusRuntimeException(
                    Status.UNAVAILABLE));
            uut.fetchById(UUID.randomUUID());
        });
    }

    @Test
    void testPublishPropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            when(blockingStub.publish(any())).thenThrow(new StatusRuntimeException(
                    Status.UNAVAILABLE));
            uut.publish(Collections.singletonList(Fact.builder().build("{}")));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void testCancelPropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            ClientCall<MSG_SubscriptionRequest, MSG_Notification> call = mock(ClientCall.class);
            doThrow(new StatusRuntimeException(Status.UNAVAILABLE)).when(call).cancel(any(), any());
            uut.cancel(call);
        });
    }

    @Test
    void testSerialOfPropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            when(blockingStub.serialOf(any())).thenThrow(new StatusRuntimeException(
                    Status.UNAVAILABLE));
            uut.serialOf(mock(UUID.class));
        });
    }

    @Test
    void testInitializePropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            when(blockingStub.handshake(any())).thenThrow(new StatusRuntimeException(
                    Status.UNAVAILABLE));
            uut.initialize();
        });
    }

    @Disabled
    @Test
    void testConfigureGZipPropagatesRetryableExceptionOnUnavailableStatus() {
        fail("unimplemented");
    }

    @Disabled
    @Test
    void testConfigureLZ4PropagatesRetryableExceptionOnUnavailableStatus() {
        fail("unimplemented");
    }

    @Test
    void testEnumerateNamespacesPropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            when(blockingStub.enumerateNamespaces(any())).thenThrow(new StatusRuntimeException(
                    Status.UNAVAILABLE));
            uut.enumerateNamespaces();
        });
    }

    @Test
    void testEnumerateTypesPropagatesRetryableExceptionOnUnavailableStatus() {
        assertThrows(RetryableException.class, () -> {
            when(blockingStub.enumerateTypes(any())).thenThrow(new StatusRuntimeException(
                    Status.UNAVAILABLE));
            uut.enumerateTypes("ns");
        });
    }

    @Test
    void testConstruction() {
        expectNPE(() -> new GrpcFactStore((AddressChannelFactory) null));
        expectNPE(() -> new GrpcFactStore((Channel) null));
        expectNPE(() -> new GrpcFactStore(mock(RemoteFactStoreBlockingStub.class), null));
        expectNPE(() -> new GrpcFactStore(null, mock(RemoteFactStoreStub.class)));
        expectNPE(() -> new GrpcFactStore(null, null));
    }

    @Test
    void testSubscribeNull() {
        expectNPE(() -> uut.subscribe(null, mock(FactObserver.class)));
        expectNPE(() -> uut.subscribe(null, null));
        expectNPE(() -> uut.subscribe(mock(SubscriptionRequestTO.class), null));
    }

    @Test
    void testMatchingProtocolVersion() {
        when(blockingStub.handshake(any())).thenReturn(conv.toProto(ServerConfig.of(ProtocolVersion
                .of(1, 0, 0), new HashMap<>())));
        uut.initialize();
    }

    @Test
    void testCompatibleProtocolVersion() {
        when(blockingStub.handshake(any())).thenReturn(conv.toProto(ServerConfig.of(ProtocolVersion
                .of(1, 1, 0), new HashMap<>())));
        uut.initialize();
    }

    @Test
    void testIncompatibleProtocolVersion() {
        Assertions.assertThrows(IncompatibleProtocolVersions.class, () -> {
            when(blockingStub.handshake(any())).thenReturn(conv.toProto(ServerConfig.of(
                    ProtocolVersion.of(2, 0, 0), new HashMap<>())));
            uut.initialize();
        });
    }

    @Test
    void testInitializationExecutesOnlyOnce() {
        when(blockingStub.handshake(any())).thenReturn(conv.toProto(ServerConfig.of(ProtocolVersion
                .of(1, 1, 0), new HashMap<>())));
        uut.initialize();
        uut.initialize();
        verify(blockingStub, times(1)).handshake(any());
    }
}
