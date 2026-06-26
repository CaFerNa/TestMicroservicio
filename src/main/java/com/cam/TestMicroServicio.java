package com.cam;

import org.teavm.interop.Export;
import org.teavm.jso.core.JSString;
import org.teavm.jso.JSExport;

public class TestMicroServicio {

    public static void main(String[] args) {
        // Inicialización si fuera necesaria
    }

    @Export(name = "getHTML")
    public static String getHTML(int[] charCodes) {
        //String response = Plantilla.renderHtml(args, 1);
        try {
            // 1. Convertimos el array de ints (primitivos) a chars (primitivos)
            char[] chars = new char[charCodes.length];
            for (int i = 0; i < charCodes.length; i++) {
                chars[i] = (char) charCodes[i];
            }
            String xmlReal = new String(chars);
            String response = Plantilla.renderHtml(xmlReal);
        return response;
        } catch (Exception e) {
            return "<div class='error'>Error procesando array: " + e.getMessage() + "</div>";
        }
    }
}