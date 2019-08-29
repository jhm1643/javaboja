package api.bo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.config.UcexTestProperties;
import api.dao.ApiV1Dao;
import api.domain.BusinessObject;
import api.domain.ExtensionCommand;
import api.domain.ExtensionData;
import api.domain.MadnMemberRequest;
import api.domain.NumberTenantType;
import api.domain.Rabbit;
import api.domain.Tenant;
import api.domain.UserInfo;
import api.domain.UserMemberRequest;
import api.utils.UCHistory;
 
@Component
@EnableConfigurationProperties(UcexTestProperties.class)
public class BusinessWorker {

	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	private Rabbit r;
	
	@Autowired
	private UcexTestProperties testMode;
	
	//2019.01.15 martino EMS로부터 오는 정보중에 '|'가 포함되어 있어서 DELIMITER 사용불가하여 변경
	//private final String DELIMITER = "|";
	private final String DELIMITER = "\u0002";
	
	private static Logger logger = LoggerFactory.getLogger(BusinessWorker.class);

	public void run(BusinessObject bo, String message) {

		switch (bo.getCommand()) {

		case "login":
			break;

		case "logout":
			break;

		case "createExtension":
			createExtension(bo, message);
			break;

		case "modifyExtension":
			modifyExtension(bo, message);
			break;

		case "deleteExtension":
			deleteExtension(bo, message);
			break;

		case "createMADN":
			createMADN(bo, message);
			break;

		case "deleteMADN":
			deleteMADN(bo, message);
			break;

		case "addMemberMADN":
			addMemberMADN(bo, message);
			break;

		case "deleteMemberMADN":
			deleteMemberMADN(bo, message);
			break;

		case "createUser":
			createUser(bo, message);
			break;

		case "modifyUser":
			modifyUser(bo, message);
			break;

		case "deleteUser":
			deleteUser(bo, message);
			break;

		case "addUserExtension":
			addUserExtension(bo, message);
			break;

		case "deleteUserExtension":
			deleteUserExtension(bo, message);
			break;


		case "createTenant":
			createTenant(bo, message);
			break;
			
		case "modifyTenant":
			modifyTenant(bo, message);
			break;

		case "deleteTenant":
			deleteTenant(bo, message);
			break;

			
		/* ACD commands */
		case "addRepNumber":
			addRepNumber(bo, message);
			break;

		case "deleteRepNumber":
			deleteRepNumber(bo, message);
			break;

		case "deleteAllRepNumber":
			deleteAllRepNumber(bo, message);
			break;

		case "addSupervisor":
			addSupervisor(bo, message);
			break;

		case "deleteSupervisor":
			deleteSupervisor(bo, message);
			break;

		case "deleteAllSupervisor":
			deleteAllSupervisor(bo, message);
			break;

		case "addAgentNumber":
			addAgentNumber(bo, message);
			break;

		case "deleteAgentNumber":
			deleteAgentNumber(bo, message);
			break;

		case "deleteAllAgentNumber":
			deleteAllAgentNumber(bo, message);
			break;

		}

		/*
		// test
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		int n = userDao.getUserCount();
		logger.info("############ userDao [{}] get user count : {}", userDao, n);
		*/
	}

	private void deleteTenant(BusinessObject bo, String message) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("cm", bo.getCm());
		param.put("tenant", bo.getTenant());
		param.put("tenant_idx", "0");
		
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.deleteTenant(param);
		
