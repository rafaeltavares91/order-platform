package dev.study.orderplatform.order.adapter.out.identifier;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UuidV7OrderIdGeneratorTest {

    @Test
    void generatesRfc9562VersionSevenIdentifiers() {
        var id = new UuidV7OrderIdGenerator().nextId();

        assertThat(id.version()).isEqualTo(7);
        assertThat(id.variant()).isEqualTo(2);
    }
}
