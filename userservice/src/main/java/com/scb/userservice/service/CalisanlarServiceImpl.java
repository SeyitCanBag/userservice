package com.scb.userservice.service;

import com.scb.userservice.domain.Calisanlar;
import com.scb.userservice.repo.CalisanlarRepo;
import com.scb.userservice.repo.CalisanlarRepoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalisanlarServiceImpl implements CalisanlarService{

    private CalisanlarRepo calisanlarRepo;

    @Autowired
    public CalisanlarServiceImpl(CalisanlarRepo calisanlarRepo){
        this.calisanlarRepo = calisanlarRepo;
    }

    @Override
    @Transactional
    public void calisanEkle(Calisanlar calisan) {
        this.calisanlarRepo.calisanEkle(calisan);
    }
}
