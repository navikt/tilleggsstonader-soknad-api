create table vedlegg
(
    id            UUID PRIMARY KEY,
    soknad_id     UUID         NOT NULL REFERENCES soknad (id),
    type          VARCHAR      NOT NULL,
    navn          VARCHAR      NOT NULL,
    innhold       BYTEA        NOT NULL,
    opprettet_tid TIMESTAMP(3) NOT NULL
);

CREATE INDEX ON vedlegg (soknad_id)
