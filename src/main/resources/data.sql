insert into role (name)
values ('ROLE_ADMIN');

insert into role (name)
values ('ROLE_USER');

INSERT INTO user (id, created_at, email, password, username) VALUES
(1, NOW(), 'patryk.leczycki1@gmail.com', '', null),
(2, NOW(), '1@1', '', null);

INSERT INTO user_roles (user_id, role_id) VALUES
(1,1), (1,2);

INSERT INTO user_roles (user_id, role_id) VALUES
(2,1), (2,2);

INSERT INTO address (id, building_no, city, street) values (1, '5/16', 'Warszawa', 'Bellottiego');

INSERT INTO location (id, created_at, modified_at, name, rating, address_id) values (1, NOW(), null, 'Dom', 4, 1);

INSERT INTO review (id, comment, created_at, modified_at, discount_price, pickup_time, rating, standard_price, location_id)
values (1, 'Spoko', NOW(), null, 10, '2018-06-12 17:30:00', 4, 30, 1);

INSERT INTO photo (id, created_at, file_name, modified_at, url, review_id)
values (1, NOW(), '586x450.jpg', null, 'https://media-cdn.tripadvisor.com/media/photo-s/0d/63/a8/82/domino-s-pizza.jpg', 1),
       (2, NOW(), '1900x1427-1.jpg', null, 'https://media-cdn.tripadvisor.com/media/photo-s/15/0d/14/43/muy-buena.jpg', 1)