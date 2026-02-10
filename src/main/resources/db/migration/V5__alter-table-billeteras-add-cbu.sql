ALTER TABLE billeteras ADD cbu VARCHAR(255);
ALTER TABLE billeteras ADD CONSTRAINT uk_billeteras_cbu UNIQUE (cbu);