package com.fintech.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.UUID;

public class QrService {

    private final String qrDirectory;

    public QrService(String qrDirectory) {
        this.qrDirectory = qrDirectory;
    }

    public String generateQr(String paymentUrl) {

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    paymentUrl,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            String fileName = UUID.randomUUID() + ".png";

            Path path = FileSystems.getDefault().getPath(qrDirectory, fileName);

            MatrixToImageWriter.writeToPath(
                    bitMatrix,
                    "PNG",
                    path
            );

            return "/qr/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR", e);
        }
    }
}
