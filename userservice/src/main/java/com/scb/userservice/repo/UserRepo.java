package com.scb.userservice.repo;

import com.scb.userservice.domain.Calisanlar;
import com.scb.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);      //Kullanıcı adına göre kullanıcıyı bulacak(yani buraya kullanıcı adı girilecek ve o da kullanıcının ismini verecek)

    //void calisanEkle(Calisanlar calisan);     //Çalışan ekleme -> UserService
}
