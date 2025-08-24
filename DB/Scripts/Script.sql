-- Creazione del database
DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca;
USE biblioteca;
SET GLOBAL event_scheduler=ON;

-- Creazione tabella utenti
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

-- Creazione tabella libri
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

-- Creazione tabella prestiti
CREATE TABLE IF NOT EXISTS prestiti (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    libro_id BIGINT NULL,
    utente VARCHAR(100) NOT NULL,
    data_prestito DATE NOT NULL,
    data_restituzione DATE,
    FOREIGN KEY (libro_id) REFERENCES biblioteca.libri(id) ON DELETE SET NULL ON UPDATE CASCADE,
    utente_id BIGINT NULL,
    FOREIGN KEY (utente_id) REFERENCES biblioteca.utenti(id) ON DELETE SET NULL ON UPDATE CASCADE,
    libro_isbn_snapshot VARCHAR(30),
	libro_titolo_snapshot TEXT NULL,
	libro_autore_snapshot VARCHAR(255),
	utente_nome_snapshot VARCHAR(100),
	utente_cognome_snapshot VARCHAR(100),
	utente_snapshot TEXT NULL,
	CONSTRAINT fk_prestiti_libro FOREIGN KEY (libro_id) REFERENCES libri(id) ON DELETE SET NULL ON UPDATE CASCADE,
	CONSTRAINT fk_prestiti_utente FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL ON UPDATE CASCADE
);