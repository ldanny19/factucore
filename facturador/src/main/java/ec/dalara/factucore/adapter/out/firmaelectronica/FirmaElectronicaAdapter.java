package ec.dalara.factucore.adapter.out.firmaelectronica;

import ec.dalara.factucore.application.ApplicationException;
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
import java.util.Arrays;

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
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.CERTIFICADO.ACCESO_ERROR",
                    certificado.getRutaCertificado()
            );
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void validarEntrada(
            String xml,
            CertificadoFirmaModel certificado,
            char[] password
    ) {
        if (xml == null || xml.isBlank()) {
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.XML.REQUERIDO"
            );
        }

        if (certificado == null) {
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.CERTIFICADO.REQUERIDO"
            );
        }

        if (password == null || password.length == 0) {
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.PASSWORD.REQUERIDO"
            );
        }

        Path ruta = Path.of(certificado.getRutaCertificado());

        if (!Files.isRegularFile(ruta)) {
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.CERTIFICADO.NO_ENCONTRADO",
                    certificado.getRutaCertificado()
            );
        }
    }

    private DSSPrivateKeyEntry obtenerClave(
            Pkcs12SignatureToken token
    ) {
        var claves = token.getKeys();

        if (claves == null || claves.isEmpty()) {
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.CLAVE_PRIVADA.NO_ENCONTRADA"
            );
        }

        if (claves.size() > 1) {
            throw new ApplicationException(
                    "FACTUCORE.FIRMA_ELECTRONICA.CLAVES_PRIVADAS.MULTIPLES"
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