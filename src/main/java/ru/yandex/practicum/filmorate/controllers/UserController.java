package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import java.util.Collection;

@Validated
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserStorage storage;
    private final UserService service;

    @Autowired
    public UserController(UserStorage storage, UserService service) {
        this.storage = storage;
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        return ResponseEntity.ok(storage.getUser(id));
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getUsersList() {
        return ResponseEntity.ok(storage.getUsers());
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<User>> getFriendsList(@PathVariable int id) {
        return ResponseEntity.ok(service.getFriendsList(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return ResponseEntity.ok(service.getCommonFriends(id, otherId));
    }

    @PostMapping
    public ResponseEntity<User> addNewUser(@Valid @RequestBody User user) {
        storage.addUser(user);
        checkName(user);
        log.debug("Добавлен новый пользователь: {}", user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        int id = user.getId();

        storage.updateUser(user, id);
        checkName(user);
        log.debug("Обновлена информация о пользователе: {}", user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        service.addFriend(id, friendId);
        log.debug("Пользователи с id={} и id={} добавлены в списки друзей друг друга.", friendId, id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id, @PathVariable int friendId) {
        service.removeFriend(id, friendId);
        log.debug("Пользователи с id={} и id={} удалены из списков друзей друг друга.", friendId, id);
    }

    private static void checkName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
        log.debug("Пустое имя пользователя с id={} изменено на значение логина.", user.getId());
    }
}