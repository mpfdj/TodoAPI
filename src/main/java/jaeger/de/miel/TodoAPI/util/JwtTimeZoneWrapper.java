package jaeger.de.miel.TodoAPI.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;

/**
 * Wrapper class to display JWT timestamps in different time zones
 */
public class JwtTimeZoneWrapper {

    private final Jwt jwt;
    private final ZoneId displayZone;

    public JwtTimeZoneWrapper(Jwt jwt) {
        this(jwt, ZoneId.systemDefault());
    }

    public JwtTimeZoneWrapper(Jwt jwt, ZoneId displayZone) {
        this.jwt = jwt;
        this.displayZone = displayZone;
    }

    // ===== FORMATTING METHODS =====

    public String getExpirationInDisplayZone() {
        return formatInstant(jwt.getExpiresAt(), displayZone);
    }

    public String getIssuedAtInDisplayZone() {
        return formatInstant(jwt.getIssuedAt(), displayZone);
    }

    public String getNotBeforeInDisplayZone() {
        return formatInstant(jwt.getNotBefore(), displayZone);
    }

    public String getExpirationInAmsterdam() {
        return formatInstant(jwt.getExpiresAt(), ZoneId.of("Europe/Amsterdam"));
    }

    public String getIssuedAtInAmsterdam() {
        return formatInstant(jwt.getIssuedAt(), ZoneId.of("Europe/Amsterdam"));
    }

    public String getExpirationInUTC() {
        return formatInstant(jwt.getExpiresAt(), ZoneOffset.UTC);
    }

    public String getIssuedAtInUTC() {
        return formatInstant(jwt.getIssuedAt(), ZoneOffset.UTC);
    }

    // ===== TIME REMAINING CALCULATIONS =====

    public long getSecondsRemaining() {
        return getTimeRemaining(ChronoUnit.SECONDS);
    }

    public long getMinutesRemaining() {
        return getTimeRemaining(ChronoUnit.MINUTES);
    }

    public long getHoursRemaining() {
        return getTimeRemaining(ChronoUnit.HOURS);
    }

    public boolean isExpired() {
        Instant expiresAt = jwt.getExpiresAt();
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isAboutToExpire(long minutesThreshold) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return false;

        long minutesRemaining = ChronoUnit.MINUTES.between(Instant.now(), expiresAt);
        return minutesRemaining > 0 && minutesRemaining <= minutesThreshold;
    }

    // ===== TIMEZONE CONVERSION UTILITIES =====

    public LocalDateTime getExpirationAsLocalDateTime(ZoneId zone) {
        Instant exp = jwt.getExpiresAt();
        return exp != null ? LocalDateTime.ofInstant(exp, zone) : null;
    }

    public LocalDateTime getIssuedAtAsLocalDateTime(ZoneId zone) {
        Instant iat = jwt.getIssuedAt();
        return iat != null ? LocalDateTime.ofInstant(iat, zone) : null;
    }

    // ===== FORMATTED SUMMARIES =====

    public Map<String, String> getTimeSummary() {
        return Map.of(
                "exp_utc", getExpirationInUTC(),
                "exp_amsterdam", getExpirationInAmsterdam(),
                "exp_display_zone", getExpirationInDisplayZone(),
                "iat_utc", getIssuedAtInUTC(),
                "iat_amsterdam", getIssuedAtInAmsterdam(),
                "iat_display_zone", getIssuedAtInDisplayZone(),
                "seconds_remaining", String.valueOf(getSecondsRemaining()),
                "is_expired", String.valueOf(isExpired())
        );
    }

    // ===== DELEGATE METHODS TO ORIGINAL JWT =====

    public String getSubject() {
        return jwt.getSubject();
    }

    public String getClaimAsString(String claim) {
        return jwt.getClaimAsString(claim);
    }

    public <T> T getClaim(String claim) {
        return jwt.getClaim(claim);
    }

    public Map<String, Object> getClaims() {
        return jwt.getClaims();
    }

    public String getTokenValue() {
        return jwt.getTokenValue();
    }

    public Instant getExpiresAt() {
        return jwt.getExpiresAt();
    }

    public Instant getIssuedAt() {
        return jwt.getIssuedAt();
    }

    // ===== PRIVATE HELPER METHODS =====

    private String formatInstant(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return LocalDateTime.ofInstant(instant, zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                " (" + zoneId.getId() + ")";
    }

    private long getTimeRemaining(ChronoUnit unit) {
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return -1;

        Instant now = Instant.now();
        if (now.isAfter(expiresAt)) {
            return -unit.between(expiresAt, now);
        }
        return unit.between(now, expiresAt);
    }

    // ===== STATIC UTILITY METHODS =====

    public static JwtTimeZoneWrapper of(Jwt jwt) {
        return new JwtTimeZoneWrapper(jwt);
    }

    public static JwtTimeZoneWrapper of(Jwt jwt, ZoneId zoneId) {
        return new JwtTimeZoneWrapper(jwt, zoneId);
    }

    public static Function<Jwt, JwtTimeZoneWrapper> toAmsterdamWrapper() {
        return jwt -> new JwtTimeZoneWrapper(jwt, ZoneId.of("Europe/Amsterdam"));
    }

    public static Function<Jwt, JwtTimeZoneWrapper> toUTCWrapper() {
        return jwt -> new JwtTimeZoneWrapper(jwt, ZoneOffset.UTC);
    }
}