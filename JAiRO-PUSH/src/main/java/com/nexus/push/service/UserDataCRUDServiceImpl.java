package com.nexus.push.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.nexus.push.dao.PushLanguageDao;
import com.nexus.push.dao.PushLocationDao;
import com.nexus.push.dao.PushMemberDao;
import com.nexus.push.dao.PushTokenHistoryDao;
import com.nexus.push.domain.HttpResponseVo;
import com.nexus.push.entity.PushLanguage;
import com.nexus.push.entity.PushLocation;
import com.nexus.push.entity.PushMember;
import com.nexus.push.entity.PushTokenHistory;
import com.nexus.push.util.HttpStatusCode;
@Service
public class UserDataCRUDServiceImpl extends HttpStatusCode implements UserDataCRUDService {

	@Autowired
	private PushMemberDao pushMemberDao;
	@Autowired
	private PushTokenHistoryDao pushTokenHistoryDao;
	@Autowired
	private PushLocationDao pushLocationDao;
	@Autowired
	private PushLanguageDao pushLanguageDao;
	@Override
	public ResponseEntity<HttpResponseVo> post(PushMember pushMember) {
		try {
			for(PushLanguage pushLanguage : pushLanguageDao.selectAll()) {
				if(pushLanguage.getLanguage().equals(pushMember.getLanguage())) {
					pushMember.setLang_id(pushLanguage.getLang_id());
					break;
				}
				return ResponseEntity.status(STATUS_500_CODE)
									 .body(new HttpResponseVo(STATUS_500_CODE, PUSH_FAIL, CODE_500_LANGUAGE_ERROR));
			}
			for(PushLocation pushLocation : pushLocationDao.selectAll()) {
				if(pushLocation.getBig_city_name().equals(pushMember.getLocation())) {
					pushMember.setLoc_id(pushLocation.getLoc_id());
					break;
				}
				return ResponseEntity.status(STATUS_500_CODE)
						 .body(new HttpResponseVo(STATUS_500_CODE, PUSH_FAIL, CODE_500_LOCATION_ERROR));
			}
			pushMemberDao.merge(pushMember);
			pushTokenHistoryDao.insert(PushTokenHistory.builder().device_token(pushMember.getToken_id())
																 .device_type(pushMember.getDevice_type())
																 .build());
		}catch(Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return ResponseEntity.status(STATUS_500_CODE)
								 .body(new HttpResponseVo(STATUS_500_CODE, PUSH_FAIL, e.getMessage()));
		}
		return ResponseEntity.status(STATUS_200_CODE)
							 .body(new HttpResponseVo(STATUS_200_CODE, PUSH_SUCCESS, ""));
	}

}
