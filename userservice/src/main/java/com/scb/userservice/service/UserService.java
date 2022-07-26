package com.scb.userservice.service;

import com.scb.userservice.domain.Calisanlar;
import com.scb.userservice.domain.Role;
import com.scb.userservice.domain.User;
import java.util.List;

public interface UserService {

    User saveUser(User user);       //Bu yöntemi her çağırdığımda kullanıcıyı veritabanına kaydedeceğim.(Ve geri dönüş olarak User alacağım)
    Role saveRole(Role role);       //Rolü veritabanına kaydettik ve bu metod rolü bana geri verecek(dönüş olarak yine rolü alacağız)
    void addRoleToUser(String username, String roleName);     //kullanıcıya rol eklemek(parametre olarak username ve role name)
    User getUser(String username);        //Bu sadece user dönderecek bize ve username benzersiz olduğu için parametre olarak username kullandık.Yani bu metod girilen kullanıcı adına göre bize kullanıcı adını getirecek
    List<User>getUsers();                //Tüm kullanıcıların listesini verecek.(Liste halinde)

    //void CalisanEkle(Calisanlar calisan);   //Çalışan ekleme -> UserServiceImpl
}
