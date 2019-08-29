package api.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import api.domain.Rabbit;
import api.utils.StringUtils;

@Component
public class AccessLogInterceptor implements HandlerInterceptor {

	private static Logger logger = LoggerFactory.getLogger(AccessLogInterceptor.class);

	@Autowired
	private Rabbit r;

	@Override
	public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object obj, Exception e)
			throws Exception {
	}

	@Override
	public void postHandle(HttpServletRequest req, HttpServletResponse res, Object obj, ModelAndView m)
			throws Exception {
	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object obj) throws Exception {
		// header 에 TESTMODE: true 라고 명시되어있으면 인증 체크 무시
		// 추후 삭제될 코드
		String test = req.getHeader("TESTMODE");
		logger.debug("TESTMODE : {}", test);
		if ( test != null && test.equalsIgnoreCase("true") ) {
			return true;
		}
		
		// Authorization: IPECS-AUTH user=cshwang, nonce=111111, timestamp=123456, hash=abcdef1234
		String auth = req.getHeader("Authorization");
		logger.debug("Auth : {}", auth);

		try {
			String[] arr = auth.split(",|=");
			if (arr.length != 8) {
				logger.error("Header is wrong.");
				throw new Exception("Header is wrong.");
			}

			String username = "", nonce = "", timestamp = "", hash = "";

			for (int i = 0; i < arr.length; ++i) {
				String t = arr[i].trim();
				if (t.endsWith("user")) {
					username = arr[++i].trim();
				} else if (t.equals("nonce")) {
					nonce = arr[++i].trim();
				} else if (t.equals("timestamp")) {
					timestamp = arr[++i].trim();
				} else if (t.equals("hash")) {
					hash = arr[++i].trim();
				}
			}
			
			if ( username.isEmpty() || nonce.isEmpty() || timestamp.isEmpty() || hash.isEmpty() ) {
				logger.error("Header value is missed.");
				throw new Exception("Header value is missed.");
			}

			String password_hash = r.getLoginInfo().getHash();
			logger.info("calculate hash INPUT - {}", username + ":" + nonce + ":" + timestamp + ":" + password_hash);
			String calculated_hash = StringUtils.getSHA1Hex(username + ":" + nonce + ":" + timestamp + ":" + password_hash);
			if (!hash.equals(calculated_hash)) {
				logger.info("received   hash : {}", hash );
				logger.info("calculated hash : {}", calculated_hash );
				throw new Exception("Hash Code is Not Matched.");
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.toString());
			return false;
		}
		return true;
	}

}
