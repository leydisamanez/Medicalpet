package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Comprobante;
import com.medicalpet.medicalpet.model.DetalleVenta;
import com.medicalpet.medicalpet.model.Producto;
import com.medicalpet.medicalpet.model.Venta;
import com.medicalpet.medicalpet.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public Venta saveVentaOnly(Venta venta) {
        if (venta.getCliente() == null) {
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        if (venta.getFecha() == null) {
            venta.setFecha(LocalDate.now());
        }
        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                if (producto.getStock() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                }
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoService.save(producto);
            }
        }
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta save(Venta venta, Comprobante.TipoComprobante tipoComprobante, String ruc, String razonSocial, String direccion, double descuento) {
        try {
            if (venta.getCliente() == null) {
                throw new IllegalArgumentException("Cliente no puede ser nulo");
            }
            if (venta.getFecha() == null) {
                venta.setFecha(LocalDate.now());
            }

            // Actualizar stock de productos
            if (venta.getDetalles() != null) {
                for (DetalleVenta detalle : venta.getDetalles()) {
                    Producto producto = detalle.getProducto();
                    if (producto.getStock() < detalle.getCantidad()) {
                        throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                    }
                    producto.setStock(producto.getStock() - detalle.getCantidad());
                    productoService.save(producto);
                }
            }

            // Verificar si ya existe un comprobante para esta venta
            Comprobante comprobante = venta.getComprobante();
            if (comprobante == null && venta.getId() != null) {
                comprobante = comprobanteService.findByVentaId(venta.getId()).orElse(new Comprobante());
            } else if (comprobante == null) {
                comprobante = new Comprobante();
            }

            comprobante.setVenta(venta);
            comprobante.setTipoComprobante(tipoComprobante);
            comprobante.setFecha(venta.getFecha());
            double base = venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum();
            comprobante.setSubtotal(base);
            comprobante.setDescuento(descuento);
            if (tipoComprobante == Comprobante.TipoComprobante.FACTURA) {
                comprobante.setRuc(ruc != null ? ruc : "N/A");
                comprobante.setRazonSocial(razonSocial != null ? razonSocial : "N/A");
                comprobante.setDireccion(direccion != null ? direccion : "N/A");
                comprobante.setIgv(base * 0.18);
                comprobante.setTotal(base + comprobante.getIgv() - descuento);
            } else {
                comprobante.setIgv(0.0);
                comprobante.setTotal(base - descuento);
            }

            venta.setComprobante(comprobante);
            return ventaRepository.save(venta);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la venta: " + e.getMessage(), e);
        }
    }

    public void generateAndSendComprobante(Venta venta, String tipoComprobante) throws IOException, MessagingException {
        String pdfPath = generatePdf(venta, tipoComprobante);
        sendEmail(venta.getCliente().getEmail(), "Comprobante de Venta - MedicalPet", "Adjunto encontrará su " + tipoComprobante.toLowerCase() + ".", pdfPath);
    }

    public Optional<Venta> findById(Long id) {
        return ventaRepository.findById(id);
    }

    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    public List<Venta> findByProductoId(Long productoId) {
        return ventaRepository.findByDetallesProductoId(productoId);
    }

    @Transactional
    public void deleteById(Long id) {
        Venta venta = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));
        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoService.save(producto);
            }
        }
        if (venta.getComprobante() != null) {
            comprobanteService.deleteById(venta.getComprobante().getId());
        }
        ventaRepository.deleteById(id);
    }

    public Page<Venta> searchVentas(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ventaRepository.findAll(pageable);
        }
        return ventaRepository.findByClienteNombreContainingIgnoreCaseOrFechaContaining(keyword, keyword, pageable);
    }

    private String generatePdf(Venta venta, String tipoComprobante) throws IOException {
        String outputPath = "src/main/resources/static/pdf/" + tipoComprobante.toLowerCase() + "_" + venta.getId() + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.setNonStrokingColor(0, 128, 0);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 750);
            contentStream.showText("MedicalPet");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.setNonStrokingColor(0, 0, 0);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 730);
            contentStream.showText("R.U.C. 12345678912");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(tipoComprobante.toUpperCase() + " Electrónica");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("N° " + String.format("%09d", venta.getId()));
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Dirección: Av. Veterinaria 123, Lima - Lima - Perú");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Telf: (01) 555-1234 | Email: info@medicalpet.com");
            contentStream.endText();

            contentStream.setStrokingColor(0, 128, 0);
            contentStream.setLineWidth(1);
            contentStream.moveTo(80, 700);
            contentStream.lineTo(520, 700);
            contentStream.stroke();

            float y = 670;
            drawTable(contentStream, 100, y, 400, 50, new String[][]{
                {"DIRECCIÓN DE PARTIDA", "DIRECCIÓN DE DESTINO"},
                {cleanText("Av. Veterinaria 123, Lima - Lima - Perú"), cleanText(venta.getCliente().getDireccion() != null ? venta.getCliente().getDireccion() : "N/A")}
            }, new float[]{200, 200});

            y -= 70;

            Comprobante comp = venta.getComprobante();
            drawTable(contentStream, 100, y, 400, 80, new String[][]{
                {"DATOS DE LA MERCADERÍA", "DESTINATARIO"},
                {"FECHA DE EMISIÓN: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "RAZÓN SOCIAL: " + cleanText(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido())},
                {"FECHA DE TRASLADO: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "RUC: " + cleanText(comp.getRuc() != null ? comp.getRuc() : "N/A") : ""}
            }, new float[]{200, 200});

            y -= 70;

            drawTable(contentStream, 100, y, 400, 50, new String[][]{
                {"UNIDAD DE TRANSPORTE"},
                {"MARCA Y N° PLACA: N/A", "CONDUCTOR: N/A - DNI: N/A"}
            }, new float[]{400});

            y -= 70;

            float tableTop = y;
            float tableLeft = 100;
            float tableRight = 500;
            float rowHeight = 20;
            float cellMargin = 5;

            String[] headers = {"CANT.", "DESCRIPCIÓN", "PRECIO UNITARIO (S/)", "SUBTOTAL (S/)"};
            float[] columnWidths = {70, 230, 100, 100};

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.setNonStrokingColor(255, 255, 255);
            contentStream.beginText();
            contentStream.newLineAtOffset(tableLeft, tableTop);
            for (int i = 0; i < headers.length; i++) {
                contentStream.showText(headers[i]);
                contentStream.newLineAtOffset(columnWidths[i], 0);
            }
            contentStream.endText();

            contentStream.setNonStrokingColor(0, 128, 0);
            contentStream.addRect(tableLeft - 5, tableTop - rowHeight + 5, tableRight - tableLeft + 10, rowHeight);
            contentStream.fill();

            contentStream.setStrokingColor(0, 0, 0);
            contentStream.moveTo(tableLeft, tableTop - rowHeight);
            contentStream.lineTo(tableRight, tableTop - rowHeight);
            contentStream.stroke();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.setNonStrokingColor(0, 0, 0);
            float yData = tableTop - rowHeight - cellMargin;
            for (DetalleVenta detalle : venta.getDetalles()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(tableLeft, yData);
                String[] data = {
                    String.valueOf(detalle.getCantidad()),
                    cleanText(detalle.getProducto().getNombre()),
                    String.format("%.2f", detalle.getPrecioUnitario()),
                    String.format("%.2f", detalle.getSubtotal())
                };
                for (int i = 0; i < data.length; i++) {
                    contentStream.showText(data[i]);
                    contentStream.newLineAtOffset(columnWidths[i], 0);
                }
                contentStream.endText();
                yData -= rowHeight;
            }

            y = yData - 40;

            drawTable(contentStream, 100, y, 400, 100, new String[][]{
                {"RESUMEN"},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "RUC Cliente: " + cleanText(comp.getRuc() != null ? comp.getRuc() : "N/A") : ""},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "Razón Social: " + cleanText(comp.getRazonSocial() != null ? comp.getRazonSocial() : "N/A") : ""},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "Dirección: " + cleanText(comp.getDireccion() != null ? comp.getDireccion() : "N/A") : ""},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "IGV: " + String.format("%.2f", comp.getIgv()) : ""},
                {"Descuento: " + String.format("%.2f", comp != null ? comp.getDescuento() : 0.0)},
                {"Total: " + String.format("%.2f", comp != null ? comp.getTotal() : comp != null ? comp.getSubtotal() : 0.0)}
            }, new float[]{400});

            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.setNonStrokingColor(128, 128, 128);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 50);
            LocalDateTime now = LocalDateTime.now();
            String dateTime = now.format(DateTimeFormatter.ofPattern("hh:mm a 'on' EEEE, MMMM dd, yyyy"));
            contentStream.showText("Representación impresa de " + tipoComprobante.toUpperCase() + " | Generado el: " + dateTime + " | MedicalPet © 2025");
            contentStream.endText();

            contentStream.setStrokingColor(0, 128, 0);
            contentStream.setLineWidth(2);
            contentStream.addRect(80, 40, 440, 770);
            contentStream.stroke();

            contentStream.close();

            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            document.save(outputPath);
            return outputPath;
        }
    }

    private String cleanText(String text) {
        if (text == null) return "N/A";
        return text.replaceAll("[\\r\\n\\t]", " ")
                   .replaceAll("[^\\x20-\\x7E]", "")
                   .trim();
    }

    private void drawTable(PDPageContentStream contentStream, float x, float y, float width, float height, String[][] data, float[] columnWidths) throws IOException {
        contentStream.setStrokingColor(0, 0, 0);
        contentStream.setLineWidth(1);
        float rowHeight = height / data.length;
        float tableRight = x + width;

        contentStream.addRect(x, y - height, width, height);
        contentStream.stroke();

        float nextX = x;
        for (int i = 0; i < columnWidths.length; i++) {
            contentStream.moveTo(nextX, y - height);
            contentStream.lineTo(nextX, y);
            contentStream.stroke();
            nextX += columnWidths[i];
        }
        for (int i = 0; i <= data.length; i++) {
            float lineY = y - (i * rowHeight);
            contentStream.moveTo(x, lineY);
            contentStream.lineTo(tableRight, lineY);
            contentStream.stroke();
        }

        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.setNonStrokingColor(0, 0, 0);
        for (int i = 0; i < data.length; i++) {
            float textY = y - (i * rowHeight) - (rowHeight / 2) + 5;
            contentStream.beginText();
            contentStream.newLineAtOffset(x + 5, textY);
            for (int j = 0; j < data[i].length; j++) {
                contentStream.showText(cleanText(data[i][j]));
                if (j < data[i].length - 1) {
                    contentStream.newLineAtOffset(columnWidths[j], 0);
                }
            }
            contentStream.endText();
        }
    }

    private void sendEmail(String to, String subject, String body, String attachmentPath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        FileSystemResource file = new FileSystemResource(new File(attachmentPath));
        helper.addAttachment(file.getFilename(), file);
        mailSender.send(message);
    }
}