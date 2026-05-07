import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

//증급 코드

//실무에서는 OCR이 멈출 수 도 있고, 실패 로그도 봐야해서 timeout과 log파일을 둔다
public class IntermediateTesseractExample {
    private static final Duration OCR_TIMEOUT = Duration.ofSeconds(30);

    public static void main(String[] args) {
        Path imagePath = Path.of("sample.png");
        Path outputPath = Path.of("ocr-result");
        Path logPath = Path.of("ocr-log");

        try {
            Process process = new ProcessBuilder(
                    "tesseract",
                    imagePath.toString(),
                    outputPath.toString(),
                    "-l",
                    "kor+eng",
                    "--psm",
                    "3"
            ).redirectErrorStream(true)
                    .redirectOutput(logPath.toFile())
                    .start();

            boolean finished = process.waitFor(OCR_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                System.out.println("OCR timeout");
                return;
            }

            if (process.exitValue() != 0) {
                String log = Files.exists(logPath) ? Files.readString(logPath) : "";
                System.out.println("OCR failed");
                System.out.println(log);
                return;
            }

            Path resultTextPath = Path.of(outputPath + ".txt");
            String text = Files.exists(resultTextPath) ? Files.readString(resultTextPath) : "";

            System.out.println("OCR 결과:");
            System.out.println(text);

        } catch (IOException e) {
            System.out.println("tesseract 살향 살퍄, 설치 여부 확인 필요");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("OCR 대기 중 interrupt 발생");
        }
    }
}
