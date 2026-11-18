package com.hijosdelasventas.backend.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {
    public static final Duration ACCESS_DURATION = Duration.ofHours(1);
    public static final Duration REFRESH_DURATION = Duration.ofDays(7);
    private static final String ISSUER = "hijosdelasventas";

    private final MACSigner signer;
    private final MACVerifier verifier;

    public JwtService(@Value("${auth.jwt.secret}") String secret) throws JOSEException {
        byte[] key = Base64.getDecoder().decode(secret);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe contener al menos 32 bytes codificados en Base64");
        }
        signer = new MACSigner(key);
        verifier = new MACVerifier(key);
    }

    public String accessToken(long usuarioId) {
        return sign(usuarioId, "access", ACCESS_DURATION);
    }

    public String refreshToken(long usuarioId) {
        return sign(usuarioId, "refresh", REFRESH_DURATION);
    }

    public Optional<Long> validateAccess(String token) {
        return validate(token, "access");
    }

    public Optional<Long> validateRefresh(String token) {
        return validate(token, "refresh");
    }

    private String sign(long usuarioId, String purpose, Duration duration) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(Long.toString(usuarioId))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(duration)))
                .jwtID(UUID.randomUUID().toString())
                .claim("purpose", purpose)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("No se pudo firmar el JWT", exception);
        }
    }

    private Optional<Long> validate(String token, String purpose) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(verifier)) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!ISSUER.equals(claims.getIssuer()) || !purpose.equals(claims.getStringClaim("purpose"))
                    || claims.getExpirationTime() == null || !claims.getExpirationTime().toInstant().isAfter(Instant.now())
                    || claims.getSubject() == null) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (ParseException | JOSEException | NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
