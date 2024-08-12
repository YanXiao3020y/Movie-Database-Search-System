USE moviedb;

CREATE TABLE IF NOT EXISTS employees (
	email varchar(50) primary key,
	password varchar(20) not null,
	fullname varchar(100)

);


