ALTER TABLE profile_role
    DROP CONSTRAINT IF EXISTS profile_role_p_role_check;

ALTER TABLE profile_role
    ADD CONSTRAINT profile_role_p_role_check
        CHECK (p_role IN ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MODERATOR'));
