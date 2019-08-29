package api.utils;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.pattern.CallerDataConverter;
import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import ch.qos.logback.classic.pattern.ContextNameConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.pattern.EnsureExceptionHandling;
import ch.qos.logback.classic.pattern.FileOfCallerConverter;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.pattern.MarkerConverter;
import ch.qos.logback.classic.pattern.MethodOfCallerConverter;
import ch.qos.logback.classic.pattern.NopThrowableInformationConverter;
import ch.qos.logback.classic.pattern.PropertyConverter;
import ch.qos.logback.classic.pattern.RelativeTimeConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.pattern.color.BlackCompositeConverter;
import ch.qos.logback.core.pattern.color.BlueCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldBlueCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldCyanCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldGreenCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldMagentaCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldRedCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldWhiteCompositeConverter;
import ch.qos.logback.core.pattern.color.BoldYellowCompositeConverter;
import ch.qos.logback.core.pattern.color.CyanCompositeConverter;
import ch.qos.logback.core.pattern.color.GrayCompositeConverter;
import ch.qos.logback.core.pattern.color.GreenCompositeConverter;
import ch.qos.logback.core.pattern.color.MagentaCompositeConverter;
import ch.qos.logback.core.pattern.color.RedCompositeConverter;
import ch.qos.logback.core.pattern.color.WhiteCompositeConverter;
import ch.qos.logback.core.pattern.color.YellowCompositeConverter;
import ch.qos.logback.core.pattern.parser.Parser;

import java.nio.charset.Charset;
import java.security.Security;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;


/**
 * Created by Tony on 2018-04-20.
 */
//https://m.blog.naver.com/PostView.nhn?blogId=nayasis&logNo=220341605997&proxyReferer=https%3A%2F%2Fwww.google.co.kr%2F

public class NLoggerPatternLayout extends PatternLayoutBase<ILoggingEvent> {
	
    public static final Map<String, String> defaultConverterMap = new HashMap<String, String>();
    public static final String HEADER_PREFIX = "#nexus.log pattern: ";
    
    final boolean IS_ENCRYPT = true; // 암호화 사용: true, 암호화 사용하지않음:false

