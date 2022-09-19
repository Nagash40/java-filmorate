DROP ALL OBJECTS;

CREATE TYPE IF NOT EXISTS "mpa_name" AS ENUM (
    'G',
    'PG',
    'PG-13',
    'R',
    'NC-17'
    );

CREATE TABLE IF NOT EXISTS "mpa_rating"
(
    "mpa_id" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "name"   "mpa_name" UNIQUE
);

CREATE TYPE IF NOT EXISTS "genre_name" AS ENUM (
    'Комедия',
    'Драма',
    'Мультфильм',
    'Триллер',
    'Документальный',
    'Боевик'
    );

CREATE TABLE IF NOT EXISTS "genres"
(
    "genre_id" INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "name"     "genre_name" UNIQUE
);

CREATE TABLE IF NOT EXISTS "films"
(
    "film_id"      INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "name"         VARCHAR      NOT NULL,
    "description"  VARCHAR(200) NOT NULL,
    "release_date" DATE         NOT NULL,
    "duration"     INT          NOT NULL,
    "rate"         INT          NOT NULL,
    "mpa_id"       INT REFERENCES "mpa_rating" ("mpa_id"),
    CONSTRAINT IF NOT EXISTS "not_blank" CHECK (LENGTH("name") > 0),
    CONSTRAINT IF NOT EXISTS "earliest_date" CHECK ("release_date" >= CAST('1895-12-28' AS DATE)),
    CONSTRAINT IF NOT EXISTS "positive" CHECK ("duration" > 0)
);

CREATE TABLE IF NOT EXISTS "film_genres"
(
    "film_id"  INT REFERENCES "films" ("film_id"),
    "genre_id" INT REFERENCES "genres" ("genre_id"),
    PRIMARY KEY ("film_id", "genre_id")
);

CREATE TABLE IF NOT EXISTS "users"
(
    "user_id"  INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "email"    VARCHAR NOT NULL,
    "login"    VARCHAR NOT NULL,
    "name"     VARCHAR NOT NULL,
    "birthday" DATE    NOT NULL,
    CONSTRAINT IF NOT EXISTS "not_blank" CHECK (LENGTH("email") > 0 AND LENGTH("login") > 0 AND LENGTH("name") > 0),
    CONSTRAINT IF NOT EXISTS "earliest_date" CHECK ("birthday" > CURRENT_DATE)
);

CREATE TABLE IF NOT EXISTS "friendships_sent"
(
    "from_user_id" INT REFERENCES "users" ("user_id"),
    "to_user_id"   INT REFERENCES "users" ("user_id"),
    PRIMARY KEY ("from_user_id", "to_user_id")
);

CREATE TABLE IF NOT EXISTS "likes"
(
    "film_id"      INT REFERENCES "films" ("film_id"),
    "from_user_id" INT REFERENCES "users" ("user_id"),
    PRIMARY KEY ("film_id", "from_user_id")
);

MERGE INTO "mpa_rating" ("mpa_id", "name")
    VALUES (1, 'G'),
           (2, 'PG'),
           (3, 'PG-13'),
           (4, 'R'),
           (5, 'NC-17');

MERGE INTO "genres" ("genre_id", "name")
    VALUES (1, 'Комедия'),
           (2, 'Драма'),
           (3, 'Мультфильм'),
           (4, 'Триллер'),
           (5, 'Документальный'),
           (6, 'Боевик');