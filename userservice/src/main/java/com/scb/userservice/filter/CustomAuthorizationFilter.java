package com.scb.userservice.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static java.util.Arrays.stream;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

//Bu bizim kullanıcıdan token'ı alıp kontrol edip rolüne göre yetki vereceğimiz sınıf burası.
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {


    @Override  //Gelen yanıtı filtreleme ve kişiye verilecek yetkiyi burada oluşturacağız
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/api/login") || request.getServletPath().equals("/api/token/refresh")){ //Url yollarını karşılaştırıp doğru mu diye kontrol ediyorum.(Giriş yapmayaçalıştığı yol bu mu)
            //kişi sadece giriş yapmaya çalışıyor
            filterChain.doFilter(request, response); //sadece isteği ve yanıtı ilettik. başka hiç bir şey yapmadık.Yani demek oluyor ki kişi sadece giriş yapmak istiyor bu yüzden biz hiçbir şey yapmıyoruz
        } else { //Bu kısımda kişi uygulamaya giriş yapmış olacak ve yetkisine erişecek
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
                try {
                    String token = authorizationHeader.substring("Bearer ".length());
                    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                    JWTVerifier verifier = JWT.require(algorithm).build();
                    DecodedJWT decodedJWT = verifier.verify(token);
                    String username = decodedJWT.getSubject();
                    String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    stream(roles).forEach(role -> {
                        authorities.add(new SimpleGrantedAuthority(role));
                    });
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    filterChain.doFilter(request,response);
                }catch (Exception exception){
                    //Diyelim ki token'ı doğrulayamadık
                    log.error("Error loggin in: {}", exception.getMessage());
                    response.setHeader("error", exception.getMessage());
                    response.setStatus(FORBIDDEN.value());
                    //response.sendError(FORBIDDEN.value());
                    Map<String,String> error = new HashMap<>();
                    error.put("error_message",exception.getMessage());
                    response.setContentType(APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), error);
                    //response.sendError(FORBIDDEN.value());
                }

            }else {
                filterChain.doFilter(request, response);
            }
        }
    }
}
