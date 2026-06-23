package com.example.miniproject.service.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private MailSender mailSender;

    public void sendApprovalEmail(String toEmail, String ownerName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("✅ บัญชีของคุณได้รับการอนุมัติแล้ว - ระบบท่องเที่ยวชุมชนชนเผ่า");
        message.setText(
            "เรียน คุณ" + ownerName + ",\n\n" +
            "บัญชีเจ้าของโฮมสเตย์ของคุณได้รับการอนุมัติจาก Admin เรียบร้อยแล้ว\n" +
            "คุณสามารถเข้าสู่ระบบได้ที่: http://localhost:8080/owner/login\n\n" +
            "ขอบคุณที่ร่วมเป็นส่วนหนึ่งของระบบท่องเที่ยวชุมชนชนเผ่า\n\n" +
            "ทีมงานระบบท่องเที่ยวชุมชนชนเผ่า"
        );
        mailSender.send(message);
    }

    public void sendRejectionEmail(String toEmail, String ownerName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("❌ คำขอสมัครของคุณไม่ผ่านการอนุมัติ - ระบบท่องเที่ยวชุมชนชนเผ่า");
        message.setText(
            "เรียน คุณ" + ownerName + ",\n\n" +
            "ขออภัย คำขอสมัครบัญชีเจ้าของโฮมสเตย์ของคุณไม่ผ่านการอนุมัติ\n" +
            "หากมีข้อสงสัยกรุณาติดต่อทีมงานของเรา\n\n" +
            "ทีมงานระบบท่องเที่ยวชุมชนชนเผ่า"
        );
        mailSender.send(message);
    }

    public void sendManagerCreatedEmail(String toEmail, String managerName, String password) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("✅ บัญชีผู้จัดการท่องเที่ยวชุมชนของคุณถูกสร้างแล้ว");
    message.setText(
        "เรียน คุณ" + managerName + ",\n\n" +
        "แอดมินได้สร้างบัญชีผู้จัดการท่องเที่ยวชุมชนให้คุณเรียบร้อยแล้ว\n\n" +
        "ข้อมูลสำหรับเข้าสู่ระบบ:\n" +
        "  Username (อีเมล): " + toEmail + "\n" +
        "  Password: " + password + "\n\n" +
        "คุณสามารถเข้าสู่ระบบได้ที่: http://localhost:8080/manager/login\n\n" +
        "ทีมงานระบบท่องเที่ยวชุมชนชนเผ่า"
    );
    mailSender.send(message);
}
}