CREATE USER "sem-13a" WITH PASSWORD 'passwd';

CREATE DATABASE "hiring-procedure";
CREATE DATABASE "hour-management";
CREATE DATABASE authentication;
CREATE DATABASE courses;
CREATE DATABASE users;

GRANT ALL PRIVILEGES ON DATABASE "hiring-procedure", "hour-management", authentication, courses, users TO "sem-13a";
