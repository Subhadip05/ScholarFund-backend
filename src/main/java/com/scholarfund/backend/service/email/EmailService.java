package com.scholarfund.backend.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:scholarfund101@gmail.com}")
    private String senderEmail;

    @Async("virtualThreadTaskExecutor")
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "ScholarFund Verification");
            helper.setTo(toEmail);
            helper.setSubject("Your ScholarFund OTP Verification Code");

            String displayName = "Applicant";

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #2b6cb0;">ScholarFund Security</h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Use the one-time verification code below to proceed with your ScholarFund application:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1a202c; background: #edf2f7; padding: 10px 24px; border-radius: 6px;">%s</span>
                    </div>
                    <p style="color: #718096; font-size: 13px;">This code will expire in 5 minutes. Do not share this code with anyone.</p>
                </div>
                """.formatted(displayName, otp);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("OTP successfully dispatched to {}", toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to deliver OTP to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email dispatch failed: " + e.getMessage());
        }
    }
}