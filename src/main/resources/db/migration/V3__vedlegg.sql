create table vedlegg
(
    id        UUID PRIMARY KEY,
    soknad_id UUID    NOT NULL REFERENCES soknad (id),
    navn      VARCHAR NOT NULL,
    tittel    VARCHAR NOT NULL,
    innhold   BYTEA   NOT NULL
);

CREATE INDEX ON vedlegg (soknad_id)
