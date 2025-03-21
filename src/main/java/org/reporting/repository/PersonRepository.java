package org.reporting.repository;

import org.reporting.model.Person;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PersonRepository {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public PersonRepository(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public List<Person> findAll() {
        List<Person> persons = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM persons")) {
            
            while (rs.next()) {
                persons.add(mapResultSetToPerson(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching persons", e);
        }
        return persons;
    }

    public Optional<Person> findById(Long id) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM persons WHERE id = ?")) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPerson(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching person with id: " + id, e);
        }
        return Optional.empty();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private Person mapResultSetToPerson(ResultSet rs) throws SQLException {
        return new Person(
            rs.getLong("id"),
            rs.getString("person_number"),
            rs.getString("person_source"),
            rs.getString("first_name"),
            rs.getString("last_name")
        );
    }
} 