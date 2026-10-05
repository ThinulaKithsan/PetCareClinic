package lk.petcare;
import net.sf.jasperreports.engine.*;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
public final class ReportService {
    public void export(LocalDate from, LocalDate to, Path file) throws Exception {
        if (to.isBefore(from)) throw new ValidationException("End date must not be before start date.");
        try (InputStream template = getClass().getResourceAsStream("/reports/visits.jrxml"); Connection c = Database.open()) {
            if (template == null) throw new IllegalStateException("Report template is missing.");
            JasperReport report = JasperCompileManager.compileReport(template);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("FROM_DATE", java.sql.Timestamp.valueOf(from.atStartOfDay()));
            parameters.put("TO_DATE", java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            JasperPrint filled = JasperFillManager.fillReport(report, parameters, c);
            JasperExportManager.exportReportToPdfFile(filled, file.toString());
        }
    }
}
