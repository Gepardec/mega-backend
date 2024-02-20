package com.gepardec.mega.application.interceptor;

import com.gepardec.mega.application.exception.ForbiddenException;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.UserContext;
import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MegaMegaRolesAllowedInterceptorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserContext userContext;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private InvocationContext invocationContext;

    @InjectMocks
    private MegaRolesAllowedInterceptor megaRolesAllowedInterceptor;

    @Test
    void invoke_whenAnnotationOnClassLevel_thenUsesClassLevelAnnotation() throws Exception {
        when(invocationContext.getMethod().getAnnotation(any())).thenReturn(null);
        when(invocationContext.getTarget()).thenReturn(new TargetWithAnnotation());
        when(userContext.getUser().getRoles()).thenReturn(Set.of(Role.EMPLOYEE));

        megaRolesAllowedInterceptor.intercept(invocationContext);

        verify(invocationContext, times(1)).proceed();
    }

    @Test
    void invoke_whenNoAnnotationOnMethodAndClassLevel_thenThrowsNullpointerException() {
        when(invocationContext.getMethod().getAnnotation(any())).thenReturn(null);
        when(invocationContext.getTarget()).thenReturn(new TargetNoAnnotation());

        assertThatThrownBy(() -> megaRolesAllowedInterceptor.intercept(invocationContext))
                .isInstanceOf(NullPointerException.class);
    }

    //FIXME 1.9 NullPointerException is thrown instead of the expected ForbiddenException
    @Test
    @Disabled
    void intercept_whenNotLogged_thenThrowsForbiddenException() {
        when(invocationContext.getMethod().getAnnotation(any())).thenReturn(createAnnotation(new Role[]{Role.EMPLOYEE}));
        assertThatThrownBy(() -> megaRolesAllowedInterceptor.intercept(invocationContext)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void invoke_whenLoggedAndNotInRole_thenThrowsForbiddenException() {
        when(invocationContext.getMethod().getAnnotation(any())).thenReturn(createAnnotation(new Role[]{Role.OFFICE_MANAGEMENT}));
        when(userContext.getUser().getRoles()).thenReturn(Set.of(Role.EMPLOYEE));

        assertThatThrownBy(() -> megaRolesAllowedInterceptor.intercept(invocationContext)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void invoke_whenLoggedAndInRoleMethodAnnotated_thenThrowsForbiddenException() throws Exception {
        when(invocationContext.getMethod().getAnnotation(any())).thenReturn(createAnnotation(Role.values()));
        when(userContext.getUser().getRoles()).thenReturn(Set.of(Role.EMPLOYEE));

        megaRolesAllowedInterceptor.intercept(invocationContext);

        verify(invocationContext, times(1)).proceed();
    }

    private MegaRolesAllowed createAnnotation(final Role[] roles) {
        return new MegaRolesAllowed() {
            @Override
            public Role[] value() {
                return roles;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return MegaRolesAllowed.class;
            }
        };
    }

    @MegaRolesAllowed(Role.EMPLOYEE)
    private static class TargetWithAnnotation {

    }

    private static class TargetNoAnnotation {

    }
}
