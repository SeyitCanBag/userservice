package com.scb.userservice.service;

import com.scb.userservice.domain.Role;
import com.scb.userservice.domain.User;
import com.scb.userservice.repo.RoleRepo;
import com.scb.userservice.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //girilen username e göre kullanıcı deposunu kullanarak kullanıcımızı yükleyebiliriz.(Springe kullanıcıyı nasıl bulacağını anlattık.)
        User user = userRepo.findByUsername(username);
        if(user == null){
            log.error("Kullanıcı veritabanında bulunamadı");
            throw new UsernameNotFoundException("Kullanıcı veritabanında bulunamadı");
        }else {
            log.info("Kullanıcı veritabanında bulundu : {}",username);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {    //kullanıcının tüm rolleri üzerinde dönüyoruz ve sonra her biri için rol adını ileterek basit bir yetkilendirme yetkisi oluşturacağız.
            authorities.add(new SimpleGrantedAuthority(role.getName())); //ve sonra bu authorities listesini düzenleyeceğiz.
        });
        //iade edilmeyi bekleyen spring güvenlik kullanıcısının bilgilerini alıp(username,password,authorities) ardından şifre karşılaştırması yapıp tüm yetkileri ve her şeyi kontrol etmesi için geri gönderdik
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities); //authorities : spring security'den kullanıcıya aktaracağımız kuralların(rollerin) listesi
    }

    @Override
    public User saveUser(User user) {
        log.info("yeni kullanıcı {} veritabanına kaydediliyor", user.getName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));    //Kullanıcıyı kaydetmeden önce parolasını şifreledik.
        return userRepo.save(user);    //kullanıcıyı veritabanına kaydettik.
    }

    @Override
    public Role saveRole(Role role) {
        log.info("yeni rol {} veritabanına kaydediliyor", role.getName());
        return roleRepo.save(role);
    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        log.info("Rol {} kullanıcıya {} ekleniyor", roleName,username );
        User user = userRepo.findByUsername(username);    //Repo kısmına yazdığımız findByUsername metoduyla kullanıcının adını bulduk
        Role role = roleRepo.findByName(roleName);        //Repo kısmına yazdığımız findByName metoduyla Rol sınıfından rol adını bulduk
        user.getRoles().add(role);           //User'ın rol kısmına rol ekledik

    }

    @Override
    public User getUser(String username) {
        log.info("kullanıcı {} getiriliyor", username );
        return userRepo.findByUsername(username);
    }

    @Override
    public List<User> getUsers() {
        log.info("Tüm kullanıcılar getiriliyor");
        return userRepo.findAll();
    }

}
