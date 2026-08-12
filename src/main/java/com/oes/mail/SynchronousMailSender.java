package com.oes.mail;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Blocking SMTP send; invoke only from background worker threads.
 */
public class SynchronousMailSender {

    private static final Logger LOGGER = Logger.getLogger(SynchronousMailSender.class.getName());
    private final MailSettings settings;

    public SynchronousMailSender() {
        this.settings = MailSettings.get();
    }

    public void sendPlain(String to, String subject, String textBody) {
        if (to == null || to.isBlank()) {
            return;
        }
        if (!settings.isEnabled()) {
            LOGGER.fine(() -> "Mail disabled; skip to=" + to + " subject=" + subject);
            return;
        }
        if (!settings.isConfiguredForSending()) {
            LOGGER.warning("Mail enabled but SMTP/from not fully configured; skip send to=" + to);
            return;
        }
        if (settings.shouldSkipRecipientEmail(to)) {
            LOGGER.info(() -> "Skip send to undeliverable or demo-domain address (no bounce to sender): " + to);
            return;
        }

        if (settings.looksLikeGmail()
                && settings.isSmtpAuth()
                && !settings.getFromAddress().equalsIgnoreCase(settings.getUser())) {
            LOGGER.warning("Gmail: mail.from should usually match mail.user (the account that signs in), "
                    + "unless you configured 'Send mail as' in Google. from=" + settings.getFromAddress()
                    + " user=" + settings.getUser());
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", settings.getSmtpHost());
        props.put("mail.smtp.port", settings.getSmtpPort());
        props.put("mail.smtp.auth", Boolean.toString(settings.isSmtpAuth()));
        props.put("mail.smtp.starttls.enable", Boolean.toString(settings.isStartTls()));
        props.put("mail.smtp.starttls.required", Boolean.toString(settings.isStartTlsRequired()));
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        if (!settings.getSslProtocols().isEmpty()) {
            props.put("mail.smtp.ssl.protocols", settings.getSslProtocols());
        }
        if (!settings.getSslTrust().isEmpty()) {
            props.put("mail.smtp.ssl.trust", settings.getSslTrust());
        } else if (settings.looksLikeGmail()) {
            props.put("mail.smtp.ssl.trust", settings.getSmtpHost());
        }
        if (settings.isSmtpSslEnable()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", settings.getSmtpPort());
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        Session session;
        if (settings.isSmtpAuth()) {
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(settings.getUser(), settings.getPassword());
                }
            });
        } else {
            session = Session.getInstance(props);
        }

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(settings.getFromAddress(), settings.getFromPersonal(), "UTF-8"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(textBody == null ? "" : textBody, "UTF-8");

            Transport.send(msg);
            LOGGER.info(() -> "Mail sent to=" + to + " subject=" + subject);
        } catch (AuthenticationFailedException e) {
            logAuthFailureHint(to, e);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "Failed to send mail to=" + to, e);
        }
    }

    private void logAuthFailureHint(String to, AuthenticationFailedException e) {
        LOGGER.log(Level.WARNING, "SMTP login rejected for recipient=" + to + " — check mail.user / mail.password in mail.properties", e);
        if (settings.looksLikeGmail()) {
            LOGGER.warning(
                    "Gmail (535): Use your full email in mail.user, and a Google 'App Password' in mail.password "
                            + "(not your normal Gmail password). Enable 2-Step Verification, then: "
                            + "Google Account > Security > App passwords > create one for Mail. "
                            + "Paste the 16-character password with no spaces. "
                            + "Set mail.from to the same address as mail.user unless you use Send mail as. "
                            + "Details: https://support.google.com/mail/?p=BadCredentials");
        }
    }
}
