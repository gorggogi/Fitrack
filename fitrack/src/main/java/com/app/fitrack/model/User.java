package com.app.fitrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;

@Entity
@Table (name="users")
public class User {
	
	@Id 
	@GeneratedValue (strategy = GenerationType.IDENTITY)
	
	@Column (nullable = false, unique = true)
	private Integer id;
	
	@Column (nullable = false, unique = true, length = 45)
	private String email;
	
	@Column (nullable =false, length = 255)
	private String password;
	
	@Column (nullable=false, name="firstname", length = 45)
	private String firstName;
	
    @Column (nullable=false, name="lastname", length = 45)
    private String lastName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

	
	
}
