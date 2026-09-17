package dev.study.orderplatform.identifier;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UuidV7IdentifierGeneratorTest {

    @Test
    void generatesRfc9562VersionSevenIdentifiers() {
        var id = new UuidV7IdentifierGenerator().nextId();

        assertThat(id.version()).isEqualTo(7);
        assertThat(id.variant()).isEqualTo(2);
    }
}
