package dev.study.orderplatform.domain.model;

import java.util.UUID;

public interface IdentifierGenerator {

    UUID nextId();
}
