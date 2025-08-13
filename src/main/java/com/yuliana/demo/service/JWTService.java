package com.yuliana.demo.service;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yuliana.demo.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Service
public class JWTService {
	
	@Value("${application.security.jwt.secret-key}")
	private String secretKey;
	
	@Value("${application.security.jwt.expiration}")
	private Long jwtExpiration;
	@Value("${application.security.jwt.refresh-token.expiration}")
	private Long refreshExpiration;
	
	public String generateToken( final Usuario usuario) {
		return buildToken(usuario,jwtExpiration);
	}

	public String generateRefreshToken( final Usuario usuario) {
		return buildToken(usuario,refreshExpiration);
	}
	private String buildToken(final Usuario usuario, final Long expiration) {
	    return Jwts.builder()
	            .setId(usuario.getId().toString()) // ✅ CORREGIDO AQUÍ
	            .setClaims(Map.of("name", usuario.getNombre()))
	            .setSubject(usuario.getEmail())
	            .setIssuedAt(new Date(System.currentTimeMillis()))
	            .setExpiration(new Date(System.currentTimeMillis() + expiration))
	            .signWith(getSignInKey())
	            .compact();
	}


	private SecretKey getSignInKey() {
	    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
	    return Keys.hmacShaKeyFor(keyBytes);
	}

	public String extractUsername(String token) {
	    Claims claims = Jwts.parserBuilder()
	        .setSigningKey(getSignInKey())
	        .build()
	        .parseClaimsJws(token)
	        .getBody();
	    return claims.getSubject();
	}


	public boolean isTokenValid(final String token, final Usuario user) {
		final String username = extractUsername(token);
		return (username.equals(user.getEmail())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		final Claims jwtToken = Jwts.parserBuilder()
				 .setSigningKey(getSignInKey())
				.build()
				.parseClaimsJws(token)
	            .getBody();
		return jwtToken.getExpiration();
	}
}
