package com.pleczycki.tgtg.security;

import com.pleczycki.tgtg.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Autowired
    private JwtConfig jwtConfig;

    public String generateToken(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date expirationDate = new Date(new Date().getTime() + jwtConfig.getExpirationInMs());

        log.info(String.valueOf(jwtConfig.getExpirationInMs()));
        log.info(String.valueOf(jwtConfig.getSecret()));

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
                .claim("username", userPrincipal.getUsername())
                .claim("email", userPrincipal.getEmail())
                .claim("roles", userPrincipal.getAuthorities())
                .compact();
    }

    Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtConfig.getSecret())
                .parseClaimsJws(token)
                .getBody();
        log.info(String.valueOf(jwtConfig.getSecret()));

        return Long.parseLong(claims.getSubject());
    }

    boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(authToken);
            log.info(String.valueOf(jwtConfig.getSecret()));

            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature = " + authToken);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token = " + authToken);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token = " + authToken);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token = " + authToken);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}