    static {
        defaultConverterMap.putAll(Parser.DEFAULT_COMPOSITE_CONVERTER_MAP);

        defaultConverterMap.put("d", DateConverter.class.getName());
        defaultConverterMap.put("date", DateConverter.class.getName());

        defaultConverterMap.put("r", RelativeTimeConverter.class.getName());
        defaultConverterMap.put("relative", RelativeTimeConverter.class.getName());

        defaultConverterMap.put("level", LevelConverter.class.getName());
        defaultConverterMap.put("le", LevelConverter.class.getName());
        defaultConverterMap.put("p", LevelConverter.class.getName());

        defaultConverterMap.put("t", ThreadConverter.class.getName());
        defaultConverterMap.put("thread", ThreadConverter.class.getName());

        defaultConverterMap.put("lo", LoggerConverter.class.getName());
        defaultConverterMap.put("logger", LoggerConverter.class.getName());
        defaultConverterMap.put("c", LoggerConverter.class.getName());

//        defaultConverterMap.put("m",       MessageConverter.class.getName());
//        defaultConverterMap.put("msg",     MessageConverter.class.getName()); // TODO
//        defaultConverterMap.put("message", MessageConverter.class.getName());

        defaultConverterMap.put("C", ClassOfCallerConverter.class.getName()); // CallerConverterForClass
        defaultConverterMap.put("class", ClassOfCallerConverter.class.getName());

        defaultConverterMap.put("M", MethodOfCallerConverter.class.getName()); // CallerConverterForMethod
        defaultConverterMap.put("method", MethodOfCallerConverter.class.getName());

        defaultConverterMap.put("L", LineOfCallerConverter.class.getName()); // CallerConverterForLine
        defaultConverterMap.put("line", LineOfCallerConverter.class.getName());

        defaultConverterMap.put("F", FileOfCallerConverter.class.getName());
        defaultConverterMap.put("file", FileOfCallerConverter.class.getName());

        defaultConverterMap.put("X", MDCConverter.class.getName());
        defaultConverterMap.put("mdc", MDCConverter.class.getName());

//        defaultConverterMap.put("ex",            ThrowableProxyConverter.class.getName());
//        defaultConverterMap.put("exception",     ThrowableProxyConverter.class.getName());
//        defaultConverterMap.put("rEx",           ThrowableProxyConverter.class.getName());
//        defaultConverterMap.put("rootException", ThrowableProxyConverter.class.getName());
//        defaultConverterMap.put("throwable",     ThrowableProxyConverter.class.getName());

//        defaultConverterMap.put("xEx",        ExtendedThrowableProxyConverter.class.getName());
//        defaultConverterMap.put("xException", ExtendedThrowableProxyConverter.class.getName());
//        defaultConverterMap.put("xThrowable", ExtendedThrowableProxyConverter.class.getName());

        defaultConverterMap.put("nopex", NopThrowableInformationConverter.class.getName());
        defaultConverterMap.put("nopexception", NopThrowableInformationConverter.class.getName());

        defaultConverterMap.put("cn", ContextNameConverter.class.getName());
        defaultConverterMap.put("contextName", ContextNameConverter.class.getName());

        defaultConverterMap.put("caller", CallerDataConverter.class.getName());

        defaultConverterMap.put("marker", MarkerConverter.class.getName());

        defaultConverterMap.put("property", PropertyConverter.class.getName());

//        defaultConverterMap.put("n", LineSeparatorConverter.class.getName()); //

        defaultConverterMap.put("black", BlackCompositeConverter.class.getName());
        defaultConverterMap.put("red", RedCompositeConverter.class.getName());
        defaultConverterMap.put("green", GreenCompositeConverter.class.getName());
        defaultConverterMap.put("yellow", YellowCompositeConverter.class.getName());
        defaultConverterMap.put("blue", BlueCompositeConverter.class.getName());
        defaultConverterMap.put("magenta", MagentaCompositeConverter.class.getName());
        defaultConverterMap.put("cyan", CyanCompositeConverter.class.getName());
        defaultConverterMap.put("white", WhiteCompositeConverter.class.getName());
        defaultConverterMap.put("gray", GrayCompositeConverter.class.getName());
        defaultConverterMap.put("boldRed", BoldRedCompositeConverter.class.getName());
        defaultConverterMap.put("boldGreen", BoldGreenCompositeConverter.class.getName());
        defaultConverterMap.put("boldYellow", BoldYellowCompositeConverter.class.getName());
        defaultConverterMap.put("boldBlue", BoldBlueCompositeConverter.class.getName());
        defaultConverterMap.put("boldMagenta", BoldMagentaCompositeConverter.class.getName());
        defaultConverterMap.put("boldCyan", BoldCyanCompositeConverter.class.getName());
        defaultConverterMap.put("boldWhite", BoldWhiteCompositeConverter.class.getName());

        defaultConverterMap.put("highlight", HighlightingCompositeConverter.class.getName());
        
        //%d{MM-dd HH:mm:ss.SSS} %-5level [%thread/%logger{35},%method,%L                                                                  ] %msg%n
        //04-20 18:44:59.238     INFO     [com.ericssonlg.uce.app.UCEApplication/c.e.u.s.process.SipProcessSubscribe,procCallBackNotify,119] %PARSER_ERROR[msg]%PARSER_ERROR[n] nexus_sip,
    }

    public NLoggerPatternLayout() {
        this.postCompileProcessor = new EnsureExceptionHandling();
    }

    @Override
    public Map<String, String> getDefaultConverterMap() {
        return defaultConverterMap;
    }

