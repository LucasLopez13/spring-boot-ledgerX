create table billeteras(
    id bigserial PRIMARY KEY,
    saldo decimal(19,4) not null default 0.0000,
    usuario_id bigint not null unique,
    version bigint not null default 0,

    created_at timestamp default current_timestamp,
    updated_at timestamp,

    constraint fk_billetera_usuario foreign key (usuario_id) references usuarios(id) on delete restrict
);