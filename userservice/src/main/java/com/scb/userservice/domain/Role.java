package com.scb.userservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.AUTO;


@Entity
@Data
@NoArgsConstructor   //Parametresiz constructor üretir
@AllArgsConstructor  //tüm parametreleri kullanarak constructor üretir
public class Role {
    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    private String name;



}
