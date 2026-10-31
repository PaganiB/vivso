package com.vivso.Vivso.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vivso.Vivso.Exception.ReglaDeNegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

  private final Cloudinary cloudinary;

  // Se inicializa Cloudinary una sola vez en toda la app
  public CloudinaryService(@Value("${CLOUDINARY_URL}") String cloudinaryUrl) {
    this.cloudinary = new Cloudinary(cloudinaryUrl);
  }

  public String subirArchivo(MultipartFile archivo, String nombreCarpeta) {
    if (archivo == null || archivo.isEmpty()) {
      throw new ReglaDeNegocioException("El archivo recibido está vacío o es nulo.", HttpStatus.BAD_REQUEST);
    }
    try {
      Map resultado = cloudinary.uploader().upload(
        archivo.getBytes(),
        ObjectUtils.asMap("folder", nombreCarpeta)
      );
      return resultado.get("secure_url").toString();
    } catch (IOException e) {
      log.error("Error al procesar el archivo local antes de enviarlo a Cloudinary: {}", e.getMessage());
      throw new ReglaDeNegocioException("Error al procesar el archivo.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public void borrarArchivo(String urlSegura) {
    if (urlSegura == null || !urlSegura.contains("cloudinary")) return;
    try {
      // CORRECCIÓN: Uso de Regex seguro para extraer el public_id independientemente de la versión
      String publicId = urlSegura.replaceAll("^.*/v\\d+/(.*)\\.[a-zA-Z0-9]+$", "$1");

      // CORRECCIÓN: invalidar en CDN para que desaparezca inmediatamente
      cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
    } catch (Exception e) {
      log.error("No se pudo borrar el archivo en Cloudinary: {}", urlSegura, e);
    }
  }
}