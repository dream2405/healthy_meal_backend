package kr.ac.dankook.ace.healthy_meal_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "gender")
    private Character gender;

}