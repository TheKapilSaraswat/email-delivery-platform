-- ============================================================================
-- Email Delivery Platform - Reference seed data (PostgreSQL)
-- ============================================================================
-- This file contains NON-SENSITIVE reference data only.
-- The admin user is NOT seeded here: it is created at runtime by the
-- DataSeeder component using ADMIN_EMAIL / ADMIN_PASSWORD env vars, which
-- guarantees the password is hashed correctly and never stored in source.
-- ============================================================================

INSERT INTO templates (id, name, subject, body, user_id, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000001',
       'Welcome Email',
       'Welcome to {{name}}',
       'Hi {{name}},\n\nThanks for joining. We are glad to have you.\n\nBest regards,\nThe Team',
       NULL,
       NOW(),
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM templates WHERE id = '00000000-0000-0000-0000-000000000001'
);

INSERT INTO templates (id, name, subject, body, user_id, created_at, updated_at)
SELECT '00000000-0000-0000-0000-000000000002',
       'Weekly Newsletter',
       'Your weekly update',
       'Hello {{name}},\n\nHere is what happened this week...\n\nCheers,\nThe Team',
       NULL,
       NOW(),
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM templates WHERE id = '00000000-0000-0000-0000-000000000002'
);
