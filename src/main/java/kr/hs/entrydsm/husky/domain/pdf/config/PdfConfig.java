package kr.hs.entrydsm.husky.domain.pdf.config;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.layout.font.FontProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PdfConfig {

    @Bean
    public TemplateEngine getTemplateEngineBean() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }

    public static ConverterProperties createConverterProperties() {
        ConverterProperties properties = new ConverterProperties();
        FontProvider fontProvider = new DefaultFontProvider(false, false, false);

        List.of(
                "/fonts/KoPubWorld_Dotum_Light.ttf",
                "/fonts/KoPubWorld_Dotum_Bold.ttf",
                "/fonts/KoPubWorld_Dotum_Medium.ttf",
                "/fonts/DejaVuSans.ttf")
                .forEach(font -> {
                    try {
                        FontProgram fontProgram = FontProgramFactory.createFont(font);
                        fontProvider.addFont(fontProgram);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        properties.setFontProvider(fontProvider);
        return properties;
    }

}
