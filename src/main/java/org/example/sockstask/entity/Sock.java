package org.example.sockstask.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sock")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Sock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "cotton_percentage", nullable = false)
    private float cottonPercentage;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public void income(int quantity) {
        this.quantity += quantity;
    }

    public void outcome(int quantity) {
        this.quantity -= quantity;
    }

}
