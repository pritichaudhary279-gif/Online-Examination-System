package com.oes.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads {@code mail.properties} from the classpath (WEB-INF/classes in a WAR).
 */
public final class MailSettings {

    private static final Logger LOGGER = Logger.getLogger(MailSettings.class.getName());
    private static volatile MailSettings instance;

    private final boolean enabled;
    private final String smtpHost;
    private final String smtpPort;
    private final boolean startTls;
    private final boolean smtpAuth;
    private final String user;
    private final String password;
    private final String fromAddress;
    private final String fromPersonal;
    private final String sslProtocols;
    private final String sslTrust;
    private final boolean startTlsRequired;
    private final boolean smtpSslEnable;
    private final Set<String> skipRecipientDomains;

    private MailSettings(Properties p) {
        this.enabled = Boolean.parseBoolean(p.getProperty("mail.enabled", "false"));
        this.smtpHost = trim(p.getProperty("mail.smtp.host", ""));
        this.smtpPort = trim(p.getProperty("mail.smtp.port", "587"));
        this.startTls = Boolean.parseBoolean(p.getProperty("mail.smtp.starttls.enable", "true"));
        this.smtpAuth = Boolean.parseBoolean(p.getProperty("mail.smtp.auth", "true"));
        this.user = trim(p.getProperty("mail.user", ""));
        this.password = normalizeStoredPassword(p.getProperty("mail.password", ""));
        this.fromAddress = trim(p.getProperty("mail.from", ""));
        this.fromPersonal = trim(p.getProperty("mail.from.name", "Online Examination System"));
        this.sslProtocols = trim(p.getProperty("mail.smtp.ssl.protocols", "TLSv1.2"));
        this.sslTrust = trim(p.getProperty("mail.smtp.ssl.trust", ""));
        this.startTlsRequired = Boolean.parseBoolean(p.getProperty("mail.smtp.starttls.required", "false"));
        this.smtpSslEnable = Boolean.parseBoolean(p.getProperty("mail.smtp.ssl.enable", "false"));
        this.skipRecipientDomains = parseSkipRecipientDomains(p);
    }

    /**
     * Domains we never send to (demo seed accounts like teacher@oes.com), so Gmail does not bounce
     * "Address not found" back to mail.from. Override with mail.skip.recipient.domains in mail.properties;
     * set to empty to disable skipping.
     */
    private static Set<String> parseSkipRecipientDomains(Properties p) {
        String raw = p.getProperty("mail.skip.recipient.domains");
        if (raw == null) {
            return Set.of("oes.com", "example.com", "example.org", "test.com");
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        for (String part : trimmed.split(",")) {
            String d = part.trim().toLowerCase(Locale.ROOT);
            if (!d.isEmpty()) {
                set.add(d);
            }
        }
        return set;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Strips wrapping/accidental spaces (Gmail app passwords are 16 chars, often pasted with spaces).
     */
    private static String normalizeStoredPassword(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (t.contains(" ") && t.length() > 12) {
            return t.replaceAll("\\s+", "");
        }
        return t;
    }

    public static MailSettings get() {
        if (instance == null) {
            synchronized (MailSettings.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    private static MailSettings load() {
        Properties p = new Properties();
        try (InputStream in = MailSettings.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in != null) {
                p.load(in);
            } else {
                LOGGER.warning("classpath mail.properties not found; mail disabled");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not read mail.properties", e);
        }
        return new MailSettings(p);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getFromPersonal() {
        return fromPersonal;
    }

    public boolean isConfiguredForSending() {
        return enabled
                && !fromAddress.isBlank()
                && !smtpHost.isBlank()
                && (!smtpAuth || (!user.isBlank() && !password.isBlank()));
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public String getSslTrust() {
        return sslTrust;
    }

    public boolean isStartTlsRequired() {
        return startTlsRequired;
    }

    public boolean isSmtpSslEnable() {
        return smtpSslEnable;
    }

    public boolean looksLikeGmail() {
        return smtpHost.toLowerCase().contains("gmail.com");
    }

    /**
     * True when we should not SMTP to this address (invalid format or blocked demo domain).
     */
    public boolean shouldSkipRecipientEmail(String email) {
        if (email == null) {
            return true;
        }
        String e = email.trim();
        int at = e.lastIndexOf('@');
        if (at < 1 || at == e.length() - 1) {
            return true;
        }
        if (skipRecipientDomains.isEmpty()) {
            return false;
        }
        String domain = e.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return skipRecipientDomains.contains(domain);
    }
}
