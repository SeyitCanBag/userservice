package com.scb.userservice.security;

import com.scb.userservice.filter.CustomAuthenticationFilter;
import com.scb.userservice.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity        //web güvenliği
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {  //Güvenlik yapılandırma sınıfı.

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; //Şifre kaydedici

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {  //Bu methodda springe kullanıcıları nasıl aramamız gerektiğini söylemiş oluyoruz.
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);  //passwordEncoder = şifre kodlayıcı, userDetailsService = kullanıcı ayrıntıları hizmeti(kullanıcıları nasıl yüklemek istediğimizin bir yolu)
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
        customAuthenticationFilter.setFilterProcessesUrl("/api/login");
        http.csrf().disable();  //Siteler arası istek sahteciliğini devre dışı bıraktık.
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        //istekleri yetkilendirmeyi burda yapıyoruz (kimlere ne için izin vermeliyiz gibi)
        http.authorizeRequests().antMatchers("/api/login/**", "/api/token/refresh/**").permitAll();//permitAll() dediğimizde login kısmı için güvenlik istemiyoruz herkes erişebilir dedik
        http.authorizeRequests().antMatchers(GET, "/api/user/**").hasAnyAuthority("ROLE_USER");//kullanıcı olarak girdiyse kullanıcı rolüne sahip placak dedik
        http.authorizeRequests().antMatchers(POST, "/api/user/save/**").hasAnyAuthority("ROLE_ADMIN");  //kULLANICI eklemek için admin olması gerekiyor
        http.authorizeRequests().anyRequest().authenticated();  // istekleri yetkilendiren http yaptık ve herkesin yetkisine göre girebilmesine izin verdik
        http.addFilter(customAuthenticationFilter); // kimlik doğrulama filtresi oluşturuyoruz.
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

}
