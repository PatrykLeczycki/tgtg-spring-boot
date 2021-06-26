insert into role (name)
values ('ROLE_ADMIN');

insert into role (name)
values ('ROLE_USER');

INSERT INTO user (id, created_at, email, password, username, enabled, registration_token, pass_recovery_token) VALUES
(1, NOW(), 'patryk.leczycki1@gmail.com', '$2a$10$XGOnLLwK7gvuoEq5AK5vOe4h.DavvDVawALgj3scEVev6SYUqERt6', null, 0, null, null),
(2, NOW(), '1@1', '$2a$10$ppT.OJXeOr8xcCR4/yF.yeLwbCWaIxyHGfHFXv2ZkQYwD8kgVf/X.', null, 1, null, null),
(3, NOW(), 'test@test', '$2a$10$gbqLj6ZsQYh./3m0at.rEeL/bMp9RpUvlmANmcsqe3EMQRlV613YC', null, 1, null, null),
(4, NOW(), 'admin@admin', '$2a$10$6UefAy/6SYU.PU66ZMPApuQrGJtTWEP9LJS1x7Pk.gKH0vvqMkBKm', null, 1, null, null);

INSERT INTO user_roles (user_id, role_id) VALUES
(1,1), (1,2),
(2,1), (2,2),
(3,1),
(4,1), (4,2);

INSERT INTO address (id, building_no, city, latitude, longitude, street)
values
(1, '130', 'Warszawa', 52.2347399, 21.0094018, 'Marszałkowska'),
(2,'45A','Warszawa',52.2440589,20.9911297,'al. Jana Pawła II'),
(3,'4','Siedlce',52.16798,22.279896,'Esperanto'),
(4,'5','Toruń',53.0128804,18.6088602,'Wysoka'),
(5,'39','Toruń',53.0175271,18.5798499,'Henryka Sienkiewicza'),
(6,'16','Gdańsk',54.41103349999999,18.6301297,'Brzeźnieńska');

INSERT INTO location (id, created_at, modified_at, name, rating, address_id)
values
(1, '2020-11-19 08:21:38.864000', null, 'Manekin', 5, 1),
(2,'2020-11-21 08:21:38.864000', null, 'Wypieki Gruzińskie - Kraina Wypieków',0,2),
(3,'2020-11-21 08:22:42.066000', null, 'Al Amir',0,3),
(4,'2020-11-21 08:23:38.648000', null, 'Manekin',0,4),
(5,'2020-11-21 08:24:33.977000', null, 'Widelec',0,5),
(6,'2020-11-21 08:26:16.673000', null, 'Mnie to Rybka',0,6);

INSERT INTO review (id, comment, created_at, modified_at, discount_price, pickup_time, rating, standard_price, location_id)
values (1, 'Bardzo dobra paczka', NOW(), null, 10, '2018-06-12 17:30:00', 4, 30, 1);

INSERT INTO user_review (user_id, review_id) VALUES (2, 1);