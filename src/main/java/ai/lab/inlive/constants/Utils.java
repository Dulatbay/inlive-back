package ai.lab.inlive.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static ai.lab.inlive.constants.ValueConstants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static String extractIdFromToken(JwtAuthenticationToken token) {
        return token.getToken().getClaim(USER_ID_CLAIM);
    }

    public static String extractNameFromToken(JwtAuthenticationToken token) {
        return token.getToken().getClaim(USER_NAME_CLAIM);
    }

    public static List<String> getAuthorities(Collection<GrantedAuthority> authorities) {
        return authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    public static LocalDateTime getLocalDateTimeFromTimestamp(Long timestamp) {
        if (timestamp == null) return null;
        return Instant.ofEpochMilli(timestamp).atZone(ZONE_ID).toLocalDateTime();
    }

    public static long getTimeStampFromLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return 0;
        return Timestamp.valueOf(localDateTime).getTime();
    }
}