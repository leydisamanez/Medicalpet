package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Comprobante;
import com.medicalpet.medicalpet.model.DetalleVenta;
import com.medicalpet.medicalpet.model.Venta;
import com.medicalpet.medicalpet.model.Producto;
import com.medicalpet.medicalpet.model.Cliente;
import com.medicalpet.medicalpet.service.ClienteService;
import com.medicalpet.medicalpet.service.ProductoService;
import com.medicalpet.medicalpet.service.VentaService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.NonUniqueResultException;

@Controller
@RequestMapping("/ventas")
public class VentaController {
    private static final Logger logger = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private JavaMailSender mailSender;

    public static class VentaDTO {
        private Long id;
        private String clienteNombre;
        private String fechaFormateada;
        private double total;
        private List<String> productoNombres;

        public VentaDTO(Venta venta) {
            this.id = venta.getId();
            this.clienteNombre = venta.getCliente() != null ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() : "N/A";
            this.fechaFormateada = venta.getFecha() != null ? venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
            this.total = venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum();
            this.productoNombres = venta.getDetalles() != null && !venta.getDetalles().isEmpty() ?
                venta.getDetalles().stream()
                    .map(detalle -> detalle.getProducto().getNombre())
                    .collect(Collectors.toList()) : new ArrayList<>();
        }

        public Long getId() { return id; }
        public String getClienteNombre() { return clienteNombre; }
        public String getFechaFormateada() { return fechaFormateada; }
        public double getTotal() { return total; }
        public List<String> getProductoNombres() { return productoNombres; }
    }

