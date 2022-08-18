package ru.yandex.practicum.filmorate.service.film.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InMemoryFilmService implements FilmService {

    private final FilmStorage storage;

    @Override
    public Film getFilm(int id) {
        return storage.getFilm(id);
    }

    @Override
    public Collection<Film> getFilms() {
        return storage.getFilms();
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        Collection<Film> popularFilmsList = List.copyOf(
                storage.getFilms().stream()
                        .dropWhile(film -> film.getRate() == 0)
                        .sorted(Comparator.comparing(Film::getRate).reversed())
                        .limit(count)
                        .collect(Collectors.toList()));

        if (popularFilmsList.isEmpty()) {
            popularFilmsList = storage.getFilms().stream()
                    .limit(count)
                    .collect(Collectors.toList());
        }
        return popularFilmsList;
    }

    @Override
    public void addLike(int targetFilmId, int userId) {
        Film film = storage.getFilm(targetFilmId);
        film.addLike(userId);
    }

    @Override
    public void addFilm(Film film) {
        storage.addFilm(film);
    }

    @Override
    public void updateFilm(Film film, int id) {
        storage.updateFilm(film, id);
    }

    @Override
    public void removeLike(int targetFilmId, int userId) {
        Film film = storage.getFilm(targetFilmId);
        film.removeLike(userId);
    }
}
