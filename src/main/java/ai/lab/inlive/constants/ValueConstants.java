package ai.lab.inlive.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("UTC+00:00");
    public static final String USER_ID_CLAIM = "user_id";
    public static final String USER_NAME_CLAIM = "name";
}