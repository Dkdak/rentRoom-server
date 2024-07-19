package com.mteam.sleerenthome.security.jwt;

import com.mteam.sleerenthome.security.user.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expirationTime}")
    private int jwtExpirationTime;

    public String generateJwtTokenForUser(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationTime * 1000L))  // Corrected expiration time
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("The secret key must be at least 256 bits");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserNameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parse(token);
            return true;
        } catch (MalformedJwtException me) {
            logger.error("invalid jwt token : {} ", me.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired token : {} ", e.getMessage());
        } catch (UnsupportedJwtException ue) {
            logger.error("not suported token : {} ", ue.getMessage());
        } catch (IllegalArgumentException ie) {
            logger.error("No claims found : {} ", ie.getMessage());
        }
        return false;
    }


    public String  generatorJwtSecretKey() {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        // Base64로 인코딩
        String base64EncodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        // 생성된 키 출력
        logger.info("Generated JWT Secret Key: " + base64EncodedKey);
        return base64EncodedKey;
    }
}
