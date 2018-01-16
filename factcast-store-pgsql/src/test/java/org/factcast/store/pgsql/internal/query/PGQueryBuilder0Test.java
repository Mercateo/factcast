package org.factcast.store.pgsql.internal.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;

import org.factcast.core.subscription.SubscriptionRequestTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class PGQueryBuilder0Test {

    private PGQueryBuilder uut;

    @Mock
    private SubscriptionRequestTO request;

    @Before
    public void init() {
        initMocks(this);
        uut = new PGQueryBuilder(request);
    }

    @Test
    public void testExtractMetaJson() throws Exception {

        // given
        String key = "k";
        Object value = "v";

        // when
        String json = uut.extractMetaJson(key, value);

        // then
        assertThat(json).isEqualTo("{\"meta\":{\"k\":\"v\" }}");

    }

    @Test
    public void testExtractMetaJson_with_multi_ids() throws Exception {

        // given
        String key = "k";
        Object value = Arrays.asList("v1", "v2", "v3");

        // when
        String json = uut.extractMetaJson(key, value);

        // then
        assertThat(json).isEqualTo("{\"meta\":{\"k\":[\"v1\",\"v2\",\"v3\"] }}");

    }

}
