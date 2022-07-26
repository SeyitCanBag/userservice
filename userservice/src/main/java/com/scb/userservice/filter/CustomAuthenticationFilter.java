package com.scb.userservice.filter; //filtrelerim bu paketin içinde olacak

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//Özel kimlik dorulama filtresi
@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {  //Kullanıcı adı ve parola doğrulama filtresini extends ettik.

    private final AuthenticationManager authenticationManager;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }


    @Override
    //Kimlik doğrulama girişimi.Bu metodda istekle beraber aldığımız bilgileri(kullanıcı adı,şifre) doğrulama belirtecine iletiyoruz. sonra anahenticationManager'ı çağırırız ve ona bu istek ve bu bilgilerle burada oturum açan kullanıcının kimliğini doğrulamasını söylüyoruz.
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");  //kullanıcı adını aldık
        String password = request.getParameter("password");  //passwordu aldık
        log.info("username is : {}", username);log.info("password is : {}", password);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,password);//kullanıcı adı ve şifre doğrulama belirtecini göndereceğiz. Ve ona kimlik doğrulama token'ı diyeceğiz. ve onu yeni bir kullanıcı adı ve şifresi kimlik doğrulama token'ına eşitleyeceğim
        return authenticationManager.authenticate(authenticationToken);

    }

    @Override
    //Başarılı kimlik doğrulama
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        User user = (User) authentication.getPrincipal();//Başarılı bir şekilde girmiş kullanıcı için bir kullanıcı oluşturuyoruz.(authentication = kimlik doğrulama ilkesi)
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());//JSON Web Tokenını ve yenileme tokenını imzalayacağım algoritma bu olacak.
        String access_token = JWT.create()  //Token oluşturduk ve aşağıda kullanıcının kimlik bilgilerini  yazacağız
                .withSubject(user.getUsername()) //benzersiz bir şey seçilmesi daha iyi
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))  //sona erme süresi(10dk)
                .withIssuer(request.getRequestURL().toString()) //uygulamamızın url'i
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))//Sahip olacağımız kurallar listesinin anahtarı olarak vereceğimiz isim budur
                .sign(algorithm);
        String refresh_token = JWT.create()  //Yenileme token'ımız olacak
                .withSubject(user.getUsername()) //benzersiz bir şey seçilmesi daha iyi
                .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))  //sona erme süresi(10dk)
                .withIssuer(request.getRequestURL().toString()) //uygulamamızın url'i
                .sign(algorithm);

        /*response.setHeader("access_token",access_token);        //erişim tokenını response kullanarak ön uçtaki kullanıcıya vereceğiz.
        response.setHeader("refresh_token",refresh_token);    //yenileme tokenını response kullanarak ön uçtaki kullanıcıya vereceğiz.
        //ve artıkKullanıcılar başarılı bir şekilde giriş yaptığında yanıttaki başlıkları kontrol edebiliriz*/
        //Aşağıdaki kısım sadece hader yerine tüm tokenın birleştirilmiş halini verecek
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token",access_token);
        tokens.put("refresh_token",refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

    }
}