		logHistoryTable("uc_tenant_info", param.get("tenant_idx").toString(), UCHistory.HISTORY_TYPE.DELETE);
	}

	private void modifyTenant(BusinessObject bo, String message) {
		ObjectMapper mapper = new ObjectMapper();
		Tenant t = null;
		try {
			t = mapper.readValue(message, Tenant.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}		
		
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.modifyTenant(t);
		
		logHistoryTable("uc_tenant_info", t.getTenant_idx(), UCHistory.HISTORY_TYPE.UPDATE);
	}

	private void createTenant(BusinessObject bo, String message) {
		ObjectMapper mapper = new ObjectMapper();
		Tenant t = null;
		try {
			t = mapper.readValue(message, Tenant.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.createTenant(t);
		
		logHistoryTable("uc_tenant_info", t.getTenant_idx(), UCHistory.HISTORY_TYPE.INSERT);
	}

	private void deleteUserExtension(BusinessObject bo, String message) {
		
		ObjectMapper mapper = new ObjectMapper();
		UserMemberRequest r = null;
		try {
			r = mapper.readValue(message, UserMemberRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}		
		
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		
		List<NumberTenantType> list = r.getData();

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userid", bo.getParam());
		
		for ( NumberTenantType n : list ) {
			param.put("extension_id", n.getNumber());
			param.put("tenant", n.getTenant());
			param.put("cm", bo.getCm());
			
			userDao.deleteUserExtension(param);
			
			logHistoryTable("uc_extension", n.getNumber(), UCHistory.HISTORY_TYPE.UPDATE);
		}
	}

	private void addUserExtension(BusinessObject bo, String message) {
		ObjectMapper mapper = new ObjectMapper();
		UserMemberRequest r = null;
		try {
			r = mapper.readValue(message, UserMemberRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}		
		
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		
		List<NumberTenantType> list = r.getData();

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("userid", bo.getParam());
		param.put("cm", bo.getCm());
		
		for ( NumberTenantType n : list ) {
			param.put("extension_id", n.getNumber());
			param.put("tenant", n.getTenant());
			
			userDao.addUserExtension(param);
			
			logHistoryTable("uc_extension", n.getNumber(), UCHistory.HISTORY_TYPE.UPDATE);
		}
		
	}

	private void deleteUser(BusinessObject bo, String message) {
		/*
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.deleteUser(bo);
		userDao.deleteUserOption(bo);

		logHistoryTable("uc_user", bo.getParam(), UCHistory.HISTORY_TYPE.DELETE);
		logHistoryTable("uc_user_optoin", bo.getParam(), UCHistory.HISTORY_TYPE.DELETE);
		*/
	}

	private void modifyUser(BusinessObject bo, String message) {
		ObjectMapper mapper = new ObjectMapper();
		UserInfo u = null;
		try {
			u = mapper.readValue(message, UserInfo.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}		
		
		u.setCm(bo.getCm());
		u.setTenant(bo.getTenant());
		u.setUserid(bo.getParam());

		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.modifyUser(u);
		userDao.modifyUserOption(u);

		logHistoryTable("uc_user", u.getUserid(), UCHistory.HISTORY_TYPE.UPDATE);
		logHistoryTable("uc_user_optoin", u.getUserid(), UCHistory.HISTORY_TYPE.UPDATE);
	}

	private void createUser(BusinessObject bo, String message) {
		ObjectMapper mapper = new ObjectMapper();
		UserInfo u = null;
		try {
			u = mapper.readValue(message, UserInfo.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		u.setCm(bo.getCm());
		u.setTenant(bo.getTenant());
		u.setUserid(bo.getParam());

		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.createUser(u);
		userDao.createUserOption(u);

		logHistoryTable("uc_user", u.getUserid(), UCHistory.HISTORY_TYPE.INSERT);
		logHistoryTable("uc_user_optoin", u.getUserid(), UCHistory.HISTORY_TYPE.INSERT);
	}

	private void deleteMemberMADN(BusinessObject bo, String message) {

		String cm = bo.getCm();
		String tenant = bo.getTenant();
		String madn = bo.getParam();

		ObjectMapper mapper = new ObjectMapper();
		MadnMemberRequest mmr = null;
		try {
			mmr = mapper.readValue(message, MadnMemberRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		List<ExtensionCommand> l = mmr.getData();

		for (ExtensionCommand ec : l) {
			StringBuffer sb = new StringBuffer();
			sb.append("SADN").append(DELIMITER).append("D").append(DELIMITER);
			sb.append(cm).append(DELIMITER).append(tenant).append(DELIMITER).append(madn).append(DELIMITER).append(ec.getNumber());

			if (testMode.getMode().equals("true")) {

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("cm", cm);
				param.put("tenant", tenant);
				param.put("madn", madn);
				param.put("sadn", ec.getNumber());
				
				ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
				userDao.deleteMemberMADN(param);
			}
			else {
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			
		}
	}

	private void addMemberMADN(BusinessObject bo, String message) {

		String cm = bo.getCm();
		String tenant = bo.getTenant();
		String madn = bo.getParam();

		ObjectMapper mapper = new ObjectMapper();
		MadnMemberRequest mmr = null;
		try {
			mmr = mapper.readValue(message, MadnMemberRequest.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		List<ExtensionCommand> l = mmr.getData();

		for (ExtensionCommand ec : l) {
			StringBuffer sb = new StringBuffer();
			sb.append("SADN").append(DELIMITER).append("I").append(DELIMITER);
			sb.append(cm).append(DELIMITER).append(tenant).append(DELIMITER).append(madn).append(DELIMITER).append(ec.getNumber());

			if (testMode.getMode().equals("true")) {

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("cm", cm);
				param.put("tenant", tenant);
				param.put("madn", madn);
				param.put("sadn", ec.getNumber());
				
				ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
				userDao.addMemberMADN(param);
			}
			else {
				
				try {
					r.sendCubeMessage(sb.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void deleteMADN(BusinessObject bo, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("MADN").append(DELIMITER).append("D").append(DELIMITER);
		sb.append(bo.getCm()).append(DELIMITER).append(bo.getTenant()).append(DELIMITER).append(bo.getParam());
		
		if (testMode.getMode().equals("true")) {

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", bo.getCm());
			param.put("tenant", bo.getTenant());
			param.put("madn", bo.getParam());
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			userDao.deleteMADN(param);
		}
		else {
			
			try {
				r.sendCubeMessage(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}

	private void createMADN(BusinessObject bo, String message) {
		StringBuffer sb = new StringBuffer();
		sb.append("MADN").append(DELIMITER).append("I").append(DELIMITER);
		sb.append(bo.getCm()).append(DELIMITER).append(bo.getTenant()).append(DELIMITER).append(bo.getParam());
		
		if (testMode.getMode().equals("true")) {

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", bo.getCm());
			param.put("tenant", bo.getTenant());
			param.put("madn", bo.getParam());
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			userDao.createMADN(param);
		}
		else {
			
			try {
				r.sendCubeMessage(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void deleteExtension(BusinessObject bo, String message) {

		StringBuffer sb = new StringBuffer();
		sb.append("EXTENSION").append(DELIMITER).append("D").append(DELIMITER);
		sb.append(bo.getCm()).append(DELIMITER).append(bo.getTenant()).append(DELIMITER).append(bo.getParam());
		
		if (testMode.getMode().equals("true")) {

			Map<String, Object> param = new HashMap<String, Object>();
			param.put("cm", bo.getCm());
			param.put("tenant", bo.getTenant());
			param.put("extension", bo.getParam());
			
			ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
			userDao.deleteExtension(param);
		}
		else {
			
			try {
				r.sendCubeMessage(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void modifyExtension(BusinessObject bo, String message) {

		ObjectMapper mapper = new ObjectMapper();
		ExtensionData data = null;
		try {
			data = mapper.readValue(message, ExtensionData.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("cm", data.getCm());
		param.put("tenant", data.getTenant());
		String tenant_prefix = userDao.getTenantPrefix(param);
		
		StringBuffer sb = new StringBuffer();
		sb.append("EXTENSION").append(DELIMITER).append("U").append(DELIMITER);
		sb.append(data.getCm()).append(DELIMITER).append(data.getTenant()).append(DELIMITER).append(data.getDn()).append(DELIMITER);
		sb.append(data.getTerm_type()).append(DELIMITER).append(data.getNumber_type()).append(DELIMITER);
		sb.append(data.getIp()).append(DELIMITER).append(data.getMac()).append(DELIMITER);
		sb.append(data.getUser_id()).append(DELIMITER).append(data.getOutgoing_cid()).append(DELIMITER);
		sb.append(data.getAuth_id()).append(DELIMITER).append(data.getAuth_password()).append(DELIMITER).append(data.getScti_usage()).append(DELIMITER);
		sb.append(tenant_prefix);

		if (testMode.getMode().equals("true")) {

			userDao.modifyExtension(data);
		}
		else {
			
			try {
				r.sendCubeMessage(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
	}

	private void createExtension(BusinessObject bo, String message) {

		ObjectMapper mapper = new ObjectMapper();
		ExtensionData data = null;
		try {
			data = mapper.readValue(message, ExtensionData.class);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("cm", data.getCm());
		param.put("tenant", data.getTenant());
		String tenant_prefix = userDao.getTenantPrefix(param);
		
		StringBuffer sb = new StringBuffer();
		sb.append("EXTENSION").append(DELIMITER).append("I").append(DELIMITER);
		sb.append(data.getCm()).append(DELIMITER).append(data.getTenant()).append(DELIMITER).append(data.getDn()).append(DELIMITER);
		sb.append(data.getTerm_type()).append(DELIMITER).append(data.getNumber_type()).append(DELIMITER);
		sb.append(data.getIp()).append(DELIMITER).append(data.getMac()).append(DELIMITER);
		sb.append(data.getUser_id()).append(DELIMITER).append(data.getOutgoing_cid()).append(DELIMITER);
		sb.append(data.getAuth_id()).append(DELIMITER).append(data.getAuth_password()).append(DELIMITER).append(data.getScti_usage()).append(DELIMITER);
		sb.append(tenant_prefix);
		
		if (testMode.getMode().equals("true")) {

			userDao.createExtension(data);
		}
		else {
			
			try {
				r.sendCubeMessage(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void generateDeviceType(ExtensionData data) {
		int dType = Integer.parseInt(data.getTerm_type());
		String hexString = Integer.toHexString(dType);

		if (hexString.length() == 3) {
			data.setDevice_submodel(hexString.substring(1));
			data.setDevice_model(hexString.substring(0, 1));
		}
	}

	//
	// 요렇게 호출 하세요
	// logHistoryTable( "TABLENAME", "KEY", UCHistory.HISTORY_TYPE.UPDATE);
	//
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

		ApiV1Dao userDao = sqlSessionTemplate.getMapper(ApiV1Dao.class);
		userDao.logHistoryTable(param);
	}
	
	

	/* ACD commands */

	private void deleteAllAgentNumber(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void deleteAgentNumber(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void addAgentNumber(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void deleteAllSupervisor(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void deleteSupervisor(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void addSupervisor(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void deleteAllRepNumber(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void deleteRepNumber(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}

	private void addRepNumber(BusinessObject bo, String message) {
		// TODO Auto-generated method stub

	}
}
