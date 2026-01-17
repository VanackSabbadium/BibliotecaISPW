DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE biblioteca;

CREATE TABLE IF NOT EXISTS utenti (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tessera INT NOT NULL UNIQUE,
  nome VARCHAR(100) NOT NULL,
  cognome VARCHAR(100) NOT NULL,
  email VARCHAR(150),
  telefono VARCHAR(50),
  data_attivazione DATE NULL,
  data_scadenza DATE NULL
);

CREATE TABLE IF NOT EXISTS libri (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  isbn VARCHAR(20) UNIQUE NOT NULL,
  titolo VARCHAR(200) NOT NULL,
  autore VARCHAR(100) NOT NULL,
  data_pubblicazione DATE NOT NULL,
  casa_editrice VARCHAR(100) NOT NULL,
  attivo TINYINT(1) NOT NULL DEFAULT 1,
  copie INT NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS prestiti (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  libro_id BIGINT NULL,
  utente_id BIGINT NULL,
  utente_descrizione VARCHAR(255) NULL,
  data_prestito DATE NOT NULL,
  data_restituzione DATE,
  libro_isbn_snapshot VARCHAR(30),
  libro_titolo_snapshot TEXT NULL,
  libro_autore_snapshot VARCHAR(255),
  utente_nome_snapshot VARCHAR(100),
  utente_cognome_snapshot VARCHAR(100),
  utente_snapshot TEXT NULL,
  CONSTRAINT fk_prestiti_libro FOREIGN KEY (libro_id) REFERENCES libri(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_prestiti_utente FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS prenotazioni (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  libro_id BIGINT NULL,
  utente_id BIGINT NULL,
  data_prenotazione DATE NOT NULL,
  data_evasione DATE NULL,
  libro_titolo_snapshot TEXT NULL,
  utente_snapshot TEXT NULL,
  CONSTRAINT fk_prenotazioni_libri FOREIGN KEY (libro_id) REFERENCES libri(id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_prenotazioni_utenti FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS credenziali (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  utente_id BIGINT NOT NULL UNIQUE,
  username VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  password_plain VARCHAR(255) NULL,
  role ENUM('ADMIN','BIBLIOTECARIO','UTENTE') NOT NULL DEFAULT 'UTENTE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cred_utente FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE CASCADE
);

DROP USER IF EXISTS 'Admin'@'%';
DROP USER IF EXISTS 'Admin'@'localhost';
DROP USER IF EXISTS 'Bibliotecario'@'localhost';
DROP USER IF EXISTS 'Utente'@'localhost';

CREATE USER IF NOT EXISTS 'Admin'@'localhost' IDENTIFIED BY 'admin';
ALTER USER 'Admin'@'localhost' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'Admin'@'localhost';

CREATE USER IF NOT EXISTS 'Admin'@'%' IDENTIFIED BY 'admin';
ALTER USER 'Admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'Admin'@'%';

CREATE USER 'Bibliotecario'@'localhost' IDENTIFIED BY 'bibliotecario';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'Bibliotecario'@'localhost';

INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza)
VALUES (0, 'Admin', 'Admin', 'admin@biblioteca.local', '0000000000', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 YEAR));

SET @admin_utente_id = LAST_INSERT_ID();

INSERT INTO credenziali (utente_id, username, password_hash, role)
VALUES (@admin_utente_id, 'admin', SHA2('admin', 256), 'ADMIN');

INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza)
VALUES (1, 'Bibliotecario', 'Bibliotecario', 'bibliotecario@biblioteca.local', '0000000000', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 YEAR));

SET @bib_utente_id = LAST_INSERT_ID();

INSERT INTO credenziali (utente_id, username, password_hash, role)
VALUES (@bib_utente_id, 'bibliotecario', SHA2('bibliotecario', 256), 'BIBLIOTECARIO');

INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza)
VALUES 
(1001, 'Giulia',   'Ferrari',   'giulia.ferrari@example.com',   '3331111111', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR)),
(1002, 'Lorenzo',  'Conti',     'lorenzo.conti@example.com',    '3332222222', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR)),
(1003, 'Sara',     'Marini',    'sara.marini@example.com',      '3333333333', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR)),
(1004, 'Davide',   'Rossi',     'davide.rossi@example.com',     '3334444444', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR)),
(1005, 'Elena',    'Galli',     'elena.galli@example.com',      '3335555555', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR));

INSERT INTO libri (isbn, titolo, autore, data_pubblicazione, casa_editrice, attivo, copie)
VALUES
('9788800000001', 'Programmazione Java Base',            'Mario Rossi',       '2021-03-15', 'TechPress',   1, 3),
('9788800000002', 'Strutture Dati e Algoritmi',          'Luca Bianchi',      '2020-10-01', 'UniBooks',    1, 2),
('9788800000003', 'Database Relazionali',                'Anna Verdi',        '2019-05-20', 'DBEditrice',  1, 4),
('9788800000004', 'Reti di Calcolatori',                 'Paolo Neri',        '2022-01-12', 'NetHouse',    1, 1),
('9788800000005', 'Intelligenza Artificiale: Introduzione','Chiara Gallo',   '2023-07-05', 'AI Labs',     1, 2);

DELIMITER $$
DROP FUNCTION IF EXISTS biblioteca.set_app_tessera $$
CREATE FUNCTION biblioteca.set_app_tessera(p_tessera INT)
RETURNS INT
DETERMINISTIC
NO SQL
BEGIN
  SET @app_tessera := p_tessera;
  RETURN @app_tessera;
END $$
DROP FUNCTION IF EXISTS biblioteca.get_app_tessera $$
CREATE FUNCTION biblioteca.get_app_tessera()
RETURNS INT
DETERMINISTIC
NO SQL
BEGIN
  RETURN @app_tessera;
END $$
DELIMITER ;

DROP VIEW IF EXISTS biblioteca.utenti_self;
CREATE VIEW biblioteca.utenti_self AS
SELECT
  id,
  tessera,
  nome,
  cognome,
  email,
  telefono,
  data_attivazione,
  data_scadenza
FROM biblioteca.utenti
WHERE tessera = biblioteca.get_app_tessera();

FLUSH PRIVILEGES;