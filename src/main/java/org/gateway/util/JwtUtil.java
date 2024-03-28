package org.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.Date;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;


	public boolean isInvalid(String token) throws AccessDeniedException {
		return this.isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) throws AccessDeniedException {
		return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
	}


	public Claims getAllClaimsFromToken(String token) throws AccessDeniedException {
		try {
			return Jwts.parser()
					.setSigningKey(secret)
					.parseClaimsJws(token)
					.getBody();
		} catch (SignatureException | ExpiredJwtException e) {
			throw new AccessDeniedException("Access denied: " + e.getMessage());
		}
	}

}
