package com.commerceops.admin.auth.security;

import com.commerceops.admin.auth.model.AdminUser;
import com.commerceops.admin.auth.model.Role;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String generateToken(AdminUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenTtlSeconds());
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .sorted(Comparator.naturalOrder())
                .toList();

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of(
                "iss", properties.issuer(),
                "sub", user.getId().toString(),
                "publicId", user.getPublicId().toString(),
                "email", user.getEmail(),
                "roles", roles,
                "iat", now.getEpochSecond(),
                "exp", expiresAt.getEpochSecond()
        );

        String unsignedToken = base64Json(header) + "." + base64Json(payload);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public JwtClaims validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidJwtException();
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new InvalidJwtException();
        }

        Map<String, Object> payload = parseJson(parts[1]);
        if (!properties.issuer().equals(payload.get("iss"))) {
            throw new InvalidJwtException();
        }

        Instant expiresAt = Instant.ofEpochSecond(((Number) payload.get("exp")).longValue());
        if (!expiresAt.isAfter(Instant.now())) {
            throw new InvalidJwtException();
        }

        Long userId = Long.valueOf((String) payload.get("sub"));
        UUID publicId = UUID.fromString((String) payload.get("publicId"));
        String email = (String) payload.get("email");
        List<String> roles = ((List<?>) payload.get("roles")).stream()
                .map(String.class::cast)
                .toList();

        return new JwtClaims(userId, publicId, email, roles, expiresAt);
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtlSeconds();
    }

    private String base64Json(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize JWT.", exception);
        }
    }

    private Map<String, Object> parseJson(String value) {
        try {
            byte[] json = BASE64_URL_DECODER.decode(value);
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception exception) {
            throw new InvalidJwtException();
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT.", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
