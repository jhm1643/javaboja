package api.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import api.config.RabbitMQProperties;
import api.config.SystemBackupConfig;
import api.config.UcexTestProperties;
import api.dao.ApiV1Dao;
import api.domain.AddressBook;
import api.domain.Extension;
import api.domain.ExtensionCommand;
import api.domain.ExtensionData;
import api.domain.Madn;
import api.domain.MadnCommand;
import api.domain.MemberFailData;
import api.domain.MadnMemberRequest;
import api.domain.MemberResponse;
import api.domain.NumberTenant;
import api.domain.NumberTenantType;
import api.domain.Rabbit;
import api.domain.Site;
import api.domain.Tenant;
import api.domain.UcexLogin;
import api.domain.UserInfo;
import api.domain.UserMemberRequest;
import api.domain.backup.SystemBackup;
import api.domain.backup.SystemBackupResponse;
import api.process.BackupDataTask;
import api.process.IDataBackupTask;
import api.utils.StringUtils;
import api.utils.UCHistory;

@EnableConfigurationProperties(SystemBackupConfig.class)
@RestController
public class ApiV1Controller {

	@Autowired
	private Rabbit r;

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	private UcexTestProperties testMode;
	
	@Autowired
	private ThreadPoolTaskExecutor taskExecutator;
	
	@Autowired
	private RabbitMQProperties rabbitMQProperties;
	
	@Autowired
	private SystemBackupConfig systemBackupConfig;

	private static Logger logger = LoggerFactory.getLogger(ApiV1Controller.class);

	private static HttpClient client;
	
	private static MultiThreadedHttpConnectionManager httpConnectionManager;
	
	//2019.01.15 martino EMS로부터 오는 정보중에 '|'가 포함되어 있어서 DELIMITER 사용불가하여 변경
	//private final String DELIMITER = "|"; 
	private final String DELIMITER = "\u0002";
	
	private static BackupDataTask	emsBackupTask = null;
	
	/*
	 * { "login_password":" dc17a3d072a2833536fadc1223d580ab",
	 * "client_ip":"192.168.123.101", "timestamp":"12345" }
	 */
	@RequestMapping(value = "/ucex/api/v1/users/admin/login", method = RequestMethod.POST)
	public UcexLogin login(@RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#users/admin/login : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		UcexLogin param = null;
		try {
			try {
				param = mapper.readValue(data, UcexLogin.class);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			String username = "ems_uce_user";
			String password = "ems_uce_password";

			// String username = "aaa";
			// String password = "pass";

			String nonce = java.util.UUID.randomUUID().toString();
			String timestamp = param.getTimestamp();

			String password_hash = StringUtils.getMD5(username + ":" + password);
			String timestamp_hash = StringUtils.getMD5(param.getTimestamp());
			String login_password = StringUtils.getXOR(password_hash, timestamp_hash);

			logger.info(" login_password should be : {}", login_password);
			if (login_password.equals(param.getLogin_password())) {

				// 인증 성공
				String hash = StringUtils.getSHA1Hex(username + ":" + nonce + ":" + timestamp + ":" + password_hash);
				param.setHash(hash);

				// 서버 리스트
//				List<String> server_list = new ArrayList<String>();
//				server_list.add("192.168.147.36");
				
				param.setServer_list(testMode.getServerlist());

				// POLL interval
				param.setPoll_interval("60");

				r.setLoginInfo(param);

//				r.sendMessageO("login", mapper.writeValueAsString(param));

			} else {
				// 인증실패
				throw new Exception("login : Faield to Authorization.");
			}

		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.toString());
			logger.error(e.getMessage(), e);
		}

		return param;
	}

	@RequestMapping(value = "/ucex/api/v1/users/admin/logout", method = RequestMethod.POST)
	public void logout() throws Exception {
		logger.info("logout : {}", r.getLoginInfo() == null ? null : r.getLoginInfo().getClient_ip());
		r.setLoginInfo(null);

		// r.sendMessage("logout", null);
	}

	@RequestMapping(value = "/ucex/api/v1/users/admin/checkconnection", method = RequestMethod.GET)
	public String checkconnection(@RequestBody String data) {

		logger.info("#checkconnection : {}", data);
		return data;
	}

