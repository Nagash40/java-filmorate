package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import java.util.Collection;

@Validated
@RestController
@RequestMapping("/directors")
@Slf4j
@AllArgsConstructor
public class DirectorController {

    private final FilmService service;

    @GetMapping("/{id}")
    public ResponseEntity<Director> getDirector(@PathVariable int id) {
        return ResponseEntity.ok(service.getDirector(id));
    }

    @GetMapping
    public ResponseEntity<Collection<Director>> getDirectors() {
        return ResponseEntity.ok(service.getDirectors());
    }

    @PostMapping
    public ResponseEntity<Director> addDirector(@Valid @RequestBody Director director) {
        ResponseEntity<Director> response = new ResponseEntity<>(service.addDirector(director), HttpStatus.CREATED);

        log.debug("Добавлен новый режиссер: {}", response.getBody());
        return response;
    }

    @PutMapping
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody Director director) {
        director = service.updateDirector(director);
        log.debug("Обновлены данные о режиссере с id={}", director.getId());

        return ResponseEntity.ok(director);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeDirector(@PathVariable int id) {
        service.removeDirector(id);
        log.debug("Удалены данные о режиссере с id={}", id);
    }
}
