ALTER TABLE soknad RENAME TO skjema;
ALTER TABLE skjema RENAME COLUMN soknad_json TO skjema_json;
ALTER TABLE skjema RENAME COLUMN soknad_pdf TO skjema_pdf;
ALTER TABLE skjema RENAME COLUMN soknad_frontend_git_hash TO frontend_git_hash;

ALTER TABLE vedlegg RENAME COLUMN soknad_id TO skjema_id;