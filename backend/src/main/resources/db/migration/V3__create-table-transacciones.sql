create table transacciones(
    id bigserial PRIMARY KEY,
    billetera_origen_id bigint,
    billetera_destino_id bigint not null,
    cantidad decimal(19,4) not null,
    tipo_transaccion varchar(50) not null,
    estado_transaccion varchar(50) not null,
    detalle_transaccion varchar(255),

    created_at timestamp default current_timestamp,

    constraint fk_transaccion_origen foreign key (billetera_origen_id) references billeteras(id),
    constraint fk_transaccion_destino foreign key (billetera_destino_id) references billeteras(id)
);