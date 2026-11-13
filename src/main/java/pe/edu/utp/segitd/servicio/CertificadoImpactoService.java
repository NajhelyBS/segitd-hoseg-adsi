package pe.edu.utp.segitd.servicio;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import pe.edu.utp.segitd.modelo.Comunidad;
import pe.edu.utp.segitd.modelo.FilaHistorialDespacho;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CertificadoImpactoService {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_SOLO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReporteService reporteService = new ReporteService();

    public File emitirCertificado(OffsetDateTime desde, OffsetDateTime hasta, Comunidad comunidad) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new ServicioException(
                    "El rango de fechas es inválido: la fecha «Desde» no puede ser posterior a la fecha «Hasta».");
        }

        Integer idComunidad = comunidad == null ? null : comunidad.getId();
        List<FilaHistorialDespacho> historial = reporteService.generarHistorialDespachos(desde, hasta, idComunidad);

        if (historial.isEmpty()) {
            throw new ServicioException(
                    "No existen lotes de donación entregados en el periodo seleccionado para emitir un certificado.");
        }

        ResumenImpacto resumen = reporteService.calcularResumen(desde, hasta, historial);

        String alcanceComunidad = comunidad != null
                ? comunidad.getNombre() + " (" + comunidad.getRegion() + ")"
                : resumen.comunidadesAtendidas() + " comunidad(es) altoandina(s)";

        String ongsAliadas = historial.stream()
                .map(FilaHistorialDespacho::ong)
                .filter(o -> o != null && !o.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
        if (ongsAliadas.isBlank()) {
            ongsAliadas = "ONG Pachamama Raymi";
        }

        String codigoValidacion = "CERT-B-" + (System.currentTimeMillis() % 1_000_000);
        String fechaEmision = LocalDateTime.now().format(FORMATO_FECHA_HORA);
        String periodoTexto = FORMATO_SOLO_FECHA.format(desde) + " al " + FORMATO_SOLO_FECHA.format(hasta);
        String rutaArchivo = "boletas/Certificado_Impacto_Hoseg_" + codigoValidacion + ".pdf";
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            PDFont fuenteBold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
            PDFont fuenteNormal = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                contenido.setNonStrokingColor(0.96f, 0.96f, 0.95f);
                contenido.addRect(20, 20, 572, 752);
                contenido.fill();

                contenido.setNonStrokingColor(1.0f, 1.0f, 1.0f);
                contenido.addRect(40, 40, 532, 712);
                contenido.fill();

                contenido.setNonStrokingColor(0.17f, 0.22f, 0.20f);
                contenido.addRect(40, 742, 532, 10);
                contenido.fill();

                escribirTexto(contenido, fuenteBold, 22, 0.10f, 0.10f, 0.10f, 60, 700,
                        "CERTIFICADO DE IMPACTO B");
                escribirTexto(contenido, fuenteNormal, 10, 0.54f, 0.17f, 0.09f, 60, 683,
                        "SISTEMA DE TRAZABILIDAD Y AUDITORIA SOCIAL - 14-DIEZ S.A.C. (HOSEG)");

                escribirTexto(contenido, fuenteNormal, 12, 0.19f, 0.19f, 0.19f, 60, 630,
                        "Por la presente, 14-DIEZ S.A.C. (RUC 20566428386) deja constancia auditada a favor de:");
                escribirTexto(contenido, fuenteBold, 15, 0.17f, 0.22f, 0.20f, 60, 602,
                        limpiarAscii(ongsAliadas.toUpperCase()));

                int y = 562;
                String[] parrafos = {
                        "Como respaldo verificable del impacto social y ambiental ejecutado en las comunidades",
                        "altoandinas durante el periodo comprendido del " + periodoTexto + ", derivado de",
                        "las operaciones comerciales confirmadas en el sistema SEGITD-HOSEG bajo el modelo",
                        "de triple impacto (abrigo comunitario y reforestacion de cedros andinos)."
                };
                for (String linea : parrafos) {
                    escribirTexto(contenido, fuenteNormal, 11, 0.33f, 0.33f, 0.33f, 60, y, linea);
                    y -= 18;
                }

                contenido.setNonStrokingColor(0.97f, 0.97f, 0.98f);
                contenido.addRect(60, 290, 492, 175);
                contenido.fill();

                escribirTexto(contenido, fuenteBold, 12, 0.10f, 0.10f, 0.10f, 80, 442,
                        "METRICAS AUDITADAS DE TRIPLE IMPACTO (PERIODO " + periodoTexto + "):");

                escribirTexto(contenido, fuenteNormal, 11, 0.15f, 0.15f, 0.15f, 80, 416,
                        "- Alcance comunitario: " + limpiarAscii(alcanceComunidad));
                escribirTexto(contenido, fuenteNormal, 11, 0.15f, 0.15f, 0.15f, 80, 396,
                        "- Lotes de donacion entregados y validados: " + resumen.lotesEntregados() + " lote(s)");
                escribirTexto(contenido, fuenteNormal, 11, 0.15f, 0.15f, 0.15f, 80, 376,
                        "- Prendas de abrigo entregadas en campo: " + resumen.prendasDonadas() + " unidad(es)");
                escribirTexto(contenido, fuenteNormal, 11, 0.15f, 0.15f, 0.15f, 80, 356,
                        "- Compromisos de reforestacion (cedros andinos): " + resumen.arbolesPlantados() + " arbol(es)");
                escribirTexto(contenido, fuenteNormal, 11, 0.15f, 0.15f, 0.15f, 80, 336,
                        "- Pedidos web confirmados que financiaron el impacto: "
                                + resumen.ventas().pedidosConfirmados() + " pedido(s) (S/ "
                                + resumen.ventas().montoVendido() + ")");
                escribirTexto(contenido, fuenteNormal, 11, 0.15f, 0.15f, 0.15f, 80, 316,
                        "- Estado global de donaciones del periodo: " + resumen.donacionesEntregadas()
                                + " entregadas / " + resumen.donacionesPendientes() + " pendientes");

                escribirTexto(contenido, fuenteNormal, 10, 0.45f, 0.45f, 0.45f, 60, 150,
                        "Fecha de emision: " + fechaEmision);
                escribirTexto(contenido, fuenteNormal, 10, 0.45f, 0.45f, 0.45f, 60, 135,
                        "Codigo unico de auditoria: " + codigoValidacion);

                String textoQR = "CERTIFICADO TRIPLE IMPACTO HOSEG\n"
                        + "Codigo: " + codigoValidacion + "\n"
                        + "Periodo: " + periodoTexto + "\n"
                        + "Lotes entregados: " + resumen.lotesEntregados() + "\n"
                        + "Prendas abrigo: " + resumen.prendasDonadas() + "\n"
                        + "Arboles: " + resumen.arbolesPlantados();

                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(textoQR, BarcodeFormat.QR_CODE, 120, 120);
                int ancho = bitMatrix.getWidth();
                BufferedImage imagen = new BufferedImage(ancho, ancho, BufferedImage.TYPE_INT_RGB);
                for (int xi = 0; xi < ancho; xi++) {
                    for (int yi = 0; yi < ancho; yi++) {
                        imagen.setRGB(xi, yi, bitMatrix.get(xi, yi) ? 0x000000 : 0xFFFFFF);
                    }
                }
                PDImageXObject imagenQR = JPEGFactory.createFromImage(documento, imagen);
                contenido.drawImage(imagenQR, 430, 80, 120, 120);
            }

            File archivo = new File(rutaArchivo);
            archivo.getParentFile().mkdirs();
            documento.save(archivo);
            return archivo;
        } catch (Exception e) {
            throw new ServicioException("No se pudo generar el certificado PDF: " + e.getMessage(), e);
        }
    }

    private void escribirTexto(PDPageContentStream contenido, PDFont fuente, float tamano,
                               float r, float g, float b, float x, float y, String texto) throws Exception {
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.setNonStrokingColor(r, g, b);
        contenido.newLineAtOffset(x, y);
        contenido.showText(limpiarAscii(texto));
        contenido.endText();
    }

    private String limpiarAscii(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N").replace("ė", "e").replace("ö", "o").replace("Ö", "O");
    }
}