    /** 암호화 시 사용 */
    //private String enc;

/*
	@Override
    public String doLayout(ILoggingEvent iLoggingEvent) {
    	if (!isStarted()) {
           return CoreConstants.EMPTY_STRING;
    	}
 
    	String enc;
        String header = writeLoopOnConverters(iLoggingEvent) + " ";

        StringBuffer sb = new StringBuffer(128);
        StringBuffer buffer = new StringBuffer();
        
		for (char c : iLoggingEvent.getFormattedMessage().toCharArray()) {
            switch (c) {	
                case '\r':
                    continue;

                case '\n':
                    if (NCrypto.IS_ENCRYPT == true) {
                        // 암호화
                        enc = NCrypto.getEncrypted(header.toString() + buffer.toString());
                        sb.append(enc).append(CoreConstants.LINE_SEPARATOR);
                    } else {
                        sb.append(header).append(buffer).append(CoreConstants.LINE_SEPARATOR);
                    }
                    buffer = new StringBuffer();
                    break;

                default:
                    buffer.append(c);
                    break;
            }
        }

		if (NCrypto.IS_ENCRYPT == true) {
			// 암호화
            enc = NCrypto.getEncrypted(header.toString() + buffer.toString());
            sb.append(enc).append(CoreConstants.LINE_SEPARATOR);
        } 
        else {
            sb.append(header).append(buffer).append(CoreConstants.LINE_SEPARATOR);
		}

		return sb.toString();
    }
*/
    
    //2018.11.01 martino 암호화 log event
    //2018.12.07 martino 
	//	static NCrypto.IS_ENCRYPT, NCrypto.getEncrypted()을 사용하면 오동작이 발생
    @Override
    public String doLayout(ILoggingEvent iLoggingEvent) {
    	if (!isStarted()) {
           return CoreConstants.EMPTY_STRING;
    	}      
        
    	String enc;
        String header = writeLoopOnConverters(iLoggingEvent) + " ";
        //System.out.println("Start====>" + this.hashCode() + " @ " + header + "@@@");

        StringBuffer sb = new StringBuffer(128);
        StringBuffer buffer = new StringBuffer();

		for (char c : iLoggingEvent.getFormattedMessage().toCharArray()) {
            switch (c) {	
                case '\r':
                    continue;

                case '\n':
                    if (IS_ENCRYPT == true) {
                        // 암호화
                        enc = getEncrypted(header.toString() + buffer.toString());
                        sb.append(enc).append(CoreConstants.LINE_SEPARATOR);
                    } else {
                        sb.append(header).append(buffer).append(CoreConstants.LINE_SEPARATOR);
                    }

                    buffer = new StringBuffer();
                    break;

                default:
                    buffer.append(c);
                    break;
            }
        }
 
        if (IS_ENCRYPT == true) {
        	// 암호화
            enc = getEncrypted(header.toString() + buffer.toString());
            sb.append(enc).append(CoreConstants.LINE_SEPARATOR);
        } 
        else {
            sb.append(header).append(buffer).append(CoreConstants.LINE_SEPARATOR);
        }

        //System.out.println("End====>" + this.hashCode() + " @ ");
        //2019.01.30 martino return string bug 수정
        return sb.toString();
    }

    final String KEY = "0123456789nexus0123456789nexus01";   // 256비트이므로 32바이트 문자열이 필요
    final String IV = "0123456789nexus0123456789nexus01";    // 256비트이므로 32바이트 문자열이 필요

    public String getEncrypted(String data) {
        try {
        	RijndaelEngine rijndael = new RijndaelEngine(256);
        	CBCBlockCipher cbc_rijndael = new CBCBlockCipher(rijndael);
        	ZeroBytePadding c = new ZeroBytePadding();
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc_rijndael, c);
            CipherParameters keyWithIV = new ParametersWithIV(new KeyParameter(KEY.getBytes()), IV.getBytes());
            cipher.init(true, keyWithIV);
            byte[] plaintext = data.getBytes(Charset.forName("UTF-8"));
            byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
            int offset = 0;
            offset += cipher.processBytes(plaintext, 0, plaintext.length, ciphertext, offset);
            cipher.doFinal(ciphertext, offset);
            String new_text = new String(new Base64().encode(ciphertext), Charset.forName("UTF-8"));
            return new_text;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
    
    @Override
    protected String getPresentationHeaderPrefix() {
        //return super.getPresentationHeaderPrefix();
        return HEADER_PREFIX;
    }
}