	// 내선번호 신규 생성
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/ext/{dn}", method = RequestMethod.POST)
	public void createExtension(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#createExtension tenant:{} / ext:{}", tenant, dn);
		logger.info("#createExtension : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		ExtensionData d = null;

		try {
			try {
				d = mapper.readValue(data, ExtensionData.class);
				d.setCm(cm);
				d.setDn(dn);
				d.setTenant(tenant);

				String strOrgAuthID = d.getAuth_id();
				String strOrgAuthPass = d.getAuth_password();

				// JASON 문자에 \u0003 이 입력되어서, 메모리에 0x03 이 추가되어 uce server 전체 down 되는 현상에 대한 대비책
				// EMS 에서도 수정하겠다고 했었음. 2017.12
				if( Pattern.matches( "^[ -~]*$", strOrgAuthID)==false )
				{
					logger.error("#ERROR ext:{} auth_id '{}' is not escaped strings!!!", dn, strOrgAuthID);
					logger.error("#ERROR ext:{} Extension Creation Failed", dn);
					res.sendError(HttpServletResponse.SC_BAD_REQUEST, "auth_id is not escaped strings!!!");
					return;
				}

				// JASON 문자에 \u0003 이 입력되어서, 메모리에 0x03 이 추가되어 uce server 전체 down 되는 현상에 대한 대비책
				// EMS 에서도 수정하겠다고 했었음. 2017.12
				if( Pattern.matches( "^[ -~]*$", strOrgAuthPass)==false )
				{
					logger.error("#ERROR ext:{} auth_password '{}' is not escaped strings!!!", dn, strOrgAuthPass);
					logger.error("#ERROR ext:{} Extension Creation Failed", dn);
					res.sendError(HttpServletResponse.SC_BAD_REQUEST, "auth_password is not escaped strings!!!");
					return;
				}

				// UCE 와 CLOUD 단말타입이 다르므로 변환시켜준다
				switchTermType(d);

			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}
			
			NumberTenant nt = new NumberTenant();
			nt.setCm(cm);
			nt.setNumber(dn);
			nt.setTenant(tenant);

			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			ExtensionData dd = userDao.getExtensionInfo(nt);
			
			if ( dd != null ) {
				throw new Exception("Already Exist Extension " + dn);
			}

			//
			//r.sendMessageO("createExtension", mapper.writeValueAsString(param));
			
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", d.getCm());
			param.put("tenant", d.getTenant());
			String tenant_prefix = userDao.getTenantPrefix(param);
			
			
			
			if (testMode.getMode().equals("true")) {

				userDao.createExtension(d);
			}
			else {
				
				StringBuffer sb = new StringBuffer();
				sb.append("EXTENSION").append(DELIMITER).append("I").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
				sb.append(convertUCEMedia(d.getCm())).append(DELIMITER).append(d.getTenant()).append(DELIMITER).append(d.getDn()).append(DELIMITER);
				sb.append(d.getTerm_type()).append(DELIMITER).append(d.getNumber_type()).append(DELIMITER);
				sb.append(d.getIp()).append(DELIMITER).append(d.getMac()).append(DELIMITER);
				sb.append(d.getUser_id()).append(DELIMITER).append(d.getOutgoing_cid()).append(DELIMITER);
				sb.append(d.getAuth_id()).append(DELIMITER).append(d.getAuth_password()).append(DELIMITER).append(d.getScti_usage()).append(DELIMITER);
				sb.append(tenant_prefix).append("\0");
				
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				
				// nt.setCm(convertUCEMedia(cm));
				for ( int i = 0; i < 25 && dd == null; i++) {
					Thread.sleep(200);
					dd = userDao.getExtensionInfo(nt);
					if ( dd != null) {
						break;
					}
				}
				
				if ( dd == null ) {
					// 내선 생성 실패 했으므로 삭제 command 날려 줌
					sb.setLength(0);
					sb.append("EXTENSION").append(DELIMITER).append("D").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
					sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(tenant_prefix+dn).append("\0");
					
					try {
						r.sendCubeMessage(sb.toString());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					throw new Exception("Extension Creation Failed");
				}
			}

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	private void switchTermType(ExtensionData d) {
		switch (d.getTerm_type()) {
		case "2314":
			d.setTerm_type("2326");
			break;
		case "2371":
			d.setTerm_type("2323");
			break;
		}
		
	}
	
	private void reverseSwitchTermType(ExtensionData d) {
		switch (d.getTerm_type()) {
		case "2326":
			d.setTerm_type("2314");
			break;
		case "2323":
			d.setTerm_type("2371");
			break;
		}		
	}

	// 내선번호 조회
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/ext/{dn}", method = RequestMethod.GET)
	public Extension viewExtension(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#viewExtension tenant:{} / ext:{}", tenant, dn);

		NumberTenant nt = new NumberTenant();
		nt.setCm(cm);
		nt.setNumber(dn);
		nt.setTenant(tenant);

		Extension param = new Extension();

		ExtensionCommand com = new ExtensionCommand();
		com.setTenant(tenant);
		com.setNumber(dn);

		param.setCommand_data(com);

		try {
			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			//nt.setCm(convertUCEMedia(cm));
			ExtensionData data = dao.getExtensionInfo(nt);
			
			if (data == null) {
				throw new Exception("Not Exist Extension");
			}
			
			// UCE 와 CLOUD 단말타입이 다르므로 변환시켜준다
			reverseSwitchTermType(data);
			data.setCm(cm);
			com.setData(data);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
		
		return param;
	}

	// 내선번호 파라미터 변경
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/ext/{dn}", method = RequestMethod.PUT)
	public void modifyExtension(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#modifyExtension tenant:{} / ext:{}", tenant, dn);
		logger.info("#modifyExtension : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		ExtensionData d = null;

		try {
			try {
				d = mapper.readValue(data, ExtensionData.class);
				d.setCm(cm);
				d.setTenant(tenant);
				d.setDn(dn);

			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			//r.sendMessageCTNO("modifyExtension", cm, tenant, dn, mapper.writeValueAsString(param));
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", d.getCm());
			p.put("tenant", d.getTenant());
			String tenant_prefix = userDao.getTenantPrefix(p);
			
			NumberTenant nt = new NumberTenant();
			nt.setCm(cm);
			nt.setNumber(dn);
			nt.setTenant(tenant);

			ExtensionData dd = userDao.getExtensionInfo(nt);
			if ( dd==null ) {
				throw new Exception("Not Exist Extension");
			}
			
			if (testMode.getMode().equals("true")) {

				userDao.modifyExtension(d);
			}
			else {
				
				// UCE 와 CLOUD 단말타입이 다르므로 변환시켜준다
				switchTermType(d);
				
				StringBuffer sb = new StringBuffer();
				sb.append("EXTENSION").append(DELIMITER).append("U").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
				sb.append(convertUCEMedia(d.getCm())).append(DELIMITER).append(d.getTenant()).append(DELIMITER).append(d.getDn()).append(DELIMITER);
				sb.append(d.getTerm_type()).append(DELIMITER).append(d.getNumber_type()).append(DELIMITER);
				sb.append(d.getIp()).append(DELIMITER).append(d.getMac()).append(DELIMITER);
				sb.append(d.getUser_id()).append(DELIMITER).append(d.getOutgoing_cid()).append(DELIMITER);
				sb.append(d.getAuth_id()).append(DELIMITER).append(d.getAuth_password()).append(DELIMITER).append(d.getScti_usage()).append(DELIMITER);
				sb.append(tenant_prefix).append("\0");
				
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}

	}

	// 내선번호 삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/ext/{dn}", method = RequestMethod.DELETE)
	public void deleteExtension(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#deleteExtension cm:{}", cm);
		logger.info("#deleteExtension tenant:{} / ext:{}", tenant, dn);

		try {
			//
			//			r.sendMessageCTN("deleteExtension", cm, tenant, dn);
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = userDao.getTenantPrefix(p);
			
			NumberTenant nt = new NumberTenant();
			nt.setCm(cm);
			nt.setNumber(dn);
			nt.setTenant(tenant);

			ExtensionData d = userDao.getExtensionInfo(nt);
			
			if ( d==null ) {
				throw new Exception("Not Exist Extension");
			}
						
			if (testMode.getMode().equals("true")) {

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("cm", cm);
				param.put("tenant", tenant);
				param.put("extension", dn);

				userDao.deleteExtension(param);
			}
			else {
				
				StringBuffer sb = new StringBuffer();
				sb.append("EXTENSION").append(DELIMITER).append("D").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
				sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(tenant_prefix+dn).append("\0");
				
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	private String convertUCEMedia(String cm) {
		ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		return dao.convertUCEMedia(cm);
	}

	// MADN 신규 생성
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/madn/{dn}", method = RequestMethod.POST)
	public void createMADN(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#createMADN cm:{}", cm);
		logger.info("#createMADN tenant:{} / ext:{}", tenant, dn);

		try {
			//
			//r.sendMessageCTN("createMADN", cm, tenant, dn);
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = userDao.getTenantPrefix(p);
			
			MadnCommand mc = new MadnCommand();
			mc.setCm(cm);
			mc.setMadn(dn);
			mc.setTenant(tenant);
			
			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			MadnCommand data = dao.getMadnInfo(mc);
			
			if (data != null) {
				throw new Exception("Already Exist MADN");
			}
			
			if (testMode.getMode().equals("true")) {

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("cm", cm);
				param.put("tenant", tenant);
				param.put("madn", dn);
				
				userDao.createMADN(param);
			}
			else {
				StringBuffer sb = new StringBuffer();
				sb.append("MADN").append(DELIMITER).append("I").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
				sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(dn).append(DELIMITER).append(tenant_prefix).append("\0");
				
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				
				mc.setCm(cm);
				for ( int i=0; i<5 && data==null ; i++) {
					Thread.sleep(200);
					data = dao.getMadnInfo(mc);
					if ( data != null ) {
						break;
					}
				}
				
				if ( data == null ) {
					// MADN 생성 실패 했으므로 삭제 command 날려줌
					sb.setLength(0);
					sb.append("MADN").append(DELIMITER).append("D").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
					sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(dn).append(DELIMITER).append(tenant_prefix).append("\0");
					
					try {
						r.sendCubeMessage(sb.toString());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					
					throw new Exception("MADN Creation Failed");
				}
			}
			
			res.sendError(HttpServletResponse.SC_CREATED);


		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	// MADN 조회
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/madn/{dn}", method = RequestMethod.GET)
	public Madn viewMADN(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#viewMADN cm:{}", cm);
		logger.info("#viewMADN tenant:{} / ext:{}", tenant, dn);

		Madn param = null;
		
		try {

			param = new Madn();

			//
			MadnCommand mc = new MadnCommand();
			mc.setCm(cm);
			mc.setMadn(dn);
			mc.setTenant(tenant);
			
			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			MadnCommand data = dao.getMadnInfo(mc);
			
			if (data == null) {
				throw new Exception("Not Exist MADN");
			}
			
			data.setCm(cm);
			param.setCommand_data(data);

		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}

		return param;
	}

	// MADN 삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/madn/{dn}", method = RequestMethod.DELETE)
	public void deleteMADN(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#deleteMADN cm:{}", cm);
		logger.info("#deleteMADN tenant:{} / ext:{}", tenant, dn);

		try {
			//
			//r.sendMessageCTN("deleteMADN", cm, tenant, dn);
			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = dao.getTenantPrefix(p);
			
			MadnCommand mc = new MadnCommand();
			mc.setCm(cm);
			mc.setMadn(dn);
			mc.setTenant(tenant);
			
			MadnCommand data = dao.getMadnInfo(mc);
			
			if (data == null) {
				throw new Exception("Not Exist MADN");
			}
			
			if (testMode.getMode().equals("true")) {

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("cm", cm);
				param.put("tenant", tenant);
				param.put("madn", dn);
				
				ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
				userDao.deleteMADN(param);
			}
			else {
				StringBuffer sb = new StringBuffer();
				sb.append("MADN").append(DELIMITER).append("D").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
				sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(dn).append(DELIMITER).append(tenant_prefix).append("\0");
				
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}	
			}

		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	// MADN MEMBER 추가
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/madn/{dn}/member", method = RequestMethod.POST)
	public MemberResponse addMemberMADN(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#addMemberMADN tenant:{} / ext:{}", tenant, dn);
		logger.info("#addMemberMADN cm:{}", cm);

		ObjectMapper mapper = new ObjectMapper();

		MadnMemberRequest mmr = null;
		MemberResponse ret = null;
		MemberFailData fData = null;

		try {
			try {
				mmr = mapper.readValue(data, MadnMemberRequest.class);
			} catch (Exception e1) {
				throw new Exception("login : Wrong JSON Format.");
			}

			MadnCommand mc = new MadnCommand();
			mc.setCm(cm);
			mc.setMadn(dn);
			mc.setTenant(tenant);
			
			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			MadnCommand data1 = dao.getMadnInfo(mc);
			
			if (data1 == null) {
				throw new Exception("Not Exist MADN");
			}
			
			ret = new MemberResponse();
			fData = new MemberFailData();
			ret.setCommand_data(fData);

			//

			//r.sendMessageCTNO("addMemberMADN", cm, tenant, dn, mapper.writeValueAsString(param));
			
			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = dao.getTenantPrefix(p);
			
			
			List<ExtensionCommand> l = mmr.getData();
			for (ExtensionCommand ec : l) {

				if (testMode.getMode().equals("true")) {

					Map<String, Object> param = new HashMap<String, Object>();
					param.put("cm", cm);
					param.put("tenant", tenant);
					param.put("madn", dn);
					param.put("sadn", ec.getNumber());
					
					ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
					userDao.addMemberMADN(param);
				}
				else {
					StringBuffer sb = new StringBuffer();
					sb.append("SADN").append(DELIMITER).append("I").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
					sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(tenant_prefix+dn)
					.append(DELIMITER).append(tenant_prefix+ec.getNumber()).append("\0");
					
					try {
						r.sendCubeMessage(sb.toString());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_CREATED, e.toString());
			logger.error(e.getMessage(), e);
		}

		return ret;
	}

	// MADN MEMBER 삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/madn/{dn}/member", method = RequestMethod.PUT)
	public MemberResponse deleteMemberMADN(@PathVariable("tenant") String tenant, @PathVariable("dn") String dn,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#deleteMemberMADN cm:{}", cm);
		logger.info("#deleteMemberMADN tenant:{} / ext:{}", tenant, dn);

		ObjectMapper mapper = new ObjectMapper();

		MadnMemberRequest mmr = null;
		MemberResponse ret = null;
		MemberFailData fData = null;

		try {
			try {
				mmr = mapper.readValue(data, MadnMemberRequest.class);
			} catch (Exception e1) {
				throw new Exception("login : Wrong JSON Format.");
			}

			ret = new MemberResponse();
			fData = new MemberFailData();
			ret.setCommand_data(fData);

			//
			//r.sendMessageCTNO("deleteMemberMADN", cm, tenant, dn, mapper.writeValueAsString(param));
			
			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = dao.getTenantPrefix(p);
			
			
			List<ExtensionCommand> l = mmr.getData();
			for (ExtensionCommand ec : l) {

				if (testMode.getMode().equals("true")) {

					Map<String, Object> param = new HashMap<String, Object>();
					param.put("cm", cm);
					param.put("tenant", tenant);
					param.put("madn", dn);
					param.put("sadn", ec.getNumber());
					
					dao.deleteMemberMADN(param);
				}
				else {
					StringBuffer sb = new StringBuffer();
					sb.append("SADN").append(DELIMITER).append("D").append(DELIMITER).append("1").append(DELIMITER).append("1").append(DELIMITER);
					sb.append(convertUCEMedia(cm)).append(DELIMITER).append(tenant).append(DELIMITER).append(tenant_prefix+dn)
					.append(DELIMITER).append(tenant_prefix+ec.getNumber()).append("\0");
					
					try {
						r.sendCubeMessage(sb.toString());
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}				
				}
				
			}

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_OK, e.toString());
			logger.error(e.getMessage(), e);
		}

		return ret;
	}

	/*
	private String getMediaTeanant(String cm, String tenant) {
		return String.format("1%03d%04d", Integer.parseInt(cm), Integer.parseInt(tenant));
	}
	 */

	// USER 정보 추가
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/user/{userid:.+}", method = RequestMethod.POST)
	public void createUser(@PathVariable("tenant") String tenant, @PathVariable("userid") String userid,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#createUser cm:{}", cm);
		logger.info("#createUser tenant:{} / userid:{}", tenant, userid);

		userid = userid.toLowerCase();
		
		ObjectMapper mapper = new ObjectMapper();

		UserInfo u = new UserInfo();

		try {
			try {
				u = mapper.readValue(data, UserInfo.class);
			} catch (Exception e1) {
				throw new Exception("login : Wrong JSON Format.");
			}

			addOpenfireAccount(userid, u.getPassword());
			//
			//	r.sendMessageCTNO("createUser", cm, tenant, userid, mapper.writeValueAsString(param));
			u.setCm(cm);
			u.setTenant(tenant);
			u.setUserid(userid.replaceAll("@", "\\\\40"));

			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			userDao.createUser(u);
			userDao.createUserOption(u);

			
			logHistoryTable("uc_user", u.getUserid(), UCHistory.HISTORY_TYPE.INSERT);
			logHistoryTable("uc_user_option", u.getUserid(), UCHistory.HISTORY_TYPE.INSERT);

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	private void addOpenfireAccount(String username, String password) {
		
		if ( password == null || password.isEmpty())
			password = "1111";
		try {
			password = URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		// 1. openfire Roster 등록  : userservice Plugin을 호출
		
		String pushURL = "http://"+ rabbitMQProperties.getHost() +":9090/plugins/userService/userservice?type=add&password=" + password + "&secret=UCEServer";
		
		HttpClient client = getThreadSafeClient();
		pushURL += "&username=" + username;
		PostMethod post = new PostMethod(pushURL);
		post.addRequestHeader("Content-type", "text/html; charset=UTF-8");
		
		logger.info("OPENFIRE ADD : {}", pushURL);
		PushThreadTask ptt = new PushThreadTask();
	    ptt.setEnv(client, post);
	    taskExecutator.execute(ptt);
	}
	
	private void deleteOpenfireAccount(String username) {
		String pushURL = "http://"+ rabbitMQProperties.getHost() +":9090/plugins/userService/userservice?type=delete&secret=UCEServer";

		HttpClient client = getThreadSafeClient();
		pushURL += "&username=" + username;
		PostMethod post = new PostMethod(pushURL);
		post.addRequestHeader("Content-type", "text/html; charset=UTF-8");

		logger.info("OPENFIRE DEL : {}", pushURL);
		PushThreadTask ptt = new PushThreadTask();
		ptt.setEnv(client, post);
		taskExecutator.execute(ptt);
		
		ApiV1Dao pushDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		pushDao.deleteUserRelation(username);
	}
	
	private void updateOpenfireAccount(String username, String password) {
		try {
			logger.info("PASSWORD PLAIN : [" + password+"]");
			password = URLEncoder.encode(password, "UTF-8");
			logger.info("PASSWORD ENCODED : [" + password+"]");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		
		// 1. openfire Roster 등록  : userservice Plugin을 호출
		String pushURL = "http://"+ rabbitMQProperties.getHost() +":9090/plugins/userService/userservice?type=update&secret=UCEServer";
		
		HttpClient client = getThreadSafeClient();
		pushURL += "&username=" + username;
		pushURL += "&password=" + password;
		PostMethod post = new PostMethod(pushURL);
		post.addRequestHeader("Content-type", "text/html; charset=UTF-8");
		
		logger.info("OPENFIRE UPDATE : {}", pushURL);
		PushThreadTask ptt = new PushThreadTask();
	    ptt.setEnv(client, post);
	    taskExecutator.execute(ptt);
	}


	private synchronized static HttpClient getThreadSafeClient() {
		if ( client != null ) return client;
		
		httpConnectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = httpConnectionManager.getParams();
		params.setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
		params.setConnectionTimeout(3000);
		params.setSoTimeout(3000);
		params.setMaxTotalConnections(100);
		
		httpConnectionManager.setParams(params);
		
		client = new HttpClient(httpConnectionManager);
		
		return client;
	}
	
	// openfire 에 http call 하는 Thread
	private class PushThreadTask implements Runnable {
		
		private HttpClient client;
		private PostMethod post;
		
		public void setEnv(HttpClient c, PostMethod p) {
			client = c;
			post = p;
		}
		
		@Override
		public void run() {
			try {
				int response = client.executeMethod(post);
		
				System.out.println("-------");
				System.out.println("OPENFIRE CALL STATUS : " + response + ((response == HttpStatus.SC_OK) ? " [성공]" : "[실패]"));
			
			} catch ( Exception e ) {
				logger.error(e.getMessage(), e);
				System.out.println("OPENFIRE CALL FAILED : " + e.toString() );
			} finally {
				post.releaseConnection();
			}
		}		
	}
	

	// USER 정보 변경
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/user/{userid:.+}", method = RequestMethod.PUT)
	public void modifyUser(@PathVariable("tenant") String tenant, @PathVariable("userid") String userid,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#modifyUser cm:{}", cm);
		logger.info("#modifyUser tenant:{} / userid:{}", tenant, userid);
		logger.debug("#RequestBody : {}",data);
		userid = userid.toLowerCase();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true );

		UserInfo u = null;

		try {
			try {
				u = mapper.readValue(data, UserInfo.class);
			} catch (Exception e1) {
				throw new Exception("login : Wrong JSON Format.");
			}

			//
			//			r.sendMessageCTNO("modifyUser", cm, tenant, userid, mapper.writeValueAsString(param));
			if ( u.getPassword() != null) {
				updateOpenfireAccount(userid, u.getPassword());
			}
			
			u.setCm(cm);
			u.setTenant(tenant);
			u.setUserid(userid.replaceAll("@", "\\\\40"));

			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			
			int userCount = userDao.getUserInfoCount(u);
			
			if ( userCount < 1) {
				throw new Exception("Not Exist User");
			}
			
			userDao.modifyUser(u);
			userDao.modifyUserOption(u);

			

			logHistoryTable("uc_user", u.getUserid().replaceAll("@", "\\\\40"), UCHistory.HISTORY_TYPE.UPDATE);
			logHistoryTable("uc_user_option", u.getUserid().replaceAll("@", "\\\\40"), UCHistory.HISTORY_TYPE.UPDATE);

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	// USER 정보 삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/user/{userid:.+}", method = RequestMethod.DELETE)
	public void deleteUser(@PathVariable("tenant") String tenant, @PathVariable("userid") String userid,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#deleteUser tenant:{}", cm);
		logger.info("#deleteUser tenant:{} / userid:{}", tenant, userid);

		userid = userid.toLowerCase();
		
		try {

			//
			// r.sendMessageCTN("deleteUser", cm, tenant, userid);
			
			Map<String, Object> bo = new HashMap<String, Object>();
			bo.put("cm", cm);
			bo.put("tenant", tenant);
			bo.put("userid", userid.replaceAll("@", "\\\\40"));
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			UserInfo u = new UserInfo();
			u .setCm(cm);
			u.setTenant(tenant);
			u.setUserid(userid.replaceAll("@", "\\\\40"));

			int userCount = userDao.getUserInfoCount(u);
			
			if ( userCount < 1) {
				throw new Exception("Not Exist User");
			}
			
			deleteOpenfireAccount(userid);
			
			userDao.deleteUser(bo);
			userDao.deleteUserOption(bo);

			// 즐겨찾기에서 삭제
			userDao.deleteUserRelation(bo);
			
			// 내선번호에서 삭제
			userDao.updateUserDelteExtension(bo);
			
			// 메모, 채팅 관련 사용자 데이터 삭제
			userDao.procedureRetiredUser(bo);
			bo.put("etc","");
			logger.info("procedureRetiredUser RETURNED : {}", bo.get("etc"));
			
			logHistoryTable("uc_user", userid.replaceAll("@", "\\\\40"), UCHistory.HISTORY_TYPE.DELETE);
			logHistoryTable("uc_user_option", userid.replaceAll("@", "\\\\40"), UCHistory.HISTORY_TYPE.DELETE);

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// USER 내선 추가
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/user/{userid:.+}/member", method = RequestMethod.POST)
	public MemberResponse addUserExtension(@PathVariable("tenant") String tenant, @PathVariable("cm") String cm,
			@PathVariable("userid") String userid, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#addUserExtension cm:{}", cm);
		logger.info("#addUserExtension tenant:{} / userid:{}", tenant, userid);

		userid = userid.toLowerCase();
		
		ObjectMapper mapper = new ObjectMapper();

		MemberResponse ret = null;
		MemberFailData fData = null;
		UserMemberRequest r = null;

		try {
			try {
				r = mapper.readValue(data, UserMemberRequest.class);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			ret = new MemberResponse();
			fData = new MemberFailData();
			ret.setCommand_data(fData);

			//
			//r.sendMessageCTNO("addUserExtension", cm, tenant, userid, mapper.writeValueAsString(param));
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			UserInfo u = new UserInfo();
			u .setCm(cm);
			u.setTenant(tenant);
			u.setUserid(userid.replaceAll("@", "\\\\40"));

			int userCount = userDao.getUserInfoCount(u);
			
			if ( userCount < 1) {
				throw new Exception("Not Exist User");
			}

			List<NumberTenantType> list = r.getData();

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("userid", userid.replaceAll("@", "\\\\40"));
			param.put("cm", cm);
			
			
			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = userDao.getTenantPrefix(p);
			
			for ( NumberTenantType n : list ) {
				param.put("extension_id", n.getNumber());
				param.put("tenant", n.getTenant());
				
				userDao.addUserExtension(param);
				
				logHistoryTable("uc_extension",tenant_prefix + n.getNumber(), UCHistory.HISTORY_TYPE.UPDATE);
			}

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}

		return ret;
	}

	// USER 내선 삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/user/{userid:.+}/member", method = RequestMethod.PUT)
	public MemberResponse deleteUserExtension(@PathVariable("tenant") String tenant, @PathVariable("cm") String cm,
			@PathVariable("userid") String userid, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#deleteUserExtension cm:{}", cm);
		logger.info("#deleteUserExtension tenant:{} / userid:{}", tenant, userid);

		userid = userid.toLowerCase();
		
		ObjectMapper mapper = new ObjectMapper();

		MemberResponse ret = null;
		MemberFailData fData = null;
		UserMemberRequest r = null;

		try {
			try {
				r = mapper.readValue(data, UserMemberRequest.class);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			UserInfo u = new UserInfo();
			u.setCm(cm);
			u.setTenant(tenant);
			u.setUserid(userid.replaceAll("@", "\\\\40"));

			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			int userCount = userDao.getUserInfoCount(u);
			
			if ( userCount < 1) {
				throw new Exception("Not Exist User");
			}

			
			ret = new MemberResponse();
			fData = new MemberFailData();
			ret.setCommand_data(fData);

			//
			//r.sendMessageCTNO("deleteUserExtension", cm, tenant, userid, mapper.writeValueAsString(param));
			
			
			List<NumberTenantType> list = r.getData();
			
			Map<String, Object> p = new HashMap<String, Object>();
			p.put("cm", cm);
			p.put("tenant", tenant);
			String tenant_prefix = userDao.getTenantPrefix(p);
			

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("userid", userid);
			
			for ( NumberTenantType n : list ) {
				param.put("extension_id", n.getNumber());
				param.put("tenant", n.getTenant());
				param.put("cm", convertUCEMedia(cm));
				
				userDao.deleteUserExtension(param);
				
				logHistoryTable("uc_extension", tenant_prefix + n.getNumber(), UCHistory.HISTORY_TYPE.UPDATE);
			}

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}

		return ret;
	}

	// 태넌트 추가
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}", method = RequestMethod.POST)
	public void createTenant(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#createTenant cm:{} / tenant:{}", cm, tenant);

		ObjectMapper mapper = new ObjectMapper();

		Tenant t = null;

		try {
			try {
				t = mapper.readValue(data, Tenant.class);
				t.setCm(cm);
				t.setTenant(tenant);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			//
			// r.sendMessageO("createTenant", mapper.writeValueAsString(param));
			
			Tenant param = new Tenant();
			param.setCm(cm);
			param.setTenant(tenant);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			Tenant d = dao.getTenantInfo(param);
				
			if ( d != null) {
				throw new Exception("Already Exist Tenant");
			}
			
			/*
			media_id : EMS 에서 전달받은 CM ID
			tenent_id : EMS 에서 전달받은 태넌트 ID
			media_tenant : UCE 에서 사용할 태넌트 값 ( unique 해야함 )
			*/
			dao.createTenant(t);
			dao.createCsTenant(t);
			dao.createTenantAdmin(t);
			dao.createTenantOrg(t);

			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// 태넌트 조회
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}", method = RequestMethod.GET)
	public Tenant viewTenant(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#viewTenant cm:{} / tenant:{}", cm, tenant);

		Tenant param = new Tenant();
		param.setCm(cm);
		param.setTenant(tenant);

		ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		Tenant data = dao.getTenantInfo(param);

		if (data == null) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Exist Tenant");
		}
		return data;
	}

	// 태넌트 변경
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}", method = RequestMethod.PUT)
	public Tenant modifyTenant(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#modifyTenant : cm{} tenant:{}", cm, tenant);

		ObjectMapper mapper = new ObjectMapper();

		Tenant t = null;

		try {
			try {
				t = mapper.readValue(data, Tenant.class);
				t.setCm(cm);
				t.setTenant(tenant);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			//
			// r.sendMessageCTO("modifyTenant", cm, tenant, mapper.writeValueAsString(param));
			
			Tenant param = new Tenant();
			param.setCm(cm);
			param.setTenant(tenant);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			Tenant d = dao.getTenantInfo(param);
			
			if ( d == null) {
				throw new Exception("Not Exist Tenant");
			}
			
			dao.modifyTenant(t);
			
			// 관련 사용자 정보 수정
			dao.modifyTenantUser(t);
			List<String> userList = dao.getTenantUsers(t);
			
			for (String userid : userList) {
				logHistoryTable("uc_user", userid, UCHistory.HISTORY_TYPE.UPDATE);
			}

			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}

		return t;
	}

	// 태넌트 삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}", method = RequestMethod.DELETE)
	public void deleteTenant(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, HttpServletResponse res) throws Exception {

		logger.info("#deleteTenant cm:{} / tenant:{}", cm, tenant);

		try {
			//
			// r.sendMessageCT("deleteTenant", cm, tenant);
			
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", cm);
			param.put("tenant", tenant);

			
			Tenant p = new Tenant();
			p.setCm(cm);
			p.setTenant(tenant);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			Tenant d = dao.getTenantInfo(p);
			
			if ( d == null) {
				throw new Exception("Not Exist Tenant");
			}
			
			dao.deleteTenant(param);
			dao.deleteCsTenant(param);
			dao.deleteTenantOrg(param);

			// 관련 사용자 정보 삭제
			List<String> userList = dao.getTenantUsers(p);

			dao.deleteTenantAdmin(param);
			for (String userid : userList) {
				logHistoryTable("uc_user", userid, UCHistory.HISTORY_TYPE.DELETE);
			}

			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	// 사이트 추가
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/site/{site}", method = RequestMethod.POST)
	public void createSite(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("site") String site, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#createSite cm:{} / tenant:{}", cm, tenant);

		ObjectMapper mapper = new ObjectMapper();

		Site s = null;

		try {
			try {
				s = mapper.readValue(data, Site.class);
				s.setCm(cm);
				s.setTenant(tenant);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("Wrong JSON Format.");
			}

			Site p = new Site();
			p.setCm(cm);
			p.setTenant(tenant);
			p.setSite(site);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			Site d = dao.getSiteInfo(p);
			
			if ( d != null) {
				throw new Exception("Already Exist Site");
			}
			
			//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", cm);
			param.put("tenant", tenant);
			param.put("site", site);
			param.put("sitename", s.getSitename());
			param.put("companyaddress", s.getCompanyaddress());
			param.put("site_idx", "0");
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			userDao.createSite(param);
			
			logHistoryTable("uc_organizer", param.get("site_idx").toString(), UCHistory.HISTORY_TYPE.INSERT);
			
			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	// 사이트  조회
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/site/{site}", method = RequestMethod.GET)
	public Site viewSite(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("site") String site, HttpServletResponse res) throws Exception {

		logger.info("#viewSite cm:{} / tenant:{}", cm, tenant);

		Site param = new Site();
		param.setCm(cm);
		param.setTenant(tenant);
		param.setSite(site);

		ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		Site data = dao.getSiteInfo(param);

		if (data==null) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Exist Site");
		}
			
		
		return data;
	}

	// 사이트  변경
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/site/{site}", method = RequestMethod.PUT)
	public Site modifySite(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("site") String site, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#modifySite : cm{} tenant:{}", cm, tenant);

		ObjectMapper mapper = new ObjectMapper();

		Site s = null;

		try {
			try {
				s = mapper.readValue(data, Site.class);
				s.setCm(cm);
				s.setTenant(tenant);
				s.setSite(site);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", cm);
			param.put("tenant", tenant);
			param.put("site", site);
			param.put("sitename", s.getSitename());
			param.put("companyaddress", s.getCompanyaddress());
			param.put("site_idx", "0");
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			Site d = dao.getSiteInfo(s);

			if (d==null) {
				throw new Exception("Not Exist Site");
			}
				
			
			userDao.modifySite(param);

			logHistoryTable("uc_organizer", param.get("site_idx").toString(), UCHistory.HISTORY_TYPE.UPDATE);
			
			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}

		return s;
	}

	// 사이트  삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/site/{site}", method = RequestMethod.DELETE)
	public void deleteSite(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("site") String site, HttpServletResponse res) throws Exception {

		logger.info("#deleteSite cm:{} / tenant:{}", cm, tenant);

		try {
			//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", cm);
			param.put("tenant", tenant);
			param.put("site", site);
			param.put("site_idx", "0");
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			Site d = new Site();
			d.setCm(cm);
			d.setTenant(tenant);
			d.setSite(site);

			Site d1 = userDao.getSiteInfo(d);
			if (d1==null) {
				throw new Exception("Not Exist Site");
			}
			
			userDao.deleteSite(param);

			logHistoryTable("uc_organizer", param.get("site_idx").toString(), UCHistory.HISTORY_TYPE.DELETE);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	/* 주소록 .................................. */
	/* .................................. 주소록 */
	// 주소록 추가
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/addr/{addr}", method = RequestMethod.POST)
	public void createAddressBook(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("addr") String addr, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#createAddressBook cm:{} / tenant:{}", cm, tenant);
		logger.info("#createAddressBook data : {}", data);

		ObjectMapper mapper = new ObjectMapper();

		AddressBook s = null;

		try {
			try {
				s = mapper.readValue(data, AddressBook.class);
				s.setCm(cm);
				s.setTenant(tenant);
				s.setId(addr);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			AddressBook param = new AddressBook();
			param.setCm(cm);
			param.setTenant(tenant);
			param.setId(addr);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			AddressBook d= dao.getAddressBookInfo(param);
			if ( d != null ) {
				throw new Exception("Already Exist AddressBook");
			}
			
			//			
			dao.createAddressBook(s);
			
			logHistoryTable("uc_public_addrbook", addr, UCHistory.HISTORY_TYPE.INSERT);
			
			res.sendError(HttpServletResponse.SC_CREATED);
		} catch (Exception e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
			logger.error(e.getMessage(), e);
		}
	}

	// 주소록  조회
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/addr/{addr}", method = RequestMethod.GET)
	public AddressBook viewAddressBook(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("addr") String addr, HttpServletResponse res) throws Exception {

		logger.info("#viewAddressBook cm:{} / tenant:{}", cm, tenant);

		AddressBook param = new AddressBook();
		param.setCm(cm);
		param.setTenant(tenant);
		param.setId(addr);

		ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		AddressBook data = dao.getAddressBookInfo(param);

		if (data==null) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Exist AddressBook");
		}
		
		return data;
	}

	// 주소록  변경
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/addr/{addr}", method = RequestMethod.PUT)
	public AddressBook modifyAddressBook(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("addr") String addr, @RequestBody String data, HttpServletResponse res) throws Exception {

		logger.info("#modifyAddressBook : cm{} tenant:{}", cm, tenant);

		ObjectMapper mapper = new ObjectMapper();

		AddressBook s = null;

		try {
			try {
				s = mapper.readValue(data, AddressBook.class);
				s.setCm(cm);
				s.setTenant(tenant);
				s.setId(addr);
			} catch (Exception e1) {
				throw new Exception("Wrong JSON Format.");
			}

			//
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			ApiV1Dao dao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			AddressBook d = dao.getAddressBookInfo(s);

			if (d==null) {
				throw new Exception("Not Exist AddressBook");
			}
				
			
			userDao.modifyAddressBook(s);

			logHistoryTable("uc_public_addrbook", s.getId(), UCHistory.HISTORY_TYPE.UPDATE);
			
			res.sendError(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}

		return s;
	}

	// 주소록  삭제
	@RequestMapping(value = "/ucex/api/v1/cm/{cm}/tenant/{tenant}/addr/{addr}", method = RequestMethod.DELETE)
	public void deleteAddressBook(@PathVariable("tenant") String tenant,
			@PathVariable("cm") String cm, @PathVariable("addr") String addr, HttpServletResponse res) throws Exception {

		logger.info("#deleteAddressBook cm:{} / tenant:{}", cm, tenant);

		try {
			//
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", cm);
			param.put("tenant", tenant);
			param.put("addr", addr);
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);

			AddressBook d = new AddressBook();
			d.setCm(cm);
			d.setTenant(tenant);
			d.setId(addr);

			AddressBook d1 = userDao.getAddressBookInfo(d);
			if (d1==null) {
				throw new Exception("Not Exist AddressBook");
			}
			
			userDao.deleteAddressBook(d);

			logHistoryTable("uc_public_addrbook", addr, UCHistory.HISTORY_TYPE.DELETE);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	public void logHistoryTable(String table, String key, UCHistory.HISTORY_TYPE type) {

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("history_table", table);
		param.put("key", key);

		if (type == UCHistory.HISTORY_TYPE.INSERT) {
			param.put("type", "I");
		} else if (type == UCHistory.HISTORY_TYPE.UPDATE) {
			param.put("type", "U");
		} else if (type == UCHistory.HISTORY_TYPE.DELETE) {
			param.put("type", "D");
		} else {
			logger.debug("Unkown HISTORY type ... !");
			return;
		}

		//ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		//userDao.logHistoryTable(param);
		
		// RMQ 에 enqueue !
		try {
			r.sendMessage(param);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		//
	}

	// Cloud System Backup Start
	@RequestMapping(value = "/ucex/api/cloud/system/db/backup/start", method = RequestMethod.POST)
	public @ResponseBody String cloudSystemBackupStart(@RequestBody String data, HttpServletResponse res) throws Exception {
		logger.info("#cloudSystemBackupStart req-backup_id='{}'", data);

		ObjectMapper mapper = new ObjectMapper();

		SystemBackup req = null;
		SystemBackupResponse resp = null;
		String	backupId = "";

		// Backup 상태 코드를 반환
		resp = new SystemBackupResponse();
		resp.setCode(IDataBackupTask.CTRL_START_INTERNAL_ERROR);
		resp.setDesc("");
		
		logger.debug("#cloudSystemBackupStart init resp='{}'", resp.toString());

		try {
			req = mapper.readValue(data, SystemBackup.class);
			logger.debug("#cloudSystemBackupStart backup_id='{}'", req.toString());
			
			backupId = req.getBackup_id();
			if(backupId == null || backupId.isEmpty()) {
				resp.setCode(HttpServletResponse.SC_BAD_REQUEST);
				/*res.sendError(HttpServletResponse.SC_BAD_REQUEST, "CLOUD BACKUP START : 'backup_id' is empty.");*/
				return resp.toJsonString();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			resp.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			/*res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CLOUD BACKUP START : Wrong JSON Format.");*/
			return resp.toJsonString();
		}

		try {
			logger.debug("#cloudSystemBackupStart {}", systemBackupConfig.toString());

			if(emsBackupTask != null && emsBackupTask.isRunning()) {
				if(backupId.equalsIgnoreCase(emsBackupTask.getTaskId())) {
					resp.setCode(IDataBackupTask.CTRL_STARTED);
				} else {
					resp.setCode(IDataBackupTask.CTRL_START_INTERNAL_ERROR);
				}
			} else {
				if(emsBackupTask == null || (emsBackupTask != null && emsBackupTask.isEnded())) {
					emsBackupTask = new BackupDataTask(systemBackupConfig);

					emsBackupTask.setTaskId(backupId);
					emsBackupTask.start();
					
					try {
						// Backup Thread가 정상적으로 Start 되고 나서 상태체크하도록 강제 Sleep
						Thread.sleep(200);
					} catch (Exception e) {}

					if(emsBackupTask.isRunning()) {
						resp.setCode(IDataBackupTask.CTRL_START_OK);
					} else {
						resp.setCode(IDataBackupTask.CTRL_START_INTERNAL_ERROR);
					}
				}
			}
			return resp.toJsonString();
			/*res.sendError(HttpServletResponse.SC_OK, resp.toJsonString());*/
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resp.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			/*res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());*/
			return resp.toJsonString();
		}
	}

	// Cloud System Backup Stop
	@RequestMapping(value = "/ucex/api/cloud/system/db/backup/stop", method = RequestMethod.POST)
	public @ResponseBody String cloudSystemBackupStop(@RequestBody String data, HttpServletResponse res) throws Exception {
		logger.info("#cloudSystemBackupStop req-backup_id='{}'", data);

		ObjectMapper mapper = new ObjectMapper();

		SystemBackup req = null;
		SystemBackupResponse resp = null;
		String	backupId = "";

		// Backup 상태 코드를 반환
		resp = new SystemBackupResponse();
		resp.setCode(IDataBackupTask.CTRL_STOP_INTERNAL_ERROR);
		resp.setDesc("");

		logger.debug("#cloudSystemBackupStop init resp='{}'", resp.toString());

		try {
			req = mapper.readValue(data, SystemBackup.class);
			logger.debug("#cloudSystemBackupStop backup_id='{}'", req.toString());
			
			backupId = req.getBackup_id();
			if(backupId == null || backupId.isEmpty()) {
				resp.setCode(HttpServletResponse.SC_BAD_REQUEST);
				/*res.sendError(HttpServletResponse.SC_BAD_REQUEST, "CLOUD BACKUP STOP : 'backup_id' is empty.");*/
				return resp.toJsonString();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			resp.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			/*res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CLOUD BACKUP STOP : Wrong JSON Format.");*/
			return resp.toJsonString();
		}

		// Backup ?곹깭 肄붾뱶瑜?諛섑솚
		try {
			if(emsBackupTask == null) {
				resp.setCode(IDataBackupTask.CTRL_STOP_INTERNAL_ERROR);
			} else {
				if(backupId.equalsIgnoreCase(emsBackupTask.getTaskId())) {
					if(emsBackupTask.isEnded()) {
						resp.setCode(IDataBackupTask.CTRL_STOPPED);
					} else if(emsBackupTask.isRunning()) {
						try {
							emsBackupTask.shutdownNow();
							
							try {
								// Backup Thread가 정상적으로 Stop 되고 나서 상태체크하도록 강제 Sleep
								Thread.sleep(200);
							} catch (Exception e) {}

							if(emsBackupTask.isEnded()) {
								resp.setCode(IDataBackupTask.CTRL_STOPPED);
							} else {
								resp.setCode(IDataBackupTask.CTRL_STOP_OK);
							}
							
							emsBackupTask = null;
						} catch (Exception e) {
							resp.setCode(IDataBackupTask.CTRL_STOP_INTERNAL_ERROR);
						}
					} else {
						resp.setCode(IDataBackupTask.CTRL_STOP_OK);
					}
				} else {
					resp.setCode(IDataBackupTask.CTRL_STOP_INTERNAL_ERROR);
				}
			}

			/*res.sendError(HttpServletResponse.SC_OK, resp.toJsonString());*/
			return resp.toJsonString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resp.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			/*res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());*/
			return resp.toJsonString();
		}
	}

	// Cloud System Backup Status Query
	@RequestMapping(value = "/ucex/api/cloud/system/db/backup", method = RequestMethod.GET)
	public @ResponseBody String cloudSystemBackupStatus(@RequestBody String data, HttpServletResponse res) throws Exception {
		logger.info("#cloudSystemBackupStatus req-backup_id='{}'", data);

		ObjectMapper mapper = new ObjectMapper();

		SystemBackup req = null;
		SystemBackupResponse resp = null;
		String	backupId = "";

		// Backup 상태 코드를 반환
		resp = new SystemBackupResponse();
		resp.setCode(IDataBackupTask.CTRL_STATE_INTERNAL_ERROR);

		logger.debug("#cloudSystemBackupStatus init resp='{}'", resp.toString());

		try {
			req = mapper.readValue(data, SystemBackup.class);
			logger.debug("#cloudSystemBackupStatus backup_id='{}'", req.toString());
			
			backupId = req.getBackup_id();
			if(backupId == null || backupId.isEmpty()) {
				resp.setCode(HttpServletResponse.SC_BAD_REQUEST);
				/*res.sendError(HttpServletResponse.SC_BAD_REQUEST, "CLOUD BACKUP STATE : 'backup_id' is empty.");*/
				return resp.toJsonString();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			resp.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			/*res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CLOUD BACKUP STATE : Wrong JSON Format.");*/
			return resp.toJsonString();
		}

		// Backup 상태 코드를 반환
		try {
			if(emsBackupTask == null) {
				resp.setCode(IDataBackupTask.CTRL_STATE_OK);
			} else {
				if(backupId.equalsIgnoreCase(emsBackupTask.getTaskId())) {
					if(emsBackupTask.isRunning()) {
							resp.setCode(IDataBackupTask.CTRL_STATE_RUNNING);
					} else if(emsBackupTask.isError()) {
							resp.setCode(IDataBackupTask.CTRL_STATE_INTERNAL_ERROR);
					} else {
						resp.setCode(IDataBackupTask.CTRL_STATE_OK);
					}
				} else {
					resp.setCode(IDataBackupTask.CTRL_STATE_INTERNAL_ERROR);
				}
			}

			/*res.sendError(HttpServletResponse.SC_OK, resp.toJsonString());*/
			return resp.toJsonString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resp.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			/*res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());*/
			return resp.toJsonString();
		}
	}
	
		// REST API 테스트용 컨트롤러
		/*@RequestMapping(value = "/ucex/api/v1_test", method = RequestMethod.POST)
		public @ResponseBody void TEST(@RequestBody String data, HttpServletResponse res) throws Exception {
			logger.info("#cloudSystemBackupStatus req-backup_id='{}'", data);
		}*/

}
