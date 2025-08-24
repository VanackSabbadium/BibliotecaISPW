-- Creazione del database
DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca;
USE biblioteca;
SET GLOBAL event_scheduler=ON;

-- Creazione tabella libri
CREATE TABLE IF NOT EXISTS libri (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    titolo VARCHAR(200) NOT NULL,
    autore VARCHAR(100) NOT NULL,
    data_pubblicazione DATE NOT NULL,
    casa_editrice VARCHAR(100) NOT NULL
);

-- Creazione tabella prestiti
CREATE TABLE IF NOT EXISTS prestiti (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    libro_id BIGINT NOT NULL,
    utente VARCHAR(100) NOT NULL,
    data_prestito DATE NOT NULL,
    data_restituzione DATE,
    FOREIGN KEY (libro_id) REFERENCES libri(id)
);