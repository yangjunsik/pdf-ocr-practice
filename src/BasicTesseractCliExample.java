import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


//초급 코드

//OCR이란 => 이미지 속 글자를 컴퓨터가 읽을 수 있는 텍스트로 변환하는 기술이다
//Tesseract란 => OCR을 수행하는 프로그램
//PdfRender => PDF 페이지를 OCR이 가능한 이미지로 바꾸어 주는 도구

//이 명령어에 주목 => teseract input.png output -l kor+eng --psm 6 => 이러한 명령어를 지금 코드로 구현하는것이다
//의미는 input.png파일을 읽어서 output.txt 파일로 만들고 언어는 영어랑 한국어로 OCR을 하고 페이지를 하나의 텍스트 블록처럼 해석하라는 뜻이다
public class BasicTesseractCliExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        //Path는 Java 표준 라이브러리 - 파일/디렉토리 경로 표현 객체
        //Path.of는 문자열을 Path객체로 바꾸어주는 펙토리 메서드임
        Path imagePath = Path.of("sample.png");
        Path outputPath = Path.of("ocr-result");

        //ProcessBuilder => JAVA에서 외부 프로그램을 실행하는 객체
        //inheritIO =>
        //tesseract가 터미널에 출력하는 내용
        //tesseract가 터미널에 에러로 출력하는 내용
        //tesseract가 터미널 입력을 받는 방식
        //이런 것들을 터미널에서 직접 실행한 것처럼 로그를 볼수 있다
        Process process = new ProcessBuilder(
                "tesseract",
                imagePath.toString(),
                outputPath.toString(),
                "-l",
                "kor+eng",
                "--psm",
                "3"
        ).inheritIO()
                .start();

        //waitFor => 프로세스가 끝날때 까지 기다림
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            System.out.println("OCR 실패, Exit code: " + exitCode);
            return;
        }

        Path resultTextPath = Path.of(outputPath + ".txt");
        String text = Files.readString(resultTextPath);

        System.out.println("OCR 결과:");
        System.out.println(text);
    }
}
