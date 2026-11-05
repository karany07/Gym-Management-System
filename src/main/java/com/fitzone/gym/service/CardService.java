package com.fitzone.gym.service;

import com.fitzone.gym.model.Member;
import com.fitzone.gym.repository.MemberRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final MemberRepository members;

    @Value("${gym.base-url}")
    private String baseUrl;

    /** Member ke QR ka SVG. Token na ho to yahin bana deta hai. */
    @Transactional
    public String qrSvgFor(Member m) {
        if (m.getQrToken() == null) {
            m.setQrToken(UUID.randomUUID().toString());
            members.save(m);
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return toSvg(base + "/owner/verify/" + m.getQrToken());
    }

    private String toSvg(String text) {
        BitMatrix matrix;
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 0,
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
        } catch (WriterException e) {
            throw new IllegalStateException("QR code nahi ban paya", e);
        }

        int n = matrix.getWidth();
        int quiet = 4;                       // QR ke charon taraf safed jagah
        int size = n + quiet * 2;

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
          .append(size).append(' ').append(size)
          .append("\" shape-rendering=\"crispEdges\">");
        sb.append("<rect width=\"").append(size).append("\" height=\"").append(size)
          .append("\" fill=\"#fff\"/>");
        sb.append("<path fill=\"#000\" d=\"");
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < n; x++) {
                if (matrix.get(x, y)) {
                    sb.append('M').append(x + quiet).append(' ').append(y + quiet).append("h1v1h-1z");
                }
            }
        }
        sb.append("\"/></svg>");
        return sb.toString();
    }
}