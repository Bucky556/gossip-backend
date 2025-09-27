insert into profile(id, name, username, password, status, visible, created_date)
values ('b3e37f6a-1a73-4c4d-b3b2-ec61a7f1e0f2',
        'Adminjon',
        'adminjon@gmail.com',
        '$2a$10$BXgnKH4M3F13iHRn3pFlnu6VZSupNk8FJeljtEjS0UtV9LH4FhCZ6',
        'ACTIVE',
        true,
        now());


insert into profile_role(profile_id, p_role, created_date)
values ((select p.id from profile p where p.username = 'adminjon@gmail.com'), 'ROLE_USER', now()),
       ((select p.id from profile p where p.username = 'adminjon@gmail.com'), 'ROLE_ADMIN', now());