-- RF01 / modelo "usuario": e-mail unico sem diferenciar maiusculas e CPF com 11 digitos, sem mascara.
-- O backend passa a normalizar os dados antes de gravar; aqui ajustamos os registros ja existentes.

UPDATE users
   SET email = LOWER(TRIM(email)),
       document = REGEXP_REPLACE(document, '[^0-9]', '');

-- ENABLE NOVALIDATE: vale para todos os registros novos/alterados, sem impedir a migracao
-- caso algum banco local tenha dados de teste antigos fora do padrao.
ALTER TABLE users ADD CONSTRAINT ck_users_email_lowercase
    CHECK (email = LOWER(email)) ENABLE NOVALIDATE;

ALTER TABLE users ADD CONSTRAINT ck_users_document_digits
    CHECK (REGEXP_LIKE(document, '^[0-9]{11}$')) ENABLE NOVALIDATE;
