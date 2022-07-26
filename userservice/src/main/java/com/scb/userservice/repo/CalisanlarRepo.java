package com.scb.userservice.repo;

import com.scb.userservice.domain.Calisanlar;
import com.scb.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalisanlarRepo{

    void calisanEkle(Calisanlar calisan);
}
