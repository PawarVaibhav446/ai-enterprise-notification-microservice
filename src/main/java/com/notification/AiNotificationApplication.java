package com.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AiNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiNotificationApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════╗
            ║  🚀 AI Notification Service Started!            ║
            ║  📧 Email | 📱 SMS | 💬 WhatsApp                ║
            ║  🤖 Powered by Llama 3 (Groq)                   ║
            ║                                                   ║
            ║  🌐 API: http://localhost:8080                   ║
            ║  🐰 RabbitMQ UI: http://localhost:15672         ║
            ║  📊 Health: http://localhost:8080/actuator/health║
            ╚═══════════════════════════════════════════════════╝
            
            """);
    }
}