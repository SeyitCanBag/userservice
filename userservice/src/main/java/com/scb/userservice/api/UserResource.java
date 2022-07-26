package com.scb.userservice.api;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.userservice.domain.Calisanlar;
import com.scb.userservice.domain.Role;
import com.scb.userservice.domain.User;
import com.scb.userservice.service.CalisanlarServiceImpl;
import com.scb.userservice.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;
    private final CalisanlarServiceImpl calisanlarService;

    /*@Autowired
    public UserResource(UserService userService, CalisanlarServiceImpl calisanlarService){
        this.userService = userService;
        this.calisanlarService = calisanlarService;
    }*/

    @GetMapping("/users")
    public ResponseEntity<List<User>>getUsers(){     //Tüm kullanıcıları listeyecek bir metod
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/save")
    public ResponseEntity<User>saveUsers(@RequestBody User user){     //Tüm kullanıcıları listeyecek bir metod
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(user));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role>saveRole(@RequestBody Role role){     //Tüm kullanıcıları listeyecek bir metod
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }

    @PostMapping("/role/addtouser")
    public ResponseEntity<?>addRoleToUser(@RequestBody RoleToUserForm form){     //Tüm kullanıcıları listeyecek bir metod
        userService.addRoleToUser(form.getUsername(), form.getRoleName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/add/{calisan}")
    public void CalisanEkle(@PathVariable Calisanlar calisan){
        calisanlarService.calisanEkle(calisan);
    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                User user = userService.getUser(username);
                String access_token = JWT.create()  //Token oluşturduk ve aşağıda kullanıcının kimlik bilgilerini  yazacağız
                        .withSubject(user.getUsername()) //benzersiz bir şey seçilmesi daha iyi
                        .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))  //sona erme süresi(10dk)
                        .withIssuer(request.getRequestURL().toString()) //uygulamamızın url'i
                        .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))//Sahip olacağımız kurallar listesinin anahtarı olarak vereceğimiz isim budur
                        .sign(algorithm);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token",access_token);
                tokens.put("refresh_token",refresh_token);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }catch (Exception exception){
                //Diyelim ki token'ı doğrulayamadık
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
            throw new RuntimeException("Refresh token is missing");
        }

    }

}
@Data
class RoleToUserForm{
    private String username;
    private String roleName;
}
