package vitalsanity.service.utils.autofirma;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vitalsanity.model.CentroMedico;
import vitalsanity.model.Paciente;
import vitalsanity.model.ProfesionalMedico;
import vitalsanity.model.Usuario;
import vitalsanity.repository.PacienteRepository;
import vitalsanity.repository.ProfesionalMedicoRepository;
import vitalsanity.repository.UsuarioRepository;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
public class GenerarPdf {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesionalMedicoRepository profesionalMedicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public byte[] generarPdfAutorizacion(
            Long idUsuarioProfesionalMedico,
            Long idUsuarioPaciente,
            String motivo,
            String descripcion) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Recuperar datos dinámicos
            Usuario usuarioProfesional = usuarioRepository.findById(idUsuarioProfesionalMedico).orElseThrow();
            Usuario usuarioPaciente    = usuarioRepository.findById(idUsuarioPaciente).orElseThrow();
            ProfesionalMedico profesional = profesionalMedicoRepository
                    .findByUsuarioId(idUsuarioProfesionalMedico).orElseThrow();
            Paciente paciente = pacienteRepository
                    .findByUsuarioId(idUsuarioPaciente).orElseThrow();
            CentroMedico centro = profesional.getCentroMedico();
            Hibernate.initialize(centro);
            Hibernate.initialize(centro.getUsuario());

            // Fuentes para texto normal y negrita
            PdfFont font  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Crear documento PDF
            PdfWriter  writer  = new PdfWriter(baos);
            PdfDocument pdfDoc  = new PdfDocument(writer);
            Document    document = new Document(pdfDoc);
            document.setFont(font);

