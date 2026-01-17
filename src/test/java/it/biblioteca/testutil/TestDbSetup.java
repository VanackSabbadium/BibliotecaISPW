package it.biblioteca.testutil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class TestDbSetup {

    private TestDbSetup() {}

    public static void resetSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS=0");

            st.execute("DROP TABLE IF EXISTS prestiti");
            st.execute("DROP TABLE IF EXISTS credenziali");
            st.execute("DROP TABLE IF EXISTS libri");
            st.execute("DROP TABLE IF EXISTS utenti");

            st.execute("""
                CREATE TABLE utenti (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  tessera INT NOT NULL UNIQUE,
                  nome VARCHAR(100) NOT NULL,
                  cognome VARCHAR(100) NOT NULL,
                  email VARCHAR(150),
                  telefono VARCHAR(50),
                  data_attivazione DATE NULL,
                  data_scadenza DATE NULL
                )
            """);

            st.execute("""
                CREATE TABLE libri (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  isbn VARCHAR(20) UNIQUE NOT NULL,
                  titolo VARCHAR(200) NOT NULL,
                  autore VARCHAR(100) NOT NULL,
                  data_pubblicazione DATE NOT NULL,
                  casa_editrice VARCHAR(100) NOT NULL,
                  attivo TINYINT(1) NOT NULL DEFAULT 1,
                  copie INT NOT NULL DEFAULT 1
                )
            """);

            st.execute("""
                CREATE TABLE prestiti (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  libro_id BIGINT NULL,
                  utente_id BIGINT NULL,
                  utente_descrizione VARCHAR(255) NULL,
                  data_prestito DATE NOT NULL,
                  data_restituzione DATE NULL,
                  libro_isbn_snapshot VARCHAR(30),
                  libro_titolo_snapshot TEXT NULL,
                  libro_autore_snapshot VARCHAR(255),
                  utente_nome_snapshot VARCHAR(100),
                  utente_cognome_snapshot VARCHAR(100),
                  utente_snapshot TEXT NULL,
                  CONSTRAINT fk_prestiti_libro FOREIGN KEY (libro_id) REFERENCES libri(id) ON DELETE SET NULL ON UPDATE CASCADE,
                  CONSTRAINT fk_prestiti_utente FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE SET NULL ON UPDATE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE credenziali (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  utente_id BIGINT NOT NULL UNIQUE,
                  username VARCHAR(100) UNIQUE NOT NULL,
                  password_hash VARCHAR(255) NOT NULL,
                  role ENUM('ADMIN','BIBLIOTECARIO','UTENTE') NOT NULL DEFAULT 'UTENTE',
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_cred_utente FOREIGN KEY (utente_id) REFERENCES utenti(id) ON DELETE CASCADE
                )
            """);

            st.execute("""
                INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza)
                VALUES (0,'Admin','Admin','admin@biblioteca.local','000', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 YEAR))
            """);
            st.execute("""
                INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza)
                VALUES (1,'Bibliotecario','Bib','bib@biblioteca.local','111', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 YEAR))
            """);
            st.execute("""
                INSERT INTO utenti (tessera, nome, cognome, email, telefono, data_attivazione, data_scadenza)
                VALUES (100,'Mario','Rossi','mario@biblioteca.local','222', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 YEAR))
            """);

            st.execute("INSERT INTO credenziali (utente_id, username, password_hash, role) VALUES (1,'admin',SHA2('admin',256),'ADMIN')");
            st.execute("INSERT INTO credenziali (utente_id, username, password_hash, role) VALUES (2,'bibliotecario',SHA2('bibliotecario',256),'BIBLIOTECARIO')");
            st.execute("INSERT INTO credenziali (utente_id, username, password_hash, role) VALUES (3,'mario',SHA2('mario',256),'UTENTE')");

            st.execute("""
                INSERT INTO libri (isbn,titolo,autore,data_pubblicazione,casa_editrice,attivo,copie)
                VALUES ('978000000001','Libro Test','Autore Test', CURDATE(),'Editore Test',1,3)
            """);

            st.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }
}
