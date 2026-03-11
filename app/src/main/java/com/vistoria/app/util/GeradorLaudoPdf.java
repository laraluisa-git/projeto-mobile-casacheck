package com.vistoria.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.vistoria.app.data.model.Comodo;
import com.vistoria.app.data.model.Vistoria;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeradorLaudoPdf {

    public static File gerar(Context context, Vistoria vistoria) throws IOException {
        PdfDocument document = new PdfDocument();

        Paint tituloPaint = new Paint();
        tituloPaint.setTextSize(22f);
        tituloPaint.setFakeBoldText(true);

        Paint secaoPaint = new Paint();
        secaoPaint.setTextSize(16f);
        secaoPaint.setFakeBoldText(true);

        Paint textoPaint = new Paint();
        textoPaint.setTextSize(12f);

        Paint pequenoPaint = new Paint();
        pequenoPaint.setTextSize(10f);

        Paint linhaPaint = new Paint();
        linhaPaint.setStrokeWidth(1.5f);

        int pageWidth = 595;
        int pageHeight = 842;
        int paginaNumero = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int margemEsquerda = 40;
        int margemDireita = 555;
        int y = 50;

        // Cabeçalho
        canvas.drawText("VISTORIA APP", margemEsquerda, y, tituloPaint);
        y += 28;
        canvas.drawText("Laudo de Vistoria de Imóvel", margemEsquerda, y, secaoPaint);
        y += 15;
        canvas.drawLine(margemEsquerda, y, margemDireita, y, linhaPaint);
        y += 25;

        // Dados gerais
        canvas.drawText("DADOS GERAIS", margemEsquerda, y, secaoPaint);
        y += 22;

        canvas.drawText("Protocolo: " + nullSafe(vistoria.getProtocolo()), margemEsquerda, y, textoPaint);
        y += 18;
        canvas.drawText("Imóvel: " + nullSafe(vistoria.getNomeImovel()), margemEsquerda, y, textoPaint);
        y += 18;
        canvas.drawText("Endereço: " + nullSafe(vistoria.getEndereco()), margemEsquerda, y, textoPaint);
        y += 18;
        canvas.drawText("Responsável: " + nullSafe(vistoria.getResponsavel()), margemEsquerda, y, textoPaint);
        y += 18;
        canvas.drawText("Data: " + nullSafe(vistoria.getData()), margemEsquerda, y, textoPaint);
        y += 18;
        canvas.drawText("GPS: " + nullSafe(vistoria.getCoordenadasFormatadas()), margemEsquerda, y, textoPaint);
        y += 22;

        canvas.drawLine(margemEsquerda, y, margemDireita, y, linhaPaint);
        y += 25;

        // Seção de cômodos
        canvas.drawText("CÔMODOS INSPECIONADOS", margemEsquerda, y, secaoPaint);
        y += 25;

        if (vistoria.getComodos() != null && !vistoria.getComodos().isEmpty()) {
            for (Comodo comodo : vistoria.getComodos()) {

                if (y > 700) {
                    desenharRodape(canvas, linhaPaint, pequenoPaint, paginaNumero);
                    document.finishPage(page);

                    paginaNumero++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                canvas.drawText("Cômodo: " + nullSafe(comodo.getNome()), margemEsquerda, y, secaoPaint);
                y += 20;

                canvas.drawText("Status: " + nullSafe(comodo.getStatus()), margemEsquerda, y, textoPaint);
                y += 18;

                String observacao = comodo.getObservacoes();
                if (observacao != null && !observacao.trim().isEmpty()) {
                    canvas.drawText("Observações:", margemEsquerda, y, textoPaint);
                    y += 18;

                    for (String linha : quebrarTexto(observacao, 78)) {
                        if (y > 730) {
                            desenharRodape(canvas, linhaPaint, pequenoPaint, paginaNumero);
                            document.finishPage(page);

                            paginaNumero++;
                            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create();
                            page = document.startPage(pageInfo);
                            canvas = page.getCanvas();
                            y = 50;
                        }

                        canvas.drawText(linha, margemEsquerda + 10, y, textoPaint);
                        y += 16;
                    }
                } else {
                    canvas.drawText("Observações: Sem registros", margemEsquerda, y, textoPaint);
                    y += 18;
                }

                canvas.drawText("Quantidade de fotos: " + comodo.getNumeroFotos(), margemEsquerda, y, textoPaint);
                y += 18;

                if (comodo.getFotos() != null && !comodo.getFotos().isEmpty()) {
                    canvas.drawText("Fotos do cômodo:", margemEsquerda, y, textoPaint);
                    y += 15;

                    for (String caminhoFoto : comodo.getFotos()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(caminhoFoto);

                        if (bitmap != null) {
                            Bitmap bitmapEscalado = Bitmap.createScaledBitmap(bitmap, 240, 180, true);

                            if (y + 190 > 760) {
                                desenharRodape(canvas, linhaPaint, pequenoPaint, paginaNumero);
                                document.finishPage(page);

                                paginaNumero++;
                                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create();
                                page = document.startPage(pageInfo);
                                canvas = page.getCanvas();
                                y = 50;
                            }

                            canvas.drawBitmap(bitmapEscalado, margemEsquerda + 10, y, null);
                            y += 190;
                        }
                    }
                }

                canvas.drawLine(margemEsquerda, y, margemDireita, y, linhaPaint);
                y += 20;
            }
        } else {
            canvas.drawText("Nenhum cômodo inspecionado.", margemEsquerda, y, textoPaint);
            y += 20;
        }

        // Assinatura
        if (y > 700) {
            desenharRodape(canvas, linhaPaint, pequenoPaint, paginaNumero);
            document.finishPage(page);

            paginaNumero++;
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, paginaNumero).create();
            page = document.startPage(pageInfo);
            canvas = page.getCanvas();
            y = 80;
        }

        y += 20;
        canvas.drawText("Assinatura do responsável:", margemEsquerda, y, textoPaint);
        y += 35;
        canvas.drawLine(margemEsquerda, y, 300, y, linhaPaint);
        y += 18;
        canvas.drawText(nullSafe(vistoria.getResponsavel()), margemEsquerda, y, textoPaint);

        // Rodapé final
        desenharRodape(canvas, linhaPaint, pequenoPaint, paginaNumero);

        document.finishPage(page);

        File pasta = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (pasta != null && !pasta.exists()) {
            pasta.mkdirs();
        }

        File arquivoPdf = new File(pasta, "laudo_" + vistoria.getProtocolo() + ".pdf");

        FileOutputStream fos = new FileOutputStream(arquivoPdf);
        document.writeTo(fos);
        fos.close();
        document.close();

        return arquivoPdf;
    }

    private static void desenharRodape(Canvas canvas, Paint linhaPaint, Paint pequenoPaint, int paginaNumero) {
        canvas.drawLine(40, 790, 555, 790, linhaPaint);
        canvas.drawText("Documento gerado automaticamente pelo Casa Check", 40, 810, pequenoPaint);
        canvas.drawText("Página " + paginaNumero, 500, 810, pequenoPaint);
    }

    private static String nullSafe(String valor) {
        return valor == null ? "" : valor;
    }

    private static String[] quebrarTexto(String texto, int tamanhoMaximo) {
        if (texto == null || texto.isEmpty()) {
            return new String[]{""};
        }

        int partes = (int) Math.ceil((double) texto.length() / tamanhoMaximo);
        String[] linhas = new String[partes];

        for (int i = 0; i < partes; i++) {
            int inicio = i * tamanhoMaximo;
            int fim = Math.min(inicio + tamanhoMaximo, texto.length());
            linhas[i] = texto.substring(inicio, fim);
        }

        return linhas;
    }
}