package org.reporting.model;

import org.reporting.annotation.Sortable;

public class ActiveUser {
    @Sortable
    private Long id;
    
    @Sortable("personnumber")
    private String personNumber;
    
    @Sortable("personsource")
    private String personSource;
    
    @Sortable("firstname")
    private String firstName;
    
    @Sortable("lastname")
    private String lastName;
    
    @Sortable
    private String status;

    // Constructors
    public ActiveUser() {}

    public ActiveUser(Long id, String personNumber, String personSource, 
                     String firstName, String lastName, String status) {
        this.id = id;
        this.personNumber = personNumber;
        this.personSource = personSource;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }

    public String getPersonSource() {
        return personSource;
    }

    public void setPersonSource(String personSource) {
        this.personSource = personSource;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 