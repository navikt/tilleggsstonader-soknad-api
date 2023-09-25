CREATE TABLE soknad
(
    id             UUID PRIMARY KEY,
    opprettet_tid  TIMESTAMP(3) NOT NULL,
    type           VARCHAR      NOT NULL,
    person_ident   VARCHAR      NOT NULL,
    soknad_json    JSON         NOT NULL,
    journalpost_id VARCHAR,
    soknad_pdf     VARCHAR
)