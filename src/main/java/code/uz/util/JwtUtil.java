package code.uz.util;

import code.uz.dto.JwtDTO;
import code.uz.enums.Role;
import code.uz.exception.BadException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final int liveTimeToken = 1000 * 3600 * 24;
    private static final String secretKey = "aEo4QFpxOSQybkMjWGUhbUxyN3RWcDMqRGtZMSZiV3M=";

    public static String encode(String username, UUID id, List<Role> role) {
        String strRoles = role.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));

        Map<String, String> claims = new HashMap<>();
        claims.put("roles", strRoles);
        claims.put("id", id.toString());

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + liveTimeToken))
                .signWith(getSignInKey())
                .compact();
    }

    public static String encodeID(UUID id) {
        return Jwts.builder()
                .subject(String.valueOf(id))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000))
                .signWith(getSignInKey())
                .compact();
    }

    private static SecretKey getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    public static UUID decodeID(String token) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            throw new BadException("Link already expired");
        }

    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        UUID id = UUID.fromString(claims.get("id", String.class));
        String roles = (String) claims.get("roles");
        List<Role> roleList = Arrays.stream(roles.split(","))
                .map(Role::valueOf)
                .toList();

        return new JwtDTO(username, id, roleList);
    }

  /*  public static String refreshToken(String phone, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("phone", phone);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(phone)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + liveTimeTokenRefresh))
                .signWith(getSignInKey())
                .compact();
    }*/
}
