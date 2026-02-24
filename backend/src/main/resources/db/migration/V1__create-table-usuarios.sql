create table usuarios(
    id bigserial PRIMARY KEY,
    nombre varchar(100) not null,
    apellido varchar(100) not null,
    email varchar(100) not null unique,
    password varchar(255) not null,
    rol varchar(50) not null,
    activo boolean not null default true,

    created_at timestamp default current_timestamp,
    updated_at timestamp
);