ALTER TABLE soknad ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE soknad ADD COLUMN soknad_pdf BYTEA;
