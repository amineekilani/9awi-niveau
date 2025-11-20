package com.kawi_niveau.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;

import java.util.Arrays;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private TransactionalEmailsApi getApiInstance() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);
        return new TransactionalEmailsApi();
    }

    public void sendVerificationEmail(String toEmail, String displayName, String token) {
        try {
            TransactionalEmailsApi apiInstance = getApiInstance();
            
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail);
            sender.setName(senderName);

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(toEmail);
            recipient.setName(displayName);

            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Arrays.asList(recipient));
            sendSmtpEmail.setSubject("Confirmez votre adresse email - 9awi Niveau");
            sendSmtpEmail.setHtmlContent(
                "<html><body>" +
                "<h2>Bienvenue sur 9awi Niveau !</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Merci de vous être inscrit. Pour activer votre compte, veuillez confirmer votre adresse email en cliquant sur le lien ci-dessous :</p>" +
                "<p><a href='" + verificationLink + "' style='background-color: #4CAF50; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px; display: inline-block;'>Confirmer mon email</a></p>" +
                "<p>Ou copiez ce lien dans votre navigateur :</p>" +
                "<p>" + verificationLink + "</p>" +
                "<p>Ce lien est valide pendant 24 heures.</p>" +
                "<p>Si vous n'avez pas créé de compte, ignorez cet email.</p>" +
                "<br><p>Cordialement,<br>L'équipe 9awi Niveau</p>" +
                "</body></html>"
            );

            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String displayName, String token) {
        try {
            TransactionalEmailsApi apiInstance = getApiInstance();
            
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail);
            sender.setName(senderName);

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(toEmail);
            recipient.setName(displayName);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Arrays.asList(recipient));
            sendSmtpEmail.setSubject("Réinitialisation de votre mot de passe - 9awi Niveau");
            sendSmtpEmail.setHtmlContent(
                "<html><body>" +
                "<h2>Réinitialisation de mot de passe</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le lien ci-dessous pour créer un nouveau mot de passe :</p>" +
                "<p><a href='" + resetLink + "' style='background-color: #2196F3; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px; display: inline-block;'>Réinitialiser mon mot de passe</a></p>" +
                "<p>Ou copiez ce lien dans votre navigateur :</p>" +
                "<p>" + resetLink + "</p>" +
                "<p>Ce lien est valide pendant 1 heure.</p>" +
                "<p>Si vous n'avez pas demandé cette réinitialisation, ignorez cet email. Votre mot de passe restera inchangé.</p>" +
                "<br><p>Cordialement,<br>L'équipe 9awi Niveau</p>" +
                "</body></html>"
            );

            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage(), e);
        }
    }

    public void sendAccountDeletionEmail(String toEmail, String displayName, String token) {
        try {
            TransactionalEmailsApi apiInstance = getApiInstance();
            
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail);
            sender.setName(senderName);

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(toEmail);
            recipient.setName(displayName);

            String deleteLink = frontendUrl + "/confirm-delete?token=" + token;
            
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Arrays.asList(recipient));
            sendSmtpEmail.setSubject("Confirmation de suppression de compte - 9awi Niveau");
            sendSmtpEmail.setHtmlContent(
                "<html><body>" +
                "<h2>Suppression de compte</h2>" +
                "<p>Bonjour,</p>" +
                "<p>Vous avez demandé à supprimer votre compte. Cette action est irréversible et supprimera toutes vos données.</p>" +
                "<p>Pour confirmer la suppression de votre compte, cliquez sur le lien ci-dessous :</p>" +
                "<p><a href='" + deleteLink + "' style='background-color: #f44336; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px; display: inline-block;'>Confirmer la suppression</a></p>" +
                "<p>Ou copiez ce lien dans votre navigateur :</p>" +
                "<p>" + deleteLink + "</p>" +
                "<p>Ce lien est valide pendant 1 heure.</p>" +
                "<p><strong>Si vous n'avez pas demandé cette suppression, ignorez cet email et votre compte restera actif.</strong></p>" +
                "<br><p>Cordialement,<br>L'équipe 9awi Niveau</p>" +
                "</body></html>"
            );

            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de suppression: " + e.getMessage(), e);
        }
    }

    public void sendFailedLoginAlertEmail(String toEmail, String displayName, int attemptCount) {
        try {
            TransactionalEmailsApi apiInstance = getApiInstance();
            
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail);
            sender.setName(senderName);

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(toEmail);
            recipient.setName(displayName);
            
            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(Arrays.asList(recipient));
            sendSmtpEmail.setSubject("⚠️ Alerte de sécurité - Tentatives de connexion échouées");
            sendSmtpEmail.setHtmlContent(
                "<html><body>" +
                "<h2 style='color: #f44336;'>⚠️ Alerte de sécurité</h2>" +
                "<p>Bonjour,</p>" +
                "<p><strong>Nous avons détecté " + attemptCount + " tentatives de connexion échouées sur votre compte.</strong></p>" +
                "<p>Si vous êtes à l'origine de ces tentatives, veuillez vérifier votre mot de passe. Si vous l'avez oublié, vous pouvez le réinitialiser en cliquant sur le lien ci-dessous :</p>" +
                "<p><a href='" + frontendUrl + "/forgot-password' style='background-color: #2196F3; color: white; padding: 14px 20px; text-decoration: none; border-radius: 4px; display: inline-block;'>Réinitialiser mon mot de passe</a></p>" +
                "<p><strong style='color: #f44336;'>Si vous n'êtes pas à l'origine de ces tentatives, votre compte pourrait être compromis.</strong> Nous vous recommandons de :</p>" +
                "<ul>" +
                "<li>Changer immédiatement votre mot de passe</li>" +
                "<li>Vérifier l'activité récente de votre compte</li>" +
                "<li>Contacter notre support si nécessaire</li>" +
                "</ul>" +
                "<p>Pour votre sécurité, après 5 tentatives échouées, votre compte sera temporairement verrouillé pendant 15 minutes.</p>" +
                "<br><p>Cordialement,<br>L'équipe 9awi Niveau</p>" +
                "</body></html>"
            );

            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (ApiException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'alerte: " + e.getMessage(), e);
        }
    }
}
