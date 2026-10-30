package com.vivso.Vivso.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    try {
      Map resultado = cloudinary.uploader().upload(
        archivo.getBytes(),
        ObjectUtils.asMap("folder", nombreCarpeta)
      );
      return resultado.get("secure_url").toString();
    } catch (IOException e) {
      throw new RuntimeException("Error al subir archivo a Cloudinary", e);
    }
  }

  public void borrarArchivo(String urlSegura) {
    if (urlSegura == null || !urlSegura.contains("cloudinary")) {
      return;
    }
    try {
      // Extrae el Public ID de Cloudinary desde la URL para poder borrarlo
      String[] parts = urlSegura.split("/");
      String lastPart = parts[parts.length - 1]; // nombre_archivo.jpg
      String folder = parts[parts.length - 2];   // nombre_carpeta
      String nombreSinExtension = lastPart.substring(0, lastPart.lastIndexOf("."));
      String publicId = folder + "/" + nombreSinExtension;

      cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    } catch (Exception e) {
      log.error("No se pudo borrar el archivo en Cloudinary: {}", urlSegura, e);
    }
  }
}