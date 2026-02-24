CREATE TABLE idempotencia_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotencia_key VARCHAR(255) NOT NULL,
    usuario_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_status INT,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotencia_key UNIQUE (idempotencia_key),
    CONSTRAINT fk_idempotencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
