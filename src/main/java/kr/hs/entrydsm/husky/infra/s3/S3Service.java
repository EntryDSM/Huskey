package kr.hs.entrydsm.husky.infra.s3;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import kr.hs.entrydsm.husky.infra.s3.auth.AWS4SignerBase;
import kr.hs.entrydsm.husky.infra.s3.util.BinaryUtils;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static kr.hs.entrydsm.husky.infra.s3.auth.AWS4SignerBase.*;

@NoArgsConstructor
@Service
public class S3Service extends AWS4Signer {

    private static final String SCHEME = "AWS4";

    private static final String ALGORITHM = "HMAC-SHA256";

    private static final Integer EXPIRES = 900;

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ISO8601BasicFormat);

    private SimpleDateFormat dateStampFormat = new SimpleDateFormat("yyyyMMdd");

    private AmazonS3 s3Client;

    @Value("${aws.s3.access_key}")
    private String accessKey;

    @Value("${aws.s3.secret_key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.base_image_url}")
    private String baseImageUrl;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring( originalFilename.lastIndexOf(".") + 1);
        String randomName = UUID.randomUUID().toString();
        String filename = randomName + "." + ext;

        s3Client.putObject(new PutObjectRequest(bucket, filename, file.getInputStream(), null)
                .withCannedAcl(CannedAccessControlList.AuthenticatedRead));

        return s3Client.getUrl(bucket, filename).toString();
    }

    public String generateS3ObjectUrl(String objectName) throws MalformedURLException {
        URL endpointUrl = new URL("https://" + baseImageUrl + ".s3." + region + ".amazonaws.com" + objectName);

        // X-Amz-Algorithm
        String x_amz_algorithm = SCHEME + "-" + ALGORITHM;

        // X-Amz-Credential
        dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        Date now = new Date();
        String dateStamp = dateStampFormat.format(now);
        String scope = dateStamp + "/" + region + "/s3/aws4_request";
        String x_amz_credential = accessKey + "/" + scope;

        // X-Amz-Date
        dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
        String x_amz_date = dateTimeFormat.format(now);

        // X-Amz-SignedHeaders
        Map<String, String> headers = new HashMap<>();
        String hostHeader = endpointUrl.getHost();
        headers.put("Host", hostHeader);
        String canonicalizedHeaderNames = getCanonicalizeHeaderNames(headers);
        String canonicalizedHeaders = AWS4SignerBase.getCanonicalizedHeaderString(headers);

        // X-Amz_Signature
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("X-Amz-Algorithm", x_amz_algorithm);
        queryParameters.put("X-Amz-Credential", x_amz_credential);
        queryParameters.put("X-Amz-Date", x_amz_date);
        queryParameters.put("X-Amz-SignedHeaders", canonicalizedHeaderNames);
        queryParameters.put("X-Amz-Expires", Integer.toString(EXPIRES));
        String canonicalizedQueryParameters = AWS4SignerBase.getCanonicalizedQueryString(queryParameters);

        String canonicalRequest = getCanonicalRequest(endpointUrl, "GET",
                canonicalizedQueryParameters, canonicalizedHeaderNames,
                canonicalizedHeaders, AWS4SignerBase.UNSIGNED_PAYLOAD);

        String stringToSign = getStringToSign(SCHEME, ALGORITHM, x_amz_date, scope, canonicalRequest);

        byte[] kSecret = (SCHEME + secretKey).getBytes();
        byte[] kDate = AWS4SignerBase.sign(dateStamp, kSecret, "HmacSHA256");
        byte[] kRegion = AWS4SignerBase.sign(region, kDate, "HmacSHA256");
        byte[] kService = AWS4SignerBase.sign("s3", kRegion, "HmacSHA256");
        byte[] kSigning = AWS4SignerBase.sign(TERMINATOR, kService, "HmacSHA256");
        byte[] signature = AWS4SignerBase.sign(stringToSign, kSigning, "HmacSHA256");

        StringBuilder authString = new StringBuilder();

        authString.append(endpointUrl.toString());
        authString.append("?X-Amz-Algorithm=" + queryParameters.get("X-Amz-Algorithm"));
        authString.append("&X-Amz-Credential=" + queryParameters.get("X-Amz-Credential"));
        authString.append("&X-Amz-Date=" + queryParameters.get("X-Amz-Date"));
        authString.append("&X-Amz-Expires=" + queryParameters.get("X-Amz-Expires"));
        authString.append("&X-Amz-SignedHeaders=" + queryParameters.get("X-Amz-SignedHeaders"));
        authString.append("&X-Amz-Signature=" + BinaryUtils.toHex(signature));

        return authString.toString();
    }

}
