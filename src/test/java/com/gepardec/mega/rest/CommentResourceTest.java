package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.mapper.CommentMapper;
import com.gepardec.mega.rest.model.CommentDto;
import com.gepardec.mega.rest.model.NewCommentEntryDto;
import com.gepardec.mega.service.api.CommentService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test")
@OidcSecurity(claims = {
        @Claim(key = "email", value = "test@gepardec.com")
})
class CommentResourceTest {

    @Inject
    CommentMapper mapper;

    @InjectMock
    UserContext userContext;

    @InjectMock
    CommentService commentService;

    @Test
    void setCommentStatusDone_whenPOST_thenReturnsStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .post("/comments/finish")
                .then().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void finish_whenUserNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON).put("/comments/finish")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void finish_whenValid_thenReturnsUpdatedNumber() {
        when(commentService.finish(any(Comment.class))).thenReturn(1);

        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Comment comment = Comment.builder()
                .id(0L)
                .message("Pausen eintragen!")
                .authorEmail("no-reply@gepardec.com")
                .state(EmployeeState.IN_PROGRESS)
                .build();

        final int updatedCount = given().contentType(ContentType.JSON)
                .body(comment)
                .put("/comments/finish")
                .as(Integer.class);

        assertThat(updatedCount).isEqualTo(1);
    }

    @Test
    void getAllCommentsForEmployee_whenMethodPOST_thenReturnsStatusMETHOD_NOT_ALLOWED() {
        given().contentType(ContentType.JSON)
                .queryParam("email", "no-reply@gmx.at")
                .queryParam("releasedate", "2020-10-01")
                .post("/comments/getallcommentsforemployee")
                .then().assertThat().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void getAllCommentsForEmployee_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON)
                .queryParam("email", "no-reply@gmx.at")
                .queryParam("releasedate", "2020-10-01")
                .get("/comments/getallcommentsforemployee")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void getAllCommentsForEmployee_whenInvalidEmail_thenReturnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON)
                .queryParam("email", "noreplygmx.at")
                .queryParam("releasedate", "2020-10-01")
                .get("/comments/getallcommentsforemployee")
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getAllCommentsForEmployee_whenEmailIsMissing_thenReturnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON)
                .queryParam("releasedate", "2020-10-01")
                .get("/comments/getallcommentsforemployee")
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getAllCommentsForEmployee_whenReleaseDateIsMissing_thenReturnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON)
                .queryParam("email", "no-reply@gmx.at")
                .get("/comments/getallcommentsforemployee")
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void getAllCommentsForEmployee_whenValid_thenReturnsListOfCommentsForEmployee() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Comment comment = Comment.builder().id(0L).message("Pausen eintragen!").authorEmail("no-reply@gepardec.com").state(EmployeeState.IN_PROGRESS).build();
        when(commentService.findCommentsForEmployee(anyString(), any(YearMonth.class)))
                .thenReturn(List.of(comment));

        List<CommentDto> comments = given().contentType(ContentType.JSON)
                .queryParam("email", "no-reply@gmx.at")
                .queryParam("date", "2020-10")
                .get("/comments/getallcommentsforemployee")
                .as(new TypeRef<>() {
                });

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0)).isEqualTo(mapper.mapToDto(comment));
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void newCommentForEmployee_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        given().contentType(ContentType.JSON)
                .post("/comments")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void newCommentForEmployee_whenInvalidRequest_thenReturnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON)
                .post("/comments")
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void newCommentForEmployee_whenValid_thenReturnsCreatedComment() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        when(commentService.create(
                ArgumentMatchers.anyLong(),
                any(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                any(YearMonth.class)
        )).thenReturn(Comment.builder().message("Pausen eintragen!").build());

        NewCommentEntryDto newCommentEntryDto = NewCommentEntryDto.builder()
                .comment("Pausen eintragen!")
                .employeeEmail("max.mustermann@gepardec.com")
                .stepId(2L)
                .assigneeEmail("no-reply@gepardec.com")
                .currentMonthYear("2020-10-01")
                .project("")
                .build();

        //WHEN
        CommentDto createdComment = given().contentType(ContentType.JSON)
                .body(newCommentEntryDto)
                .post("/comments")
                .as(CommentDto.class);

        //THEN
        assertThat(createdComment).isNotNull();
        assertThat(createdComment.getMessage()).isEqualTo(newCommentEntryDto.getComment());
    }

    @Test
    void deleteComment_whenIdIsMissing_thenReturnsHttpStatusMethodNotAllowed() {
        given().contentType(ContentType.JSON)
                .delete("/comments")
                .then().assertThat().statusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void deleteComment_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        given().contentType(ContentType.JSON)
                .delete("/comments/1")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void deleteComment_whenValid_thenReturnsTrue() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        when(commentService.delete(ArgumentMatchers.anyLong()))
                .thenReturn(Boolean.TRUE);

        Boolean result = given().contentType(ContentType.JSON)
                .delete("/comments/1")
                .as(Boolean.class);

        assertThat(result).isEqualTo(Boolean.TRUE);
    }

    @Test
    @TestSecurity
    @OidcSecurity
    void updateCommentForEmployee_whenNotLogged_thenReturnsHttpStatusUNAUTHORIZED() {
        given().contentType(ContentType.JSON)
                .put("/comments")
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void updateCommentForEmployee_whenInvalidRequest_henReturnsHttpStatusBAD_REQUEST() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        given().contentType(ContentType.JSON)
                .put("/comments")
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void updateCommentForEmployee_whenValid_thenReturnsUpdatedComment() {
        final User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Comment comment = Comment.builder().id(1L).message("Zeiten prüfen").build();
        when(commentService.update(ArgumentMatchers.anyLong(), anyString()))
                .thenReturn(comment);

        CommentDto updatedComment = given().contentType(ContentType.JSON)
                .body(comment)
                .put("/comments")
                .as(CommentDto.class);

        assertThat(updatedComment).isEqualTo(mapper.mapToDto(comment));
    }

    private com.gepardec.mega.domain.model.User createUserForRole(final Role role) {
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        return com.gepardec.mega.domain.model.User.builder()
                .userId("1")
                .dbId(1)
                .email("no-reply@gepardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(roles)
                .build();
    }
}
