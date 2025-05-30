package vitalsanity.dto.centro_medico;

import org.springframework.web.multipart.MultipartFile;

public class RegistroProfesionalesMedicosData {
    private MultipartFile csvFile;

    public MultipartFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(MultipartFile csvFile) {
        this.csvFile = csvFile;
    }
}
