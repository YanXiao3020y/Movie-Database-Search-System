CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;







CREATE TABLE IF NOT EXISTS movies(
                   id varchar(10) primary key,
                   title varchar(100) not null,
                   year integer not null,
                   director varchar(100) not null,
                   FULLTEXT (title)
               );

CREATE TABLE IF NOT EXISTS stars(
                   id varchar(10) primary key,
                   name varchar(100) not null,
                   birthYear integer
               );


CREATE TABLE IF NOT EXISTS stars_in_movies(
                   starId varchar(10),
                   movieId varchar(10),
                   PRIMARY KEY(starId, movieId),
                   FOREIGN KEY(starId) REFERENCES stars(id) ON UPDATE CASCADE        ON DELETE CASCADE,
                   FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE        ON DELETE CASCADE
               );


CREATE TABLE IF NOT EXISTS genres(
                   id integer primary key AUTO_INCREMENT,
                   name varchar(32) not null
               );


CREATE TABLE IF NOT EXISTS genres_in_movies(
                   genreId integer,
                   movieId varchar(10),
                   PRIMARY KEY(genreId, movieId), 
                   FOREIGN KEY(genreId) REFERENCES genres(id) ON UPDATE CASCADE        ON DELETE CASCADE,
                   FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE        ON DELETE CASCADE
               );


CREATE TABLE IF NOT EXISTS creditcards(
                   id varchar(20) primary key,
                   firstName varchar(50) not null,
                   lastName varchar(50) not null,
                   expiration date not null
               );


CREATE TABLE IF NOT EXISTS customers(
                   id integer primary key AUTO_INCREMENT,
                   firstName varchar(50) not null, 
                   lastName varchar(50) not null, 
                   ccId varchar(20), 
                   address varchar(200) not null,
                   email varchar(50) not null,
                   password varchar(20) not null,
                   FOREIGN KEY(ccId) REFERENCES creditcards(id) ON UPDATE CASCADE        ON DELETE CASCADE
               );

CREATE TABLE IF NOT EXISTS sales(
                   id integer primary key AUTO_INCREMENT,
                   customerId integer not null,
                   movieId varchar(10) not null, 
                   saleDate date not null,
                   FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE CASCADE        ON DELETE CASCADE,
                   FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE        ON DELETE CASCADE
               );


CREATE TABLE IF NOT EXISTS ratings(
                   movieId varchar(10) primary key, #we add primary key
                   rating float not null,
                   numVotes integer not null,
                   FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE        ON DELETE CASCADE
               );