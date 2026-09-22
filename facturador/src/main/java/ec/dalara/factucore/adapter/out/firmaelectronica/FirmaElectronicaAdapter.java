package ec.dalara.factucore.adapter.out.firmaelectronica;

import ec.dalara.factucore.application.port.out.FirmaElectronicaPort;
import ec.dalara.factucore.domain.firmaelectronica.CertificadoFirmaModel;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

@Component
public class FirmaElectronicaAdapter implements FirmaElectronicaPort {

    @Override
    public String firmar(
            String xml,
            CertificadoFirmaModel certificado,
            char[] password
    ) {
        validarEntrada(xml, certificado, password);

        KeyStore.PasswordProtection passwordProtection =
                new KeyStore.PasswordProtection(password);

        try (Pkcs12SignatureToken token =
                     new Pkcs12SignatureToken(
                             certificado.getRutaCertificado(),
                             passwordProtection
                     )) {

            DSSPrivateKeyEntry privateKey = obtenerClave(token);

            DSSDocument documento =
                    new eu.europa.esig.dss.model.InMemoryDocument(
                            xml.getBytes(StandardCharsets.UTF_8),
                            "comprobante.xml"
                    );

            XAdESSignatureParameters parameters =
                    crearParametros(privateKey);

            CommonCertificateVerifier certificateVerifier =
                    new CommonCertificateVerifier();

            XAdESService service =
                    new XAdESService(certificateVerifier);

            ToBeSigned dataToSign =
                    service.getDataToSign(
                            documento,
                            parameters
                    );

            SignatureValue signatureValue =
                    token.sign(
                            dataToSign,
                            parameters.getDigestAlgorithm(),
                            privateKey
                    );

            DSSDocument documentoFirmado =
                    service.signDocument(
                            documento,
                            parameters,
                            signatureValue
                    );

            return new String(
                    documentoFirmado.getBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No fue posible acceder al certificado de firma electrónica",
                    e
            );
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void validarEntrada(
            String xml,
            CertificadoFirmaModel certificado,
            char[] password
    ) {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException(
                    "El XML a firmar es obligatorio"
            );
        }

        if (certificado == null) {
            throw new IllegalArgumentException(
                    "El certificado de firma es obligatorio"
            );
        }

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException(
                    "La contraseña del certificado es obligatoria"
            );

        }

        if (!Files.isRegularFile(
                Path.of(certificado.getRutaCertificado()))) {
            throw new IllegalArgumentException(
                    "El archivo del certificado no existe"
            );
        }
    }

    private DSSPrivateKeyEntry obtenerClave(
            Pkcs12SignatureToken token
    ) {
        var claves = token.getKeys();

        if (claves == null || claves.isEmpty()) {
            throw new IllegalStateException(
                    "El certificado no contiene una clave privada utilizable"
            );
        }

        if (claves.size() > 1) {
            throw new IllegalStateException(
                    "El certificado contiene múltiples claves privadas"
            );
        }

        return claves.get(0);
    }

    private XAdESSignatureParameters crearParametros(
            DSSPrivateKeyEntry privateKey
    ) {
        XAdESSignatureParameters parameters =
                new XAdESSignatureParameters();

        parameters.setSignatureLevel(
                SignatureLevel.XAdES_BASELINE_B
        );

        parameters.setSignaturePackaging(
                SignaturePackaging.ENVELOPED
        );

        parameters.setDigestAlgorithm(
                DigestAlgorithm.SHA256
        );

        parameters.setSigningCertificate(
                privateKey.getCertificate()
        );

        parameters.setCertificateChain(
                privateKey.getCertificateChain()
        );

        return parameters;
    }
}