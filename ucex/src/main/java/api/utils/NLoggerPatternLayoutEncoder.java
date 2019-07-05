package api.utils;

import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;

/**
 * Created by Tony on 2018-04-20.
 */
//https://m.blog.naver.com/PostView.nhn?blogId=nayasis&logNo=220341605997&proxyReferer=https%3A%2F%2Fwww.google.co.kr%2F
public class NLoggerPatternLayoutEncoder extends PatternLayoutEncoderBase {
    @Override
    public void start() {
        NLoggerPatternLayout patternLayout = new NLoggerPatternLayout();

        patternLayout.setContext(context);
        patternLayout.setPattern(getPattern());
        patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
        patternLayout.start();

        this.layout = patternLayout;

        super.start();
    }
}
