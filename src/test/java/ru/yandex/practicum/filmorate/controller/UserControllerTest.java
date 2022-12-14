package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase
@Sql(scripts = "classpath:schematest.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:datatest.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void addNewUserTest() {
        User user = makeDefaultUser();
        ResponseEntity<User> response = restTemplate.postForEntity(getActualURI(), user, User.class);
        user.setName(user.getLogin());
        user.setId(1);

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
        user.setId(1);
        ResponseEntity<User> response = restTemplate.exchange(
                getActualURI(),
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void shouldNotUpdateNonexistentUser() {
        User user = makeDefaultUser(1);
        ResponseEntity<User> response = restTemplate.exchange(
                getActualURI(),
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void addFriendTest() {
        addDefaultUser();
        addDefaultUser();

        HttpStatus responseCode = addFriendDefault().getStatusCode();

        assertEquals(HttpStatus.OK, responseCode);
    }

    @Test
    public void shouldThrowExceptionForAddingSelfToFriends() {
        addDefaultUser();
        HttpStatus responseCode = restTemplate.exchange(
                        getActualURI() + "/1/friends/1",
                        HttpMethod.PUT,
                        new HttpEntity<>(null),
                        User.class)
                .getStatusCode();

        assertEquals(HttpStatus.CONFLICT, responseCode);
    }

    @Test
    public void shouldThrowExceptionForAlreadyAddedFriend() {
        addDefaultUser();
        addDefaultUser();

        addFriendDefault();
        HttpStatus responseCode = addFriendDefault().getStatusCode();

        assertEquals(HttpStatus.CONFLICT, responseCode);
    }

    @Test
    public void removeFriendTest() {
        addDefaultUser();
        addDefaultUser();
        addFriendDefault();

        HttpStatus responseCode = restTemplate.exchange(
                getActualURI() + "/1/friends/2",
                HttpMethod.DELETE,
                new HttpEntity<>(null),
                User.class)
                .getStatusCode();

        assertEquals(HttpStatus.OK, responseCode);
    }

    @Test
    public void shouldThrowExceptionForNotFoundFriend() {
        addDefaultUser();
        addDefaultUser();

        HttpStatus responseCode = restTemplate.exchange(
                        getActualURI() + "/1/friends/2",
                        HttpMethod.DELETE,
                        new HttpEntity<>(null),
                        User.class)
                .getStatusCode();

        assertEquals(HttpStatus.NOT_FOUND, responseCode);
    }

    @Test
    public void getUsersTest() {
        User user = addDefaultUser();
        user.setId(1);
        ResponseEntity<Collection<User>> response = restTemplate.exchange(
                getActualURI(),
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(user), response.getBody());
    }

    @Test
    public void getFriendsListTest() {
        ResponseEntity<Collection<User>> response;

        User user1 = addDefaultUser();
        user1.setId(1);
        User user2 = addDefaultUser();
        user2.setId(2);
        addFriendDefault();

        response = restTemplate.exchange(
                getActualURI() + "/1/friends",
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(user2), response.getBody());

        restTemplate.exchange(
                getActualURI() + "/2/friends/1",
                HttpMethod.PUT,
                new HttpEntity<>(null),
                User.class);
        response = restTemplate.exchange(
                getActualURI() + "/2/friends",
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(user1), response.getBody());
    }

    @Test
    public void getCommonFriendsTest() {
        addDefaultUser();
        addDefaultUser();
        User user = addDefaultUser();
        user.setId(3);

        restTemplate.exchange(
                getActualURI() + "/1/friends/3",
                HttpMethod.PUT,
                new HttpEntity<>(null),
                User.class);

        restTemplate.exchange(
                getActualURI() + "/2/friends/3",
                HttpMethod.PUT,
                new HttpEntity<>(null),
                User.class);

        ResponseEntity<Collection<User>> response = restTemplate.exchange(
                getActualURI() + "/1/friends/common/2",
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(user), response.getBody());
    }

    @Test
    public void removeUserTest() {
        addDefaultUser();

        HttpStatus statusCode = restTemplate.exchange(
                getActualURI() + "/1",
                HttpMethod.DELETE,
                new HttpEntity<>(null),
                User.class
        ).getStatusCode();

        assertEquals(HttpStatus.OK, statusCode);

        statusCode = restTemplate.getForEntity(getActualURI() + "/1",
                User.class
                ).getStatusCode();

        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void getFilmRecommendationTest() {
        ResponseEntity<Collection<Film>> response;

        Film film1 = addDefaultFilm();
        film1.setId(1);
        Film film2 = addDefaultFilm();
        film2.setId(2);
        Film film3 = addDefaultFilm();
        film3.setId(3);
        User user1 = addDefaultUser();
        user1.setId(1);
        User user2 = addDefaultUser();
        user2.setId(2);

        addLikeDefault(1, 1);
        addLikeDefault(1, 2);
        addLikeDefault(2, 2);
        addLikeDefault(3, 2);

        response = restTemplate.exchange(
                getActualURI() + "/1/recommendations",
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    private Film makeDefaultFilm() {
        return new Film(
                0,
                "nisi eiusmod",
                "adipisicing",
                LocalDate.parse("1967-03-25"),
                100,
                0,
                new MPA(1, "G"));
    }

    private ResponseEntity<Film> addLikeDefault(int filmId, int userId) {
        return restTemplate.exchange(
                "http://localhost:" + port + "/films/" + filmId + "/like/"+ userId,
                HttpMethod.PUT,
                new HttpEntity<>(null),
                Film.class);
    }

    private User makeDefaultUser() {
        return new User(
                0,
                "mail@mail.ru",
                "NickName",
                "",
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
                0,
                email,
                login,
                "",
                birthday);
    }

    private User addDefaultUser() {
        return restTemplate.postForObject(getActualURI(), makeDefaultUser(), User.class);
    }

    private Film addDefaultFilm() {
        return restTemplate.postForObject("http://localhost:" + port + "/films", makeDefaultFilm(), Film.class);
    }

    private String getActualURI() {
        return "http://localhost:" + port + "/users";
    }

    private ResponseEntity<User> addFriendDefault() {
        return restTemplate.exchange(
                        getActualURI() + "/1/friends/2",
                        HttpMethod.PUT,
                        new HttpEntity<>(null),
                        User.class);
    }
}
