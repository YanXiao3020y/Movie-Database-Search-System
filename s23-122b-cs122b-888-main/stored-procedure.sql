use moviedb;

-- tables for tracking id for adding --
CREATE TABLE IF NOT EXISTS starID(
	sid integer primary key
);
INSERT INTO starID VALUES(9423080);
CREATE TABLE IF NOT EXISTS movieID(
	id integer primary key
);
INSERT INTO movieID VALUES(0499469);


-- Used to update the star id for addstar --
DELIMITER //
CREATE PROCEDURE updateStarID(IN stID integer)
BEGIN
	UPDATE starID 
    	SET sid = stID
	WHERE sid = stID-1;
END //
DELIMITER ;


-- add_star --
DELIMITER //

CREATE PROCEDURE add_star (IN starName varchar(100), IN birthYear integer, OUT star_id varchar(10))
BEGIN

    	INSERT INTO stars VALUES(CONCAT("nm", ((SELECT sid FROM starID) +1)), starName, birthYear);
    	SET star_id = CONCAT("nm", ((SELECT sid FROM starID) +1));
    	CALL updateStarID((SELECT sid FROM starID) +1);
    
END//

DELIMITER ;


-- Get id for add_movie --
DELIMITER //
CREATE PROCEDURE updateMovieID(IN mID integer)
BEGIN
	UPDATE movieID 
    	SET id = mID
	WHERE id = mID-1;
END //
DELIMITER ;


-- add_movie --
DELIMITER //

CREATE PROCEDURE add_movie (IN mtitle varchar(100), IN myear integer, IN mdirector varchar(100),
                   IN starName varchar(100), IN genre varchar(32), OUT message varchar(20), OUT moID varchar(10), OUT star_id varchar(10), OUT genre_id varchar(10))
BEGIN
    DECLARE temp_id INTEGER;

    IF EXISTS (SELECT 1 FROM movies WHERE title = mtitle AND year = myear AND director = mdirector) THEN
        SET message = "fail";
    ELSE
        INSERT INTO movies VALUES(CONCAT("tt", (SELECT id FROM movieID) + 1), mtitle, myear, mdirector);
        SET temp_id = (SELECT id FROM movieID) + 1;
        CALL updateMovieID(temp_id);
        SET message = "success";
        SET moID = CONCAT("tt", temp_id);

        IF EXISTS (SELECT 1 FROM stars WHERE name = starName) THEN
            SET star_id = (SELECT MAX(id) FROM stars WHERE name = starName);
        ELSE
            CALL add_star(starName, NULL, @temp);
            SET star_id = @temp;
        END IF;

        INSERT INTO stars_in_movies VALUES(star_id, CONCAT("tt", temp_id));

        IF NOT EXISTS (SELECT 1 FROM genres WHERE name = genre) THEN
            INSERT INTO genres(name) VALUES(genre);
        END IF;

        SET genre_id = (SELECT id FROM genres WHERE name = genre);
        INSERT INTO genres_in_movies VALUES(genre_id, CONCAT("tt", temp_id));
    END IF;
END //

DELIMITER ;




-- add_movie, add_star, add_star_in_movie for xml --



-- xml add_movie -- 
DELIMITER //

CREATE PROCEDURE add_xml_movie (IN mtitle varchar(100), IN myear integer, IN mdirector varchar(100), IN genre varchar(32))
BEGIN
	DECLARE temp_id INTEGER;
	SET temp_id = (SELECT id FROM movieID) + 1;
    	INSERT INTO movies VALUES(CONCAT("tt", temp_id), mtitle, myear, mdirector);
    	CALL updateMovieID(temp_id);

	IF NOT EXISTS (SELECT 1 FROM genres WHERE name = genre) THEN
            INSERT INTO genres(name) VALUES(genre);
      END IF;

      
      INSERT INTO genres_in_movies VALUES((SELECT id FROM genres WHERE name = genre), CONCAT("tt", temp_id));

END //

DELIMITER ;


-- xml add_star_in_movie --
DELIMITER //
CREATE PROCEDURE add_star_in_movie(IN star_id varchar(10), IN movie_id varchar(10))
BEGIN
	INSERT ignore INTO stars_in_movies VALUES(star_id, movie_id);
END //

DELIMITER ;


-- xml add star --

DELIMITER //

CREATE PROCEDURE add_xml_star (IN starName varchar(100), IN birthYear integer)
BEGIN

    	INSERT INTO stars VALUES(CONCAT("nm", ((SELECT sid FROM starID) +1)), starName, birthYear);
    	CALL updateStarID((SELECT sid FROM starID) +1);
    
END//

DELIMITER ;