    @GetMapping("/listar")
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha,desc") String sort,
            @RequestParam(defaultValue = "") String keyword,
            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == "anonymousUser") {
            logger.warn("⚠️ Usuario no autenticado o anónimo al acceder a /ventas/listar");
            return "redirect:/login";
        }
        logger.info("ℹ️ Usuario autenticado: {}", auth.getName());
        try {
            String[] sortParams = sort.split(",");
            Sort sortOrder = Sort.by(sortParams[0]);
            if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
                sortOrder = sortOrder.descending();
            } else {
                sortOrder = sortOrder.ascending();
            }
            Pageable pageable = PageRequest.of(page, size, sortOrder);
            Page<Venta> ventasPage = ventaService.searchVentas(keyword, pageable);
            List<VentaDTO> ventasDTO = new ArrayList<>();
            if (ventasPage == null || ventasPage.getContent().isEmpty()) {
                logger.info("ℹ️ No se encontraron ventas para la página {} con keyword '{}'", page, keyword);
                model.addAttribute("ventas", ventasDTO);
                model.addAttribute("message", "No se encontraron ventas.");
            } else {
                ventasDTO = ventasPage.getContent().stream()
                        .map(VentaDTO::new)
                        .collect(Collectors.toList());
                logger.info("✅ Se cargaron {} ventas para la página {}", ventasDTO.size(), page);
                model.addAttribute("ventas", ventasDTO);
            }
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ventasPage != null ? ventasPage.getTotalPages() : 0);
            model.addAttribute("pageSize", size);
            model.addAttribute("sort", sort);
            model.addAttribute("keyword", keyword);
            model.addAttribute("sortId", "id," + (sort.startsWith("id") ? (sort.endsWith("asc") ? "desc" : "asc") : "desc"));
            model.addAttribute("sortCliente", "cliente_id," + (sort.startsWith("cliente_id") ? (sort.endsWith("asc") ? "desc" : "asc") : "desc"));
            model.addAttribute("sortFecha", "fecha," + (sort.startsWith("fecha") ? (sort.endsWith("asc") ? "desc" : "asc") : "desc"));
            model.addAttribute("sortTotal", "total," + (sort.startsWith("total") ? (sort.endsWith("asc") ? "desc" : "asc") : "desc"));
        } catch (Exception e) {
            logger.error("😱 Error al cargar las ventas: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar las ventas: " + e.getMessage());
            return "redirect:/ventas/error?message=" + e.getMessage();
        }
        return "ventas/listar";
    }

    @GetMapping("/nueva")
    public String nuevaVentaForm(Model model) {
        try {
            Venta venta = new Venta();
            model.addAttribute("venta", venta);
            List<Cliente> clientes = clienteService.findAll() != null ? new ArrayList<>(clienteService.findAll()) : new ArrayList<>();
            List<Producto> productos = productoService.findAll() != null ? productoService.findAll() : new ArrayList<>();
            model.addAttribute("clientes", clientes);
            model.addAttribute("productos", productos);
            model.addAttribute("detalles", new ArrayList<DetalleVenta>());
            if (clientes.isEmpty()) {
                logger.warn("⚠️ No hay clientes disponibles para nueva venta");
                model.addAttribute("warning", "No hay clientes disponibles. Agregue clientes primero.");
            }
            if (productos.isEmpty()) {
                logger.warn("⚠️ No hay productos disponibles para nueva venta");
                model.addAttribute("warning", "No hay productos disponibles. Agregue productos primero.");
            }
            logger.info("ℹ️ Form installation de nueva venta cargado con {} clientes y {} productos", clientes.size(), productos.size());
            return "ventas/form";
        } catch (Exception e) {
            logger.error("😱 Error al cargar los datos para nueva venta: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los datos para nueva venta: " + e.getMessage());
            return "ventas/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarVentaForm(@PathVariable Long id, Model model) {
        try {
            Venta venta = ventaService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
            logger.info("ℹ️ Carga de venta con ID {} para edición", id);
            model.addAttribute("venta", venta);
            List<Cliente> clientes = clienteService.findAll() != null ? new ArrayList<>(clienteService.findAll()) : new ArrayList<>();
            List<Producto> productos = productoService.findAll() != null ? productoService.findAll() : new ArrayList<>();
            model.addAttribute("clientes", clientes);
            model.addAttribute("productos", productos);
            model.addAttribute("detalles", venta.getDetalles() != null ? venta.getDetalles() : new ArrayList<>());
        } catch (Exception e) {
            logger.error("😱 Error al cargar los datos para edición de venta con ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
            return "ventas/form";
        }
        return "ventas/form";
    }

    @PostMapping("/guardar")
    public String guardarVenta(
            @ModelAttribute("venta") Venta venta,
            @RequestParam("clienteId") Long clienteId,
            @RequestParam("tipoComprobante") String tipoComprobante,
            @RequestParam(value = "ruc", required = false) String ruc,
            @RequestParam(value = "razonSocial", required = false) String razonSocial,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "descuento", required = false, defaultValue = "0.0") double descuento,
            @RequestParam(value = "detalleProductoId", required = false) Long[] detalleProductIds,
            @RequestParam(value = "detalleCantidad", required = false) Integer[] detalleCantidades,
            Model model) {
        try {
            logger.info("ℹ️ Iniciando guardado de venta. ID de venta recibido: {}, ClienteID: {}", venta.getId(), clienteId);

            // Forzar nuevo registro si es una creación
            if (venta.getId() == null) {
                venta.setId(null);
            }

            // Asignar el cliente
            Cliente cliente = clienteService.findById(clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));
            venta.setCliente(cliente);
            logger.info("ℹ️ Cliente asignado: {} {}", cliente.getNombre(), cliente.getApellido());

            if (venta.getFecha() == null) {
                venta.setFecha(LocalDate.now());
            }

            // Validar detalles de la venta
            List<DetalleVenta> detalles = new ArrayList<>();
            if (detalleProductIds != null && detalleCantidades != null && detalleProductIds.length > 0 && detalleProductIds.length == detalleCantidades.length) {
                for (int index = 0; index < detalleProductIds.length; index++) {
                    Long productId = detalleProductIds[index];
                    Integer cantidad = detalleCantidades[index];
                    if (cantidad == null || cantidad <= 0) {
                        throw new IllegalArgumentException("La cantidad para el producto con ID " + productId + " no es válida.");
                    }
                    Producto producto = productoService.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));
                    if (producto.getStock() < cantidad) {
                        throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                    }
                    double precioUnitario = producto.getPrecio();
                    double subtotal = precioUnitario * cantidad;
                    DetalleVenta detalle = new DetalleVenta(venta, producto, cantidad, precioUnitario, subtotal);
                    detalles.add(detalle);
                    logger.info("ℹ️ Detalle agregado: Producto {}, Cantidad: {}", producto.getNombre(), cantidad);
                }
            } else {
                logger.warn("⚠️ No se proporcionaron detalles de productos o cantidades válidas");
                throw new IllegalArgumentException("Los detalles de productos y cantidades no son válidos.");
            }
            venta.setDetalles(detalles);

            // Crear o actualizar el comprobante
            Comprobante comprobante = venta.getComprobante() != null ? venta.getComprobante() : new Comprobante();
            comprobante.setVenta(venta);
            comprobante.setTipoComprobante(Comprobante.TipoComprobante.valueOf(tipoComprobante.toUpperCase()));
            double base = venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum();
            comprobante.setSubtotal(base);
            comprobante.setDescuento(descuento);
            if (comprobante.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA) {
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

            // Guardar la venta completa con detalles y comprobante
            Venta savedVenta = ventaService.save(venta, comprobante.getTipoComprobante(), ruc, razonSocial, direccion, descuento);
            logger.info("✅ Venta guardada exitosamente con ID: {}", savedVenta.getId());

            // Generar PDF y enviar correo (fuera de la transacción)
            String pdfPath = generatePdf(savedVenta, tipoComprobante);
            sendEmail(savedVenta.getCliente().getEmail(), "Comprobante de Venta - MedicalPet", "Adjunto encontrará su " + tipoComprobante.toLowerCase() + ".", pdfPath);

            return "redirect:/ventas/listar";
        } catch (Exception e) {
            logger.error("😱 Error al guardar la venta: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al guardar la venta: " + e.getMessage());
            model.addAttribute("venta", venta);
            model.addAttribute("clientes", clienteService.findAll());
            model.addAttribute("productos", productoService.findAll());
            model.addAttribute("detalles", venta.getDetalles() != null ? venta.getDetalles() : new ArrayList<>());
            return "ventas/form";
        }
    }

    @GetMapping("/generar-pdf/{id}")
    public void generarPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        try {
            Venta venta = ventaService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
            logger.info("ℹ️ Generando PDF para venta con ID: {}", id);
            String tipoComprobante = venta.getComprobante() != null ? venta.getComprobante().getTipoComprobante().name() : "BOLETA";
            String pdfPath = generatePdf(venta, tipoComprobante);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + tipoComprobante.toLowerCase() + "_" + venta.getId() + ".pdf");
            FileSystemResource file = new FileSystemResource(new File(pdfPath));
            file.getInputStream().transferTo(response.getOutputStream());
            logger.info("✅ PDF generado y enviado para venta con ID: {}", id);
        } catch (Exception e) {
            logger.error("😱 Error al generar el PDF para venta con ID {}: {}", id, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el PDF: " + e.getMessage());
        }
    }

    private String generatePdf(Venta venta, String tipoComprobante) throws IOException {
        String outputPath = "src/main/resources/static/pdf/" + tipoComprobante.toLowerCase() + "_" + venta.getId() + ".pdf";

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Encabezado - Identidad de MedicalPet
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

            // Línea divisoria
            contentStream.setStrokingColor(0, 128, 0);
            contentStream.setLineWidth(1);
            contentStream.moveTo(80, 700);
            contentStream.lineTo(520, 700);
            contentStream.stroke();

            // Tabla Dirección
            float y = 670;
            drawTable(contentStream, 100, y, 400, 50, new String[][]{
                {"DIRECCIÓN DE PARTIDA", "DIRECCIÓN DE DESTINO"},
                {cleanText("Av. Veterinaria 123, Lima - Lima - Perú"), cleanText(venta.getCliente().getDireccion() != null ? venta.getCliente().getDireccion() : "N/A")}
            }, new float[]{200, 200});

            y -= 70;

            // Tabla Datos de la Mercadería y Destinatario
            Comprobante comp = venta.getComprobante();
            drawTable(contentStream, 100, y, 400, 80, new String[][]{
                {"DATOS DE LA MERCADERÍA", "DESTINATARIO"},
                {"FECHA DE EMISIÓN: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "RAZÓN SOCIAL: " + cleanText(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido())},
                {"FECHA DE TRASLADO: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "RUC: " + cleanText(comp.getRuc() != null ? comp.getRuc() : "N/A") : ""}
            }, new float[]{200, 200});

            y -= 70;

            // Tabla Unidad de Transporte
            drawTable(contentStream, 100, y, 400, 50, new String[][]{
                {"UNIDAD DE TRANSPORTE"},
                {"MARCA Y N° PLACA: N/A", "CONDUCTOR: N/A - DNI: N/A"}
            }, new float[]{400});

            y -= 70;

            // Tabla de Mercadería
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

            // Tabla Resumen
            drawTable(contentStream, 100, y, 400, 100, new String[][]{
                {"RESUMEN"},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "RUC Cliente: " + cleanText(comp.getRuc() != null ? comp.getRuc() : "N/A") : ""},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "Razón Social: " + cleanText(comp.getRazonSocial() != null ? comp.getRazonSocial() : "N/A") : ""},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "Dirección: " + cleanText(comp.getDireccion() != null ? comp.getDireccion() : "N/A") : ""},
                {comp != null && comp.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA ? "IGV: " + String.format("%.2f", comp.getIgv()) : ""},
                {"Descuento: " + String.format("%.2f", comp != null ? comp.getDescuento() : 0.0)},
                {"Total: " + String.format("%.2f", comp != null ? comp.getTotal() : comp != null ? comp.getSubtotal() : 0.0)}
            }, new float[]{400});

            // Pie de página
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
            logger.info("✅ PDF generado en: {}", outputPath);
        } catch (IOException e) {
            logger.error("😱 Error al generar el PDF para venta con ID {}: {}", venta.getId(), e.getMessage(), e);
            throw e;
        }

        return outputPath;
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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(file.getFilename(), file);
            mailSender.send(message);
            logger.info("✅ Correo enviado a {} con adjunto {}", to, file.getFilename());
        } catch (MessagingException e) {
            logger.error("😱 Error al enviar correo a {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarVenta(@PathVariable Long id, Model model) {
        try {
            ventaService.deleteById(id);
            logger.info("✅ Venta con ID {} eliminada exitosamente", id);
        } catch (Exception e) {
            logger.error("😱 Error al eliminar la venta con ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("error", "Error al eliminar la venta: " + e.getMessage());
            return "redirect:/ventas/listar?error=" + e.getMessage();
        }
        return "redirect:/ventas/listar";
    }

    @GetMapping("/exportar")
    public void exportarVentas(HttpServletResponse response, @RequestParam(defaultValue = "") String keyword) throws IOException {
        try {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=ventas.csv");
            List<Venta> ventas = ventaService.searchVentas(keyword, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Cliente,Fecha,Total\n");
            for (Venta venta : ventas) {
                csv.append(venta.getId()).append(",")
                   .append(venta.getCliente() != null ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() : "N/A").append(",")
                   .append(venta.getFecha() != null ? venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A").append(",")
                   .append(venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum()).append("\n");
            }
            response.getWriter().write(csv.toString());
            logger.info("✅ Exportación CSV generada con {} ventas", ventas.size());
        } catch (Exception e) {
            logger.error("😱 Error al exportar ventas con keyword '{}': {}", keyword, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al exportar: " + e.getMessage());
        }
    }

    @GetMapping("/error")
    public String error(Model model, @RequestParam(value = "message", required = false) String message) {
        logger.warn("⚠️ Redirigiendo a página de error con mensaje: {}", message);
        if (message == null) {
            message = "Ocurrió un error inesperado. Por favor, contacte al administrador.";
        }
        model.addAttribute("error", message);
        return "error";
    }

    @ExceptionHandler(NonUniqueResultException.class)
    public String handleNonUniqueResultException(NonUniqueResultException ex, Model model) {
        logger.error("😱 Error de duplicidad en la base de datos: {}", ex.getMessage(), ex);
        model.addAttribute("error", "Error: Se encontraron múltiples registros para un identificador único. Por favor, revise la base de datos y elimine duplicados (Comprobante con venta_id).");
        return "redirect:/ventas/error?message=" + ex.getMessage();
    }
}