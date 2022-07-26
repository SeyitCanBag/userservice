package com.scb.userservice.repo;

import com.scb.userservice.domain.Calisanlar;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Repository
public class CalisanlarRepoImpl implements CalisanlarRepo{
    EntityManager entityManager;

    @Override
    @Transactional
    public void calisanEkle(Calisanlar calisan) {
        Session session = entityManager.unwrap(Session.class);
        session.save(calisan);
    }
}
