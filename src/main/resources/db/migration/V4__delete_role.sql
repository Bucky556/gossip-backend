-- 1. Mos kelmaydigan rollarni tozalash
DELETE FROM profile_role
WHERE p_role NOT IN ('ROLE_ADMIN', 'ROLE_USER');

-- 2. Agar constraint mavjud bo'lsa, o'chirib qayta qo'shish
ALTER TABLE profile_role
    DROP CONSTRAINT IF EXISTS profile_role_p_role_check;

ALTER TABLE profile_role
    ADD CONSTRAINT profile_role_p_role_check
        CHECK (p_role IN ('ROLE_ADMIN', 'ROLE_USER'));
