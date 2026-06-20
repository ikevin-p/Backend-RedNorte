package com.rednorte.mschatbot.repository;

import com.rednorte.mschatbot.model.MensajeChatbot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeChatbotRepository extends JpaRepository<MensajeChatbot, Long> {

    List<MensajeChatbot> findByIdentificadorConversacionOrderByFechaHoraAsc(String identificadorConversacion);

    void deleteByIdentificadorConversacion(String identificadorConversacion);
}
