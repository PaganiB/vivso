package com.vivso.Vivso.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class ExpoPushClient {

  private static final String URL = "https://exp.host/--/api/v2/push/send";
  private static final int MAX_POR_LOTE = 100; // límite de Expo por request

  private final RestClient restClient;

  public ExpoPushClient() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(5));
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  public static boolean esTokenValido(String token) {
    return token != null
      && (token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken["))
      && token.endsWith("]");
  }

  /** Envía en lotes de 100 y devuelve los tokens que Expo marcó como DeviceNotRegistered. */
  public List<String> enviar(List<String> tokens, String titulo, String cuerpo, Map<String, String> data) {
    List<String> invalidos = new ArrayList<>();
    List<String> validos = tokens.stream().filter(ExpoPushClient::esTokenValido).distinct().toList();

    for (int i = 0; i < validos.size(); i += MAX_POR_LOTE) {
      List<String> lote = validos.subList(i, Math.min(i + MAX_POR_LOTE, validos.size()));
      try {
        invalidos.addAll(enviarLote(lote, titulo, cuerpo, data));
      } catch (Exception e) {
        log.error("Error enviando lote a Expo (el resto de lotes sigue): ", e);
      }
    }
    return invalidos;
  }

  private List<String> enviarLote(List<String> lote, String titulo, String cuerpo, Map<String, String> data) {
    List<Map<String, Object>> mensajes = new ArrayList<>();
    for (String token : lote) {
      Map<String, Object> m = new HashMap<>();
      m.put("to", token);
      m.put("title", titulo);
      m.put("body", cuerpo);
      m.put("data", data);
      m.put("sound", "default");
      m.put("priority", "high");
      mensajes.add(m);
    }

    Map<String, Object> body = restClient.post()
      .uri(URL)
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .body(mensajes)
      .retrieve()
      .body(new ParameterizedTypeReference<Map<String, Object>>() {});

    List<String> invalidos = new ArrayList<>();
    if (body == null || !(body.get("data") instanceof List<?> tickets)) {
      log.warn("Respuesta inesperada de Expo: {}", body);
      return invalidos;
    }

    // Los tickets vienen en el MISMO orden que los mensajes: el ticket i corresponde a lote.get(i)
    for (int i = 0; i < tickets.size() && i < lote.size(); i++) {
      if (!(tickets.get(i) instanceof Map<?, ?> ticket) || "ok".equals(ticket.get("status"))) continue;
      Object error = ticket.get("details") instanceof Map<?, ?> d ? d.get("error") : null;
      if ("DeviceNotRegistered".equals(error)) {
        invalidos.add(lote.get(i));
      } else {
        log.warn("Expo rechazó un mensaje: {}", ticket.get("message"));
      }
    }
    return invalidos;
  }
}