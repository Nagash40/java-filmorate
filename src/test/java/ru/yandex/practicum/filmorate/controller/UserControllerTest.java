package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void addNewUserTest() {
        User user = makeDefaultUser();
        ResponseEntity<User> response = restTemplate.postForEntity(getActualURI(), user, User.class);
        user.setId(1);
        user.setName(user.getLogin());

        assertEquals(user, response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void shouldNotAddUserWithInvalidEmail() {
        User userWithWrongEmail = makeCustomUser(
                "mail",
                null,
                null);
        assertEquals(HttpStatus.BAD_REQUEST,
                restTemplate.postForEntity(getActualURI(), userWithWrongEmail, User.class).getStatusCode());
    }

    @Test
    public void shouldNotAddUserWithInvalidWhitespaceLogin() {
        User userWithWrongEmail = makeCustomUser(
                null,
                "lo gin",
                null);
        assertEquals(HttpStatus.BAD_REQUEST,
                restTemplate.postForEntity(getActualURI(), userWithWrongEmail, User.class).getStatusCode());
    }

    @Test
    public void shouldNotAddUserWithInvalidBlankLogin() {
        User userWithWrongEmail = makeCustomUser(
                null,
                "",
                null);
        assertEquals(HttpStatus.BAD_REQUEST,
                restTemplate.postForEntity(getActualURI(), userWithWrongEmail, User.class).getStatusCode());
    }

    @Test
    public void shouldReplaceBlankNameWithLogin() {
        User userWithWrongEmail = makeCustomUser(
                null,
                "login",
                null);
        assertEquals("login",
                restTemplate.postForEntity(getActualURI(), userWithWrongEmail, User.class).getBody().getName());
    }

    @Test
    public void shouldNotAddUserWithInvalidBirthday() {
        User userWithWrongEmail = makeCustomUser(
                null,
                null,
                LocalDate.ofYearDay(40000, 20));
        assertEquals(HttpStatus.BAD_REQUEST,
                restTemplate.postForEntity(getActualURI(), userWithWrongEmail, User.class).getStatusCode());
    }

    @Test
    public void updateUserTest() {
        User user = addDefaultUser();
        ResponseEntity<User> response = restTemplate.exchange(
                getActualURIWithQuery(),
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class,
                1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void shouldNotUpdateNonexistentUser() {
        User user = makeDefaultUser(1);
        ResponseEntity<User> response = restTemplate.exchange(
                getActualURIWithQuery(),
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class,
                1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getUsersTest() {
        User user = addDefaultUser();
        ResponseEntity<List<User>> response = restTemplate.exchange(
                getActualURI(),
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(user), response.getBody());
    }

    private User makeDefaultUser() {
        return new User(
                "mail@mail.ru",
                "NickName",
                LocalDate.parse("1946-08-20")
        );
    }

    private User makeDefaultUser(int id) {
        User user = makeDefaultUser();
        user.setId(id);
        return user;
    }

    private User makeCustomUser(String email, String login, LocalDate birthday) {
        if (email == null) {
            email = "mail@mail.ru";
        }
        if (login == null) {
            login = "Nick Name";
        }
        if (birthday == null) {
            birthday = LocalDate.parse("1946-08-20");
        }

        return new User(
                email,
                login,
                birthday);
    }

    private User addDefaultUser() {
        return restTemplate.postForObject(getActualURI(), makeDefaultUser(), User.class);
    }

    private String getActualURI() {
        return "http://localhost:" + port + "/users";
    }

    private String getActualURIWithQuery() {
        return getActualURI() + "/?id={id}";
    }
}
