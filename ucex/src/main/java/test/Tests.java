package test;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
@Component
public class Tests {
    @Value("${ncrypto.option}")
    private String ncrypto_option;
    public String getNcrypto_option() {
        return ncrypto_option;
    }
    public void setNcrypto_option(String ncrypto_option) {
        this.ncrypto_option = ncrypto_option;
    }
}
