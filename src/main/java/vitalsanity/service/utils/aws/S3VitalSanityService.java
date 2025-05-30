package vitalsanity.service.utils.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.List;


@Service
public class S3VitalSanityService {

    private S3Client s3Client;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.kms.key-id:}")
    private String kmsKeyId;

    @PostConstruct
    private void init() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void subirFichero(String key, MultipartFile file) throws IOException {
        /* He comentado la línea del cifrado AWS_KMS debido a que me estaban cargando costes adicionales de AWS.
           Como ya se ha explicado en la memoria del TFG, el uso de KMS sí que resultaría interesante
           ya que permite un control más granular sobre las claves de cifrado y su gestión, ofreciendo una mayor personalización.
           Pero para evitar más sablazos inesperados de AWS, he optado por sustituir AWS_KMS por AES256,
         */
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                //.serverSideEncryption(ServerSideEncryption.AWS_KMS)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
    }

    public void subirFicheroBytes(String key, byte[] fileContent) throws IOException {
        /* He comentado la línea del cifrado AWS_KMS debido a que me estaban cargando costes adicionales de AWS.
           Como ya se ha explicado en la memoria del TFG, el uso de KMS sí que resultaría interesante
           ya que permite un control más granular sobre las claves de cifrado y su gestión, ofreciendo una mayor personalización.
           Pero para evitar más sablazos inesperados de AWS, he optado por sustituir AWS_KMS por AES256,
         */
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                //.serverSideEncryption(ServerSideEncryption.AWS_KMS)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(fileContent));
    }

    public byte[] obtenerBytesFicheroAPartirDeS3Key(String s3Key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        return objectBytes.asByteArray();
    }


    // Generar una URL pre-firmada para descargar el archivo
    public String generarUrlPrefirmada(String s3Key, Duration duration) {
        // Creamos un S3Presigner (se autogestiona con DefaultCredentialsProvider)
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest =
                    presigner.presignGetObject(presignRequest);

            // Retorna la URL pre-firmada
            return presignedGetObjectRequest.url().toString();
        }
    }

    // Listar objetos de S3 (no se usa si obtenemos la info de la DB)
    public List<S3Object> listAllFilesInBucket() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(listRequest);
        return result.contents();
    }
}
