package dev.study.orderplatform.identifier;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.github.f4b6a3.uuid.UuidCreator;

import dev.study.orderplatform.domain.port.OrderIdGenerator;

@Component
public class UuidV7OrderIdGenerator implements OrderIdGenerator {

    @Override
    public UUID nextId() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