            // Tras crear PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event event) {
                    PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                    PdfPage page = docEvent.getPage();
                    int pageNumber = docEvent.getDocument().getPageNumber(page);
                    Rectangle pageSize = page.getPageSize();
                    PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), docEvent.getDocument());
                    // Aquí usamos sólo pdfCanvas y pageSize
                    Canvas canvas = new Canvas(pdfCanvas, pageSize);
                    canvas.setFont(font)
                            .setFontSize(10)
                            .showTextAligned(
                                    String.valueOf(pageNumber),
                                    pageSize.getRight() - 36,
                                    pageSize.getBottom() + 15,
                                    TextAlignment.RIGHT
                            );
                    canvas.close();
                }
            });

            // Línea de encabezado: ubicación y fecha en texto
            LocalDate today = LocalDate.now();
            String monthName = today.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("es","ES"))
                    .toLowerCase();
            String header = String.format("%s (%s), a %d de %s de %d.",
                    centro.getUsuario().getMunicipio(),
                    centro.getUsuario().getProvincia(),
                    today.getDayOfMonth(),
                    monthName,
                    today.getYear());
            document.add(new Paragraph(header)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10)
            );
            // Título principal
            document.add(new Paragraph("AUTORIZACIÓN DE ACCESO AL HISTORIAL MÉDICO CENTRALIZADO")
                    .setFont(bold)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15)
            );

            // 1. Introducción
            document.add(new Paragraph("1. Introducción")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "A continuación, se presenta el texto completo de la autorización de acceso al " +
                            "historial médico centralizado, incluyendo todos los datos proporcionados, " +
                            "estructurado conforme a los requisitos de información y consentimiento " +
                            "recogidos en el Reglamento General de Protección de Datos (RGPD) y en la Ley " +
                            "Orgánica 3/2018 de Protección de Datos Personales y garantía de los derechos " +
                            "digitales (LOPDGDD). Se detallan las partes intervinientes, la finalidad del " +
                            "tratamiento, la base jurídica, el alcance de los derechos de ambas partes intervinientes, el " +
                            "mecanismo de revocación, la identificación del responsable del tratamiento, " +
                            "así como las firmas electrónica y cofirma respectivamente mediante AutoFirma y su versión móvil (la aplicación de Cliente móvil @firma).")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            // 2. Identificación de las partes
            document.add(new Paragraph("2. Identificación de las partes")
                    .setFont(bold)
                    .setFontSize(12)
            );
            // Profesional médico
            document.add(new Paragraph("Profesional médico")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioProfesional.getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nº Del Documento de Identificación: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioProfesional.getNifNie()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Especialidad: ").setFont(bold).setFontSize(10))
                    .add(new Text(profesional.getEspecialidadMedica().getNombre()).setFont(font).setFontSize(10))
                    .setMarginBottom(5)
            );
            // Centro médico asociado
            document.add(new Paragraph("Centro médico asociado")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("NIF: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getNifNie()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Provincia: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getProvincia()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Municipio: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getMunicipio()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Dirección: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getDireccion()).setFont(font).setFontSize(10))
                    .setMarginBottom(5)
            );
            // Paciente
            document.add(new Paragraph("Paciente")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioPaciente.getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nº Del Documento de Identificación: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioPaciente.getNifNie()).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 3. Objeto y Finalidad de la autorización
            document.add(new Paragraph("3. Objeto y Finalidad de la autorización")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "Mediante el presente documento, por una parte, el profesional médico identificado " +
                            "en el apartado 1 solicita autorización al historial clínico centralizado del paciente. " +
                            "Por otra parte, el paciente identificado en el apartado 1, otorga su consentimiento " +
                            "libre, específico, informado e inequívoco al profesional identificado en el apartado 1 " +
                            "y al centro sanitario al que presta servicios para acceder íntegramente a su historial " +
                            "clínico centralizado en la plataforma VitalSanity, con la única finalidad de posibilitar " +
                            "la adecuada prestación de atención sanitaria, de conformidad con el artículo 9.2.a) RGPD " +
                            "que permite el tratamiento de categorías especiales de datos previo consentimiento explícito del interesado.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "El acceso comprenderá todos los informes médicos y la documentación clínica asociada " +
                            "que obren en el repositorio, incluidos los informes futuros que se incorporen mientras " +
                            "la presente autorización permanezca vigente.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Tras la firma de esta autorización tanto por parte del profesional médico como por parte " +
                            "del paciente, el profesional médico en cuestión tendrá acceso a todo el historial médico " +
                            "centralizado del paciente, lo cual implicará el acceso a todos los informes y documentos " +
                            "médicos del paciente, incluyendo informes y documentos creados por otros profesionales médicos.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Tras haberse efectuado la autorización, el paciente podrá en cualquier momento desautorizar " +
                            "al profesional médico, acción que implicará la pérdida del acceso al historial centralizado " +
                            "por parte del profesional en cuestión. No obstante, la acción de desautorización no supondrá " +
                            "la denegación al acceso a informes y documentos propios creados con anterioridad, de modo que " +
                            "el profesional médico seguirá teniendo acceso a estos informes y documentos que el mismo hubiese agregado al historial del paciente con fecha anterior a la desautorización.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            // 4. Responsable y Encargado del tratamiento
            document.add(new Paragraph("4. Responsable y Encargado del tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Responsable: ").setFont(bold).setFontSize(10))
                    .add(new Text("Centro Médico “" + centro.getUsuario().getNombreCompleto()
                            + "” (NIF " + centro.getUsuario().getNifNie() + ").").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Encargado: ").setFont(bold).setFontSize(10))
                    .add(new Text("VitalSanity S.A. (NIF A79667150), Calle Innovación, 123, nº 10, 28001 Madrid, España, correo: vital@sanity.es.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 5. Información específica de la autorización
            document.add(new Paragraph("5. Información específica de la autorización")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Motivo de la solicitud: ").setFont(bold).setFontSize(10))
                    .add(new Text(motivo).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Descripción complementaria: ").setFont(bold).setFontSize(10))
                    .add(new Text(descripcion).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 6. Base Jurídica del Tratamiento
            document.add(new Paragraph("6. Base Jurídica del Tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Consentimiento informado: ").setFont(bold).setFontSize(10))
                    .add(new Text("La legitimación para el tratamiento se basa en el consentimiento libre, específico, informado e inequívoco del paciente, tal como exige el artículo 6 de la LOPDGDD y el artículo 6.1.a) del RGPD.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Datos de salud (categoría especial): ").setFont(bold).setFontSize(10))
                    .add(new Text("Se trata de datos relativos a la salud, cuya recogida y tratamiento requieren consentimiento explícito conforme al artículo 9.2.a) del RGPD.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Obligación de información: ").setFont(bold).setFontSize(10))
                    .add(new Text("El responsable ha facilitado a la paciente toda la información prevista en el artículo 13 del RGPD en el momento de la obtención de sus datos.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10)
                    )
                    .add(new Text("Deber de confidencialidad: ").setFont(bold).setFontSize(10))
                    .add(new Text("El profesional y el centro están sujetos al deber de confidencialidad establecido en el artículo 5 de la LOPDGDD y en la Ley 41/2002, que protege la confidencialidad de los datos de salud.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 7. Datos objeto de tratamiento
            document.add(new Paragraph("7. Datos objeto de tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Informes médicos").setFont(bold).setFontSize(10))
                    .add(new Text(" (con sus descripciones, observaciones, …).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Documentación clínica").setFont(bold).setFontSize(10))
                    .add(new Text(" (informe de urgencias, notas de evolución, prescripciones, …).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Cualquier dato de salud recabado").setFont(bold).setFontSize(10))
                    .add(new Text(" en el historial centralizado.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 8. Principios aplicables
            document.add(new Paragraph("8. Principios aplicables")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El tratamiento de los datos se regirá por los principios de licitud, lealtad, transparencia, limitación de la finalidad, minimización de datos, exactitud, limitación del plazo de conservación, integridad y confidencialidad establecidos en el artículo 5 del RGPD, así como por las garantías adicionales recogidas en la LOPDGDD.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            // 9. Derechos de las partes involucradas
            document.add(new Paragraph("9. Derechos de las partes involucradas")
                    .setFont(bold)
                    .setFontSize(12)
            );
            String[] derechos = {
                    "Acceso a sus datos y a la información relacionada (art. 15 RGPD).",
                    "Rectificación de datos inexactos (art. 16 RGPD).",
                    "Supresión de datos («derecho al olvido», art. 17 RGPD).",
                    "Limitación del tratamiento (art. 18 RGPD).",
                    "Portabilidad de los datos (art. 20 RGPD).",
                    "Oposición al tratamiento (art. 21 RGPD).",
                    "Revocación del consentimiento en cualquier momento, sin afectar la licitud del tratamiento previo (art. 7.3 RGPD).",
                    "Derecho a no ser objeto de decisiones automatizadas."
            };
            for (String r : derechos) {
                document.add(new Paragraph("• " + r)
                        .setMarginLeft(10)
                        .setFont(font)
                        .setFontSize(10)
                );
            }
            document.add(new Paragraph(
                    "Podrá dirigir sus solicitudes al Responsable del Tratamiento, quien facilitará su ejercicio en un plazo máximo de un mes, prorrogable por otros dos meses en casos de especial complejidad,")
                    .setMarginLeft(10)
                    .setMarginBottom(5)
                    .setFont(font)
                    .setFontSize(10)
            );
            // Medios de ejercicio
            String centroEmailName = centro.getUsuario().getNombreCompleto()
                    .toLowerCase()
                    .replace(" ", "-");
            document.add(new Paragraph("• Puede contactar con el Delegado de Protección de Datos en delegadopd@"
                    + centroEmailName + ".es.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede contactar con el encargado en vital@sanity.es.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede presentarse presencialmente en el domicilio social de las entidades.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede presentar reclamación ante la AEPD.")
                    .setMarginLeft(10)
                    .setMarginBottom(10)
                    .setFont(font)
                    .setFontSize(10)
            );

            // 10. Revocación y Plazo de Conservación
            document.add(new Paragraph("10. Revocación y Plazo de Conservación")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El paciente podrá revocar libremente su consentimiento en cualquier momento, sin que ello afecte la licitud del tratamiento realizado con anterioridad a la revocación, conforme al artículo 7.3 del RGPD, que exige que la retirada del consentimiento sea tan sencilla como su otorgamiento.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Los datos se conservarán únicamente durante el tiempo necesario para cumplir las finalidades sanitarias y para atender las obligaciones legales y de custodia establecidas en la normativa sectorial.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(10)
            );

            // 11. Medidas de seguridad
            document.add(new Paragraph("11. Medidas de seguridad")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El responsable del tratamiento garantiza la aplicación de las medidas técnicas y organizativas apropiadas para proteger los datos personales frente a destrucción, pérdida, alteración, divulgación o acceso no autorizado, conforme al artículo 32 del RGPD y a las directrices establecidas por la Agencia Española de Protección de Datos.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(10)
            );

            // 12. Firma y Fecha
            document.add(new Paragraph("12. Firma y Fecha")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Profesional médico: ").setFont(bold).setFontSize(10))
                    .add(new Text("firmado electrónicamente por el profesional médico " + usuarioProfesional.getNombreCompleto() +
                            " mediante AutoFirma (o mediante su versión para móvil Cliente móvil @firma), conforme a la definición de firma electrónica avanzada en el art. 26 del Reglamento (UE) 910/2014 (eIDAS).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Paciente: ").setFont(bold).setFontSize(10))
                    .add(new Text("cofirma electrónica mediante Autofirma (o mediante su versión para móvil Cliente móvil @firma), conforme a la definición de firma electrónica avanzada en el art. 26 del Reglamento (UE) 910/2014 (eIDAS).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Validez jurídica: ").setFont(bold).setFontSize(10))
                    .add(new Text("ambas firmas cumplen los requisitos de seguridad y legalidad establecidos en la Ley 59/2003, de firma electrónica, y tienen plena eficacia probatoria.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // Fecha de expedición con formato DD/MM/YYYY
            String fechaExp = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            document.add(new Paragraph()
                    .add(new Text("Fecha de expedición de la autorización: ").setFont(bold).setFontSize(10))
                    .add(new Text(fechaExp).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(5)
            );

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de autorización", e);
        }
    }


    public byte[] generarPdfEdicionInforme(
            Long profesionalMedicoId,
            Long pacienteId,
            String tituloInforme,
            String descripcion,
            String observaciones) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            ProfesionalMedico profesional = profesionalMedicoRepository.findById(profesionalMedicoId).orElse(null);
            Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);

            Usuario usuarioProfesional = profesional.getUsuario();
            Usuario usuarioPaciente = paciente.getUsuario();


            CentroMedico centro = profesional.getCentroMedico();

            Usuario usuarioCentroMedico = usuarioRepository.findByCentroMedicoId(centro.getId()).orElse(null);

            // Fuentes para texto normal y negrita
            PdfFont font  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Crear documento PDF
            PdfWriter  writer  = new PdfWriter(baos);
            PdfDocument pdfDoc  = new PdfDocument(writer);
            Document  document = new Document(pdfDoc);
            document.setFont(font);

            // Tras crear PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event event) {
                    PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                    PdfPage page = docEvent.getPage();
                    int pageNumber = docEvent.getDocument().getPageNumber(page);
                    Rectangle pageSize = page.getPageSize();
                    PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), docEvent.getDocument());
                    // Aquí usamos sólo pdfCanvas y pageSize
                    Canvas canvas = new Canvas(pdfCanvas, pageSize);
                    canvas.setFont(font)
                            .setFontSize(10)
                            .showTextAligned(
                                    String.valueOf(pageNumber),
                                    pageSize.getRight() - 36,
                                    pageSize.getBottom() + 15,
                                    TextAlignment.RIGHT
                            );
                    canvas.close();
                }
            });

            // Línea de encabezado: ubicación y fecha en texto
            LocalDate today = LocalDate.now();
            String monthName = today.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("es","ES"))
                    .toLowerCase();
            String header = String.format("%s (%s), a %d de %s de %d.",
                    centro.getUsuario().getMunicipio(),
                    centro.getUsuario().getProvincia(),
                    today.getDayOfMonth(),
                    monthName,
                    today.getYear());
            document.add(new Paragraph(header)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10)
            );
            // Título principal
            document.add(new Paragraph("DOCUMENTO REPRESENTATIVO DE LA EDICIÓN DE UN INFORME DEL HISTORIAL MÉDICO CENTRALIZADO DE UN PACIENTE")
                    .setFont(bold)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15)
            );

            // 1. Introducción
            document.add(new Paragraph("1. Introducción")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "A continuación, se presenta el texto completo correspondiente al documento firmado " +
                            "que confirma y representa al informe médico editado " +
                            "del historial médico centralizado del paciente en cuestión, incluyendo todos los datos proporcionados, " +
                            "estructurado conforme a los requisitos de información y consentimiento " +
                            "recogidos en el Reglamento General de Protección de Datos (RGPD) y en la Ley " +
                            "Orgánica 3/2018 de Protección de Datos Personales y garantía de los derechos " +
                            "digitales (LOPDGDD). Se detallan las partes intervinientes, la finalidad del " +
                            "tratamiento, la base jurídica, el alcance de los derechos de las partes intervinientes, " +
                            "la identificación del responsable del tratamiento, " +
                            "así como la información pertinente a la firma eletrónica por parte del profesional médico).")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            // 2. Identificación de las partes
            document.add(new Paragraph("2. Identificación de las partes")
                    .setFont(bold)
                    .setFontSize(12)
            );
            // Profesional médico
            document.add(new Paragraph("Profesional médico")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioProfesional.getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nº Del Documento de Identificación: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioProfesional.getNifNie()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Especialidad: ").setFont(bold).setFontSize(10))
                    .add(new Text(profesional.getEspecialidadMedica().getNombre()).setFont(font).setFontSize(10))
                    .setMarginBottom(5)
            );

            // Centro médico asociado
            document.add(new Paragraph("Centro médico asociado")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("NIF: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getNifNie()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Provincia: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getProvincia()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Municipio: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getMunicipio()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Dirección: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getDireccion()).setFont(font).setFontSize(10))
                    .setMarginBottom(5)
            );

            // Paciente
            document.add(new Paragraph("Paciente")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioPaciente.getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nº Del Documento de Identificación: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioPaciente.getNifNie()).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );



            // 3. Objeto y Finalidad de la autorización
            document.add(new Paragraph("3. Objeto y Finalidad de este documento.")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "Mediante el presente documento, el profesional médico identificado " +
                            "en el apartado 1 efectúa y confirma la edición de un informe médico del historial clínico centralizado del paciente, garantizando la veracidad " +
                            "y la legitimidad de los datos incluidos en dicho informe médico. " +
                            "Tras haberse creado y subido el nuevo informe, el profesional médico " +
                            "podrá adjuntar al informe documentos clínicos específicos para aportar cualquier información extra que considere relevante. "  +
                            "Todos los documentos asociados al informe anterior se seguirán manteniendo y seguirán teniendo validez para el nuevo informe editado.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Cualquier profesional médico autorizado por el paciente tendrá acceso " +
                            "tanto al nuevo informe editado como a todos y cada uno de los documentos adjuntados a dicho informe ." +
                            "La desautorización por parte del paciente supondrá la pérdida del acceso por parte del profesional médico " +
                            "a informes y documentos de otros profesionales médicos autorizados. No obstante, a pesar de la desautorización, el profesional médico " +
                            "seguirá teniendo acceso a aquellos informes y documentos pasados que él mismo haya agregado al historial clínico del paciente.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                            "Todo lo establecido en este documento se ajusta plenamente a lo dispuesto en el artículo 9.2 a) del Reglamento (UE) 2016/679 (RGPD), " +
                            "que permite el tratamiento de datos de categorías especiales siempre que medie el consentimiento explícito del interesado. ")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );

            // 4. Responsable y Encargado del tratamiento
            document.add(new Paragraph("4. Responsable y Encargado del tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Responsable: ").setFont(bold).setFontSize(10))
                    .add(new Text("Centro Médico “" + centro.getUsuario().getNombreCompleto()
                            + "” (NIF " + centro.getUsuario().getNifNie() + ").").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Encargado: ").setFont(bold).setFontSize(10))
                    .add(new Text("VitalSanity S.A. (NIF A79667150), Calle Innovación, 123, nº 10, 28001 Madrid, España, correo: vital@sanity.es.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 5. Información específica del informe
            document.add(new Paragraph("5. Información específica del informe editado")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Título del informe: ").setFont(bold).setFontSize(10))
                    .add(new Text(tituloInforme).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Descripción del informe: ").setFont(bold).setFontSize(10))
                    .add(new Text(descripcion).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Observaciones del informe: ").setFont(bold).setFontSize(10))
                    .add(new Text(observaciones).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 6. Base Jurídica del Tratamiento
            document.add(new Paragraph("6. Base Jurídica del Tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Consentimiento informado: ").setFont(bold).setFontSize(10))
                    .add(new Text("La legitimación para el tratamiento se basa en el consentimiento libre, específico, informado e inequívoco del paciente, tal como exige el artículo 6 de la LOPDGDD y el artículo 6.1.a) del RGPD.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Datos de salud (categoría especial): ").setFont(bold).setFontSize(10))
                    .add(new Text("Se trata de datos relativos a la salud, cuya recogida y tratamiento requieren consentimiento explícito conforme al artículo 9.2.a) del RGPD.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Obligación de información: ").setFont(bold).setFontSize(10))
                    .add(new Text("El responsable ha facilitado al paciente toda la información prevista en el artículo 13 del RGPD en el momento de la obtención de sus datos.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10)
                    )
                    .add(new Text("Deber de confidencialidad: ").setFont(bold).setFontSize(10))
                    .add(new Text("El profesional y el centro están sujetos al deber de confidencialidad establecido en el artículo 5 de la LOPDGDD y en la Ley 41/2002, que protege la confidencialidad de los datos de salud.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 7. Datos objeto de tratamiento
            document.add(new Paragraph("7. Datos objeto de tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Informes médicos").setFont(bold).setFontSize(10))
                    .add(new Text(" (con sus descripciones, observaciones, …).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Documentación clínica").setFont(bold).setFontSize(10))
                    .add(new Text(" (informe de urgencias, notas de evolución, prescripciones, …).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Cualquier dato de salud recabado").setFont(bold).setFontSize(10))
                    .add(new Text(" en el historial centralizado.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 8. Principios aplicables
            document.add(new Paragraph("8. Principios aplicables")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El tratamiento de los datos se regirá por los principios de licitud, lealtad, transparencia, limitación de la finalidad, minimización de datos, exactitud, limitación del plazo de conservación, integridad y confidencialidad establecidos en el artículo 5 del RGPD, así como por las garantías adicionales recogidas en la LOPDGDD.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            // 9. Derechos de las partes involucradas
            document.add(new Paragraph("9. Derechos de las partes involucradas")
                    .setFont(bold)
                    .setFontSize(12)
            );
            String[] derechos = {
                    "Acceso a sus datos y a la información relacionada (art. 15 RGPD).",
                    "Rectificación de datos inexactos (art. 16 RGPD).",
                    "Supresión de datos («derecho al olvido», art. 17 RGPD).",
                    "Limitación del tratamiento (art. 18 RGPD).",
                    "Portabilidad de los datos (art. 20 RGPD).",
                    "Oposición al tratamiento (art. 21 RGPD).",
                    "Revocación del consentimiento en cualquier momento, sin afectar la licitud del tratamiento previo (art. 7.3 RGPD).",
                    "Derecho a no ser objeto de decisiones automatizadas."
            };
            for (String r : derechos) {
                document.add(new Paragraph("• " + r)
                        .setMarginLeft(10)
                        .setFont(font)
                        .setFontSize(10)
                );
            }
            document.add(new Paragraph(
                    "Podrá dirigir sus solicitudes al Responsable del Tratamiento, quien facilitará su ejercicio en un plazo máximo de un mes, prorrogable por otros dos meses en casos de especial complejidad,")
                    .setMarginLeft(10)
                    .setMarginBottom(5)
                    .setFont(font)
                    .setFontSize(10)
            );
            // Medios de ejercicio
            String centroEmailName = centro.getUsuario().getNombreCompleto()
                    .toLowerCase()
                    .replace(" ", "-");
            document.add(new Paragraph("• Puede contactar con el Delegado de Protección de Datos en delegadopd@"
                    + centroEmailName + ".es.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede contactar con el encargado en vital@sanity.es.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede presentarse presencialmente en el domicilio social de las entidades.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede presentar reclamación ante la AEPD.")
                    .setMarginLeft(10)
                    .setMarginBottom(10)
                    .setFont(font)
                    .setFontSize(10)
            );

            // 10. Revocación y Plazo de Conservación
            document.add(new Paragraph("10. Revocación y Plazo de Conservación")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El paciente podrá revocar libremente su consentimiento en cualquier momento, sin que ello afecte la licitud del tratamiento realizado con anterioridad a la revocación, conforme al artículo 7.3 del RGPD, que exige que la retirada del consentimiento sea tan sencilla como su otorgamiento.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Los datos se conservarán únicamente durante el tiempo necesario para cumplir las finalidades sanitarias y para atender las obligaciones legales y de custodia establecidas en la normativa sectorial.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(10)
            );

            // 11. Medidas de seguridad
            document.add(new Paragraph("11. Medidas de seguridad")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El responsable del tratamiento garantiza la aplicación de las medidas técnicas y organizativas apropiadas para proteger los datos personales frente a destrucción, pérdida, alteración, divulgación o acceso no autorizado, conforme al artículo 32 del RGPD y a las directrices establecidas por la Agencia Española de Protección de Datos.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(10)
            );

            // 12. Firma y Fecha
            document.add(new Paragraph("12. Firma y Fecha")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Profesional médico: ").setFont(bold).setFontSize(10))
                    .add(new Text("firmado electrónicamente por el profesional médico " + usuarioProfesional.getNombreCompleto() +
                            " mediante AutoFirma (o mediante su versión para móvil Cliente móvil @firma), conforme a la definición de firma electrónica avanzada en el art. 26 del Reglamento (UE) 910/2014 (eIDAS).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Validez jurídica: ").setFont(bold).setFontSize(10))
                    .add(new Text("La firma del profesional médico cumple los requisitos de seguridad y legalidad establecidos en la Ley 59/2003, de firma electrónica, y tiene plena eficacia probatoria.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // Fecha de expedición con formato DD/MM/YYYY
            String fechaExp = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            document.add(new Paragraph()
                    .add(new Text("Fecha de expedición de la autorización: ").setFont(bold).setFontSize(10))
                    .add(new Text(fechaExp).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(5)
            );

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }



    public byte[] generarPdfInforme(
            Long profesionalMedicoId,
            Long pacienteId,
            String tituloInforme,
            String descripcion,
            String observaciones) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            ProfesionalMedico profesional = profesionalMedicoRepository.findById(profesionalMedicoId).orElse(null);
            Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);

            Usuario usuarioProfesional = profesional.getUsuario();
            Usuario usuarioPaciente = paciente.getUsuario();


            CentroMedico centro = profesional.getCentroMedico();

            Usuario usuarioCentroMedico = usuarioRepository.findByCentroMedicoId(centro.getId()).orElse(null);

            // Fuentes para texto normal y negrita
            PdfFont font  = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Crear documento PDF
            PdfWriter  writer  = new PdfWriter(baos);
            PdfDocument pdfDoc  = new PdfDocument(writer);
            Document  document = new Document(pdfDoc);
            document.setFont(font);

            // Tras crear PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
                @Override
                public void handleEvent(Event event) {
                    PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                    PdfPage page = docEvent.getPage();
                    int pageNumber = docEvent.getDocument().getPageNumber(page);
                    Rectangle pageSize = page.getPageSize();
                    PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), docEvent.getDocument());
                    // Aquí usamos sólo pdfCanvas y pageSize
                    Canvas canvas = new Canvas(pdfCanvas, pageSize);
                    canvas.setFont(font)
                            .setFontSize(10)
                            .showTextAligned(
                                    String.valueOf(pageNumber),
                                    pageSize.getRight() - 36,
                                    pageSize.getBottom() + 15,
                                    TextAlignment.RIGHT
                            );
                    canvas.close();
                }
            });

            // Línea de encabezado: ubicación y fecha en texto
            LocalDate today = LocalDate.now();
            String monthName = today.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("es","ES"))
                    .toLowerCase();
            String header = String.format("%s (%s), a %d de %s de %d.",
                    centro.getUsuario().getMunicipio(),
                    centro.getUsuario().getProvincia(),
                    today.getDayOfMonth(),
                    monthName,
                    today.getYear());
            document.add(new Paragraph(header)
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(10)
            );
            // Título principal
            document.add(new Paragraph("DOCUMENTO REPRESENTATIVO DE LA AGREGACIÓN DE UN NUEVO INFORME AL HISTORIAL MÉDICO CENTRALIZADO DE UN PACIENTE")
                    .setFont(bold)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15)
            );

            // 1. Introducción
            document.add(new Paragraph("1. Introducción")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "A continuación, se presenta el texto completo correspondiente al documento firmado " +
                            "que confirma y representa al informe médico creado y agregado " +
                            "al historial médico centralizado del paciente en cuestión, incluyendo todos los datos proporcionados, " +
                            "estructurado conforme a los requisitos de información y consentimiento " +
                            "recogidos en el Reglamento General de Protección de Datos (RGPD) y en la Ley " +
                            "Orgánica 3/2018 de Protección de Datos Personales y garantía de los derechos " +
                            "digitales (LOPDGDD). Se detallan las partes intervinientes, la finalidad del " +
                            "tratamiento, la base jurídica, el alcance de los derechos de las partes intervinientes, " +
                            "la identificación del responsable del tratamiento, " +
                            "así como la información pertinente a la firma eletrónica por parte del profesional médico).")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            // 2. Identificación de las partes
            document.add(new Paragraph("2. Identificación de las partes")
                    .setFont(bold)
                    .setFontSize(12)
            );
            // Profesional médico
            document.add(new Paragraph("Profesional médico")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioProfesional.getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nº Del Documento de Identificación: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioProfesional.getNifNie()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Especialidad: ").setFont(bold).setFontSize(10))
                    .add(new Text(profesional.getEspecialidadMedica().getNombre()).setFont(font).setFontSize(10))
                    .setMarginBottom(5)
            );

            // Centro médico asociado
            document.add(new Paragraph("Centro médico asociado")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("NIF: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getNifNie()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Provincia: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getProvincia()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Municipio: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getUsuario().getMunicipio()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Dirección: ").setFont(bold).setFontSize(10))
                    .add(new Text(centro.getDireccion()).setFont(font).setFontSize(10))
                    .setMarginBottom(5)
            );

            // Paciente
            document.add(new Paragraph("Paciente")
                    .setFont(bold)
                    .setMarginLeft(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nombre: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioPaciente.getNombreCompleto()).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(20)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Nº Del Documento de Identificación: ").setFont(bold).setFontSize(10))
                    .add(new Text(usuarioPaciente.getNifNie()).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );


            // 3. Objeto y Finalidad de la autorización
            document.add(new Paragraph("3. Objeto y Finalidad de este documento.")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "Mediante el presente documento, el profesional médico identificado " +
                            "en el apartado 1 efectúa y confirma la agregación de un nuevo informe médico al historial clínico centralizado del paciente, garantizando la veracidad " +
                            "y la legitimidad de los datos incluidos en dicho informe médico. " +
                            "Tras haberse creado y subido el informe en cuestión, el profesional médico " +
                            "podrá adjuntar al informe documentos clínicos específicos para aportar cualquier información extra que considere relevante. ")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Cualquier profesional médico autorizado por el paciente tendrá acceso " +
                            "tanto al nuevo informe creado como a todos y cada uno de los documentos adjuntados a dicho informe ." +
                            "La desautorización por parte del paciente supondrá la pérdida del acceso por parte del profesional médico " +
                            "a informes y documentos de otros profesionales médicos autorizados. No obstante, a pesar de la desautorización, el profesional médico " +
                            "seguirá teniendo acceso a aquellos informes y documentos pasados que él mismo haya agregado al historial clínico del paciente.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Todo lo establecido en este documento se ajusta plenamente a lo dispuesto en el artículo 9.2 a) del Reglamento (UE) 2016/679 (RGPD), " +
                            "que permite el tratamiento de datos de categorías especiales siempre que medie el consentimiento explícito del interesado. ")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(5)
            );

            // 4. Responsable y Encargado del tratamiento
            document.add(new Paragraph("4. Responsable y Encargado del tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Responsable: ").setFont(bold).setFontSize(10))
                    .add(new Text("Centro Médico “" + centro.getUsuario().getNombreCompleto()
                            + "” (NIF " + centro.getUsuario().getNifNie() + ").").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Encargado: ").setFont(bold).setFontSize(10))
                    .add(new Text("VitalSanity S.A. (NIF A79667150), Calle Innovación, 123, nº 10, 28001 Madrid, España, correo: vital@sanity.es.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 5. Información específica del informe
            document.add(new Paragraph("5. Información específica del informe")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Título del informe: ").setFont(bold).setFontSize(10))
                    .add(new Text(tituloInforme).setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Descripción del informe: ").setFont(bold).setFontSize(10))
                    .add(new Text(descripcion).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Observaciones del informe: ").setFont(bold).setFontSize(10))
                    .add(new Text(observaciones).setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 6. Base Jurídica del Tratamiento
            document.add(new Paragraph("6. Base Jurídica del Tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Consentimiento informado: ").setFont(bold).setFontSize(10))
                    .add(new Text("La legitimación para el tratamiento se basa en el consentimiento libre, específico, informado e inequívoco del paciente, tal como exige el artículo 6 de la LOPDGDD y el artículo 6.1.a) del RGPD.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Datos de salud (categoría especial): ").setFont(bold).setFontSize(10))
                    .add(new Text("Se trata de datos relativos a la salud, cuya recogida y tratamiento requieren consentimiento explícito conforme al artículo 9.2.a) del RGPD.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Obligación de información: ").setFont(bold).setFontSize(10))
                    .add(new Text("El responsable ha facilitado al paciente toda la información prevista en el artículo 13 del RGPD en el momento de la obtención de sus datos.").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10)
                    )
                    .add(new Text("Deber de confidencialidad: ").setFont(bold).setFontSize(10))
                    .add(new Text("El profesional y el centro están sujetos al deber de confidencialidad establecido en el artículo 5 de la LOPDGDD y en la Ley 41/2002, que protege la confidencialidad de los datos de salud.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 7. Datos objeto de tratamiento
            document.add(new Paragraph("7. Datos objeto de tratamiento")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Informes médicos").setFont(bold).setFontSize(10))
                    .add(new Text(" (con sus descripciones, observaciones, …).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Documentación clínica").setFont(bold).setFontSize(10))
                    .add(new Text(" (informe de urgencias, notas de evolución, prescripciones, …).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Cualquier dato de salud recabado").setFont(bold).setFontSize(10))
                    .add(new Text(" en el historial centralizado.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // 8. Principios aplicables
            document.add(new Paragraph("8. Principios aplicables")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El tratamiento de los datos se regirá por los principios de licitud, lealtad, transparencia, limitación de la finalidad, minimización de datos, exactitud, limitación del plazo de conservación, integridad y confidencialidad establecidos en el artículo 5 del RGPD, así como por las garantías adicionales recogidas en la LOPDGDD.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginBottom(10)
            );

            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            // 9. Derechos de las partes involucradas
            document.add(new Paragraph("9. Derechos de las partes involucradas")
                    .setFont(bold)
                    .setFontSize(12)
            );
            String[] derechos = {
                    "Acceso a sus datos y a la información relacionada (art. 15 RGPD).",
                    "Rectificación de datos inexactos (art. 16 RGPD).",
                    "Supresión de datos («derecho al olvido», art. 17 RGPD).",
                    "Limitación del tratamiento (art. 18 RGPD).",
                    "Portabilidad de los datos (art. 20 RGPD).",
                    "Oposición al tratamiento (art. 21 RGPD).",
                    "Revocación del consentimiento en cualquier momento, sin afectar la licitud del tratamiento previo (art. 7.3 RGPD).",
                    "Derecho a no ser objeto de decisiones automatizadas."
            };
            for (String r : derechos) {
                document.add(new Paragraph("• " + r)
                        .setMarginLeft(10)
                        .setFont(font)
                        .setFontSize(10)
                );
            }
            document.add(new Paragraph(
                    "Podrá dirigir sus solicitudes al Responsable del Tratamiento, quien facilitará su ejercicio en un plazo máximo de un mes, prorrogable por otros dos meses en casos de especial complejidad,")
                    .setMarginLeft(10)
                    .setMarginBottom(5)
                    .setFont(font)
                    .setFontSize(10)
            );
            // Medios de ejercicio
            String centroEmailName = centro.getUsuario().getNombreCompleto()
                    .toLowerCase()
                    .replace(" ", "-");
            document.add(new Paragraph("• Puede contactar con el Delegado de Protección de Datos en delegadopd@"
                    + centroEmailName + ".es.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede contactar con el encargado en vital@sanity.es.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede presentarse presencialmente en el domicilio social de las entidades.")
                    .setMarginLeft(10)
                    .setFont(font)
                    .setFontSize(10)
            );
            document.add(new Paragraph("• Puede presentar reclamación ante la AEPD.")
                    .setMarginLeft(10)
                    .setMarginBottom(10)
                    .setFont(font)
                    .setFontSize(10)
            );

            // 10. Revocación y Plazo de Conservación
            document.add(new Paragraph("10. Revocación y Plazo de Conservación")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El paciente podrá revocar libremente su consentimiento en cualquier momento, sin que ello afecte la licitud del tratamiento realizado con anterioridad a la revocación, conforme al artículo 7.3 del RGPD, que exige que la retirada del consentimiento sea tan sencilla como su otorgamiento.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(5)
            );
            document.add(new Paragraph(
                    "Los datos se conservarán únicamente durante el tiempo necesario para cumplir las finalidades sanitarias y para atender las obligaciones legales y de custodia establecidas en la normativa sectorial.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(10)
            );

            // 11. Medidas de seguridad
            document.add(new Paragraph("11. Medidas de seguridad")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph(
                    "El responsable del tratamiento garantiza la aplicación de las medidas técnicas y organizativas apropiadas para proteger los datos personales frente a destrucción, pérdida, alteración, divulgación o acceso no autorizado, conforme al artículo 32 del RGPD y a las directrices establecidas por la Agencia Española de Protección de Datos.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginLeft(10)
                    .setMarginBottom(10)
            );

            // 12. Firma y Fecha
            document.add(new Paragraph("12. Firma y Fecha")
                    .setFont(bold)
                    .setFontSize(12)
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Profesional médico: ").setFont(bold).setFontSize(10))
                    .add(new Text("firmado electrónicamente por el profesional médico " + usuarioProfesional.getNombreCompleto() +
                            " mediante AutoFirma (o mediante su versión para móvil Cliente móvil @firma), conforme a la definición de firma electrónica avanzada en el art. 26 del Reglamento (UE) 910/2014 (eIDAS).").setFont(font).setFontSize(10))
            );
            document.add(new Paragraph()
                    .setMarginLeft(10)
                    .add(new Text("• ").setFont(font).setFontSize(10))
                    .add(new Text("Validez jurídica: ").setFont(bold).setFontSize(10))
                    .add(new Text("La firma del profesional médico cumple los requisitos de seguridad y legalidad establecidos en la Ley 59/2003, de firma electrónica, y tiene plena eficacia probatoria.").setFont(font).setFontSize(10))
                    .setMarginBottom(10)
            );

            // Fecha de expedición con formato DD/MM/YYYY
            String fechaExp = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            document.add(new Paragraph()
                    .add(new Text("Fecha de expedición de la autorización: ").setFont(bold).setFontSize(10))
                    .add(new Text(fechaExp).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(5)
            );

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }
}
