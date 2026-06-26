package com.cam;

import java.util.ArrayList;
import java.util.List;

public class Plantilla {

    /**
     * Extrae el contenido de una etiqueta XML de un bloque de texto.
     * Es un método simple que no maneja atributos ni XML complejo.
     * @param xmlBlock El string que contiene la etiqueta (ej: "<id>123</id>...")
     * @param tagName El nombre de la etiqueta a buscar (ej: "id")
     * @return El contenido de la etiqueta o una cadena vacía si no se encuentra.
     */
    private static String extractValueSafe(String xmlBlock, String tagName) {
        // 1. Buscamos el inicio de la etiqueta, ej: "<fecha_inicio"
        String startPattern = "<" + tagName;
        int startNodeIdx = xmlBlock.indexOf(startPattern);

        if (startNodeIdx == -1) {
            return ""; // La etiqueta no existe en este bloque
        }

        // 2. Buscamos dónde se cierra la apertura de la etiqueta: ">"
        int endOfStartTagIdx = xmlBlock.indexOf(">", startNodeIdx);
        if (endOfStartTagIdx == -1) {
            return ""; // XML mal formado
        }

        // 3. ¿Es una etiqueta auto-cerrada? (ej: "... />")
        if (endOfStartTagIdx > 0 && xmlBlock.charAt(endOfStartTagIdx - 1) == '/') {
            return ""; // Está vacía
        }

        // 4. El contenido real empieza justo después del ">"
        int contentStartIdx = endOfStartTagIdx + 1;

        // 5. Buscamos la etiqueta de cierre: "</fecha_inicio>"
        String endPattern = "</" + tagName + ">";
        int contentEndIdx = xmlBlock.indexOf(endPattern, contentStartIdx);

        if (contentEndIdx == -1) {
            return ""; // No tiene cierre, devolvemos vacío para no crashear
        }

        // 6. Extraemos y limpiamos el texto
        return xmlBlock.substring(contentStartIdx, contentEndIdx).trim();
    }

    /**
     * El método principal que recibe el XML completo y devuelve el HTML.
     */
    public static String renderHtml(String xmlData) {
        try {
            List<Proyecto> proyectos = new ArrayList<>();
            int cursor = 0;

            // Bucle principal para separar cada proyecto
            while (true) {
                // Buscamos "<proyecto" (sin cerrar el >, por si tiene atributos)
                int startProj = xmlData.indexOf("<proyecto", cursor);
                if (startProj == -1) {
                    break; // No hay más proyectos
                }

                // Buscamos el final de este proyecto
                int endProj = xmlData.indexOf("</proyecto>", startProj);
                if (endProj == -1) {
                    // Si el XML está cortado, cogemos hasta el final para no crashear
                    endProj = xmlData.length();
                }

                // Aislamos solo el texto de ESTE proyecto
                String block = xmlData.substring(startProj, endProj);

                // Parseamos los datos usando nuestro método seguro
                Proyecto p = new Proyecto();
                p.id = extractValueSafe(block, "id"); // Si no existe, devolverá ""
                p.referencia = extractValueSafe(block, "referencia");
                p.titulo_esp = extractValueSafe(block, "titulo_esp");
                p.nombre_instituto = extractValueSafe(block, "nombre_instituto");

                // Formateamos la fecha si existe (quitar la T y los milisegundos si quieres, aquí la pasamos tal cual)
                p.fecha_inicio = extractValueSafe(block, "fecha_inicio");
                p.fecha_fin = extractValueSafe(block, "fecha_fin");

                p.cantidad = extractValueSafe(block, "cantidad");
                p.area_id = extractValueSafe(block, "area_id");
                p.instituto_id = extractValueSafe(block, "instituto_id");

                proyectos.add(p);

                // Movemos el cursor para la siguiente iteración
                cursor = endProj + "</proyecto>".length();
            }

            // --- Lógica de Pintado HTML (Sin Cambios) ---
            if (proyectos.isEmpty()) {
                return "<div class='empty'>No se encontraron proyectos en la respuesta XML.</div>";
            }

            StringBuilder html = new StringBuilder();
            html.append("<div class='meta'>Mostrando ").append(proyectos.size()).append(" proyecto(s)</div>");
            html.append("<table><thead><tr>");
            html.append("<th>id</th><th>referencia</th><th>titulo_esp</th><th>nombre_instituto</th>");
            html.append("<th>fecha_inicio</th><th>fecha_fin</th><th>cantidad</th><th>area_id</th><th>instituto_id</th>");
            html.append("</tr></thead><tbody>");

            for (Proyecto p : proyectos) {
                html.append("<tr>");
                html.append("<td class='num'>").append(p.id).append("</td>");
                html.append("<td class='wrap'>").append(p.referencia).append("</td>");
                html.append("<td class='wrap'>").append(p.titulo_esp).append("</td>");
                html.append("<td class='wrap'>").append(p.nombre_instituto).append("</td>");
                html.append("<td class='date'>").append(p.fecha_inicio.split("T")[0]).append("</td>"); // Pequeño truco para que la fecha quede limpia (YYYY-MM-DD)
                html.append("<td class='date'>").append(p.fecha_fin.split("T")[0]).append("</td>");
                html.append("<td class='num'>").append(p.cantidad).append("</td>");
                html.append("<td class='num'>").append(p.area_id).append("</td>");
                html.append("<td class='num'>").append(p.instituto_id).append("</td>");
                html.append("</tr>");
            }

            html.append("</tbody></table>");
            return html.toString();

        } catch (Exception e) {
            // Ahora este bloque sí capturará cualquier error no previsto y lo mostrará en pantalla en vez de bloquearse.
            return "<div class='error'><strong>WASM Error:</strong> " + e.toString() + "</div>";
        }
    }
}