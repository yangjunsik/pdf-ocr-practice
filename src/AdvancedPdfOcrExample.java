import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class AdvancedPdfOcrExample {
    private static final int OCR_DPI = 200;
    private static final Duration OCR_TIMEOUT = Duration.ofSeconds(30);

    public static void main(String[] args) throws Exception {
        File pdfFile = new File("sample.pdf");

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);

            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                String text = ocrPdfPage(renderer, pageIndex);

                System.out.println("Page " + (pageIndex + 1));
                System.out.println(text);
                System.out.println();
            }
        }
    }

    private static String ocrPdfPage(PDFRenderer renderer, int pageIndex) throws Exception {
        Path tempDirectory = Files.createTempDirectory("temp");
        Path imagePath = tempDirectory.resolve("page.png");
        Path outputPath = tempDirectory.resolve("ocr-output");
        Path logPath = tempDirectory.resolve("tesseract.log");

        try{
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, OCR_DPI, ImageType.RGB);
            ImageIO.write(image, "png", imagePath.toFile());

            Process process = new ProcessBuilder(
                    "tesseract",
                    imagePath.toString(),
                    outputPath.toString(),
                    "-l",
                    "kor+eng",
                    "--psm",
                    "6"
            ).redirectErrorStream(true)
                    .redirectOutput(logPath.toFile())
                    .start();

            boolean finished = process.waitFor(OCR_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "";
            }

            if (process.exitValue() != 0) {
                String log = Files.exists(logPath) ? Files.readString(logPath) : "";
                System.out.println("OCR 실패 로그:");
                System.out.println(log);
                return "";
            }

            Path resultPath = Path.of(outputPath + ".txt");
            return Files.exists(resultPath) ? Files.readString(resultPath).trim() : "";
        } finally {
            deleteIfExists(imagePath);
            deleteIfExists(Path.of(outputPath.toString() + ".txt"));
            deleteIfExists(logPath);
            deleteIfExists(tempDirectory);
        }
    }


    private static void deleteIfExists(Path path) {
        try{
            Files.deleteIfExists(path);
        } catch (Exception e) {
            System.out.println("삭제 실패: " + path);
        }
    }
}
