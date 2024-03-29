package com.gepardec.mega.application.producer;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;

import java.lang.reflect.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@QuarkusTest
class LoggerProducerTest {

    @Inject
    LoggerProducer producer;

    private InjectionPoint ip;

    private Bean<?> bean;

    private Member member;

    @BeforeEach
    void beforeEach() {
        ip = spy(InjectionPoint.class);
        bean = spy(Bean.class);
        member = spy(Member.class);
    }

    @Test
    void createLogger_whenBeanAvailable_thenBeanClassIsUsed() {
        Class<?> clazz = Object.class;
        OngoingStubbing<Class<?>> beanClassStubbing = when(bean.getBeanClass());
        beanClassStubbing.thenReturn(clazz);
        OngoingStubbing<Bean<?>> beanStubbing = when(ip.getBean());
        beanStubbing.thenReturn(bean);

        final Logger logger = producer.createLogger(ip);

        assertThat(logger.getName()).isEqualTo(Object.class.getName());
    }

    @Test
    void createLogger_whenBeanNull_thenMemberDeclaringClassIsUsed() {
        Class<?> clazz = Object.class;
        OngoingStubbing<Class<?>> memberClassStubbing = when(member.getDeclaringClass());
        memberClassStubbing.thenReturn(clazz);
        when(ip.getMember()).thenReturn(member);

        final Logger logger = producer.createLogger(ip);

        assertThat(logger.getName()).isEqualTo(Object.class.getName());
    }

    @Test
    void createLogger_whenBeanAndMemberNull_thenDefaultNameIsUsed() {
        when(ip.getBean()).thenReturn(null);
        when(ip.getMember()).thenReturn(null);

        final Logger logger = producer.createLogger(ip);

        assertThat(logger.getName()).isEqualTo("default");
    }
}
