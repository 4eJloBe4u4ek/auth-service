package backend.authservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class GAService {
    private static final String ISSUER = "MyNewsApp";

    public String generateSecret() {
        GoogleAuthenticator googleAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = googleAuth.createCredentials();

        return key.getKey();
    }

    public boolean verifySecret(String secret, int code) {
        GoogleAuthenticator googleAuth = new GoogleAuthenticator(
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().setWindowSize(5).build()
        );

        return googleAuth.authorize(secret, code);
    }

    public String buildOtpAuthUrl(String username, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                ISSUER,
                username,
                new GoogleAuthenticatorKey.Builder(secret).build());
    }

    public String generateQRBase64(String otpAuthUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 200, 200, hintMap);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            return null;
        }
    }
}
