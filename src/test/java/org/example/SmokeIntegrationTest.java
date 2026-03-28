package org.example;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SmokeIntegrationTest extends IntegrationTestBase {
    @Test
    void dockerWorks() {
        assertThat(true).isTrue();
    }